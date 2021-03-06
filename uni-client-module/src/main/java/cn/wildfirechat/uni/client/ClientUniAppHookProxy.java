package cn.wildfirechat.uni.client;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wildfirechat.client.ConnectionStatus;
import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserOnlineState;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.uni.client.jsmodel.JSConversationInfo;
import cn.wildfirechat.uni.client.jsmodel.JSMessage;
import io.dcloud.feature.uniapp.UniAppHookProxy;

public class ClientUniAppHookProxy implements UniAppHookProxy {

    private static final String TAG = "ClientUniAppHookProxy";
    static SerializeConfig serializeConfig;

    @Override
    public void onSubProcessCreate(Application application) {
//        initWFClient(application);
    }

    @Override
    public void onCreate(Application application) {
        Log.d(TAG, "application OnCreate " + isWfcUIKitEnable());
        if (!this.isWfcUIKitEnable()) {
            initWFClient(application);

            serializeConfig = new SerializeConfig();
            serializeConfig.put(Long.class, WfLongCodec.instance);
            JSON.DEFAULT_GENERATE_FEATURE = SerializerFeature.config(JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.WriteEnumUsingToString, false);
        } else {
            // do nothing, 由 uikit 层去负责初始化
        }
    }

    // client 不支持音视频处理、pc 登录处理
    private void initWFClient(Application application) {
        ChatManager.init(application, null);
        ChatManager chatManager = ChatManager.Instance();
        try {
            chatManager.startLog();
        } catch (NotInitializedExecption notInitializedExecption) {
            notInitializedExecption.printStackTrace();
        }

        Log.i(TAG, "初始化事件监听");
        try {
            Class<?> ChatManagerClazz = chatManager.getClass();
            Method[] ChatManagerMethods = ChatManagerClazz.getDeclaredMethods();

            Pattern pattern = Pattern.compile("add(.*)Listener");

            for (Method method : ChatManagerMethods) {
                Matcher matcher = pattern.matcher(method.getName());
                if (matcher.find()) {
                    Class[] paramTypes = method.getParameterTypes();
                    Log.i(TAG, paramTypes[0].getDeclaredMethods()[0].getName());
                    WildfireListenerHandler wildfireListenerHandler = new WildfireListenerHandler();
                    Object Listener = Proxy.newProxyInstance(
                        ClientModule.class.getClassLoader(),
                        new Class[]{paramTypes[0]},
                        wildfireListenerHandler);
                    method.invoke(chatManager, Listener);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private boolean isWfcUIKitEnable() {
        String uikitClazz = "cn.wildfire.chat.kit.WfcUIKit";
        try {
            Class clazz = Class.forName(uikitClazz);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}

class WildfireListenerHandler implements InvocationHandler {
    private static final String TAG = "WildfireListenerHandler";

    /**
     * @param proxy  所代理的那个真实对象
     * @param method 我们所要调用真实对象的某个方法的Method对象
     * @param args   调用真实对象某个方法时接受的参数
     * @return 代理执行完方法所返回的对象
     * @throws Throwable 执行过程抛出的各种异常
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        int status = ChatManager.Instance().getConnectionStatus();
        // 回调 js 层时，好像有大小限制，先规避一下
        if ("onReceiveMessage".equals(methodName) && status == ConnectionStatus.ConnectionStatusReceiveing) {
            List list = (List) args[0];
            if (list.size() > 100) {
                return null;
            }
        }
        JSONArray array = new JSONArray();
        array.add(methodName);
        if (args != null) {

            switch (methodName) {
                case "onMessageDelivered":
                    array.add(JSONObject.toJSONString(Util.strLongMap2Array((Map<String, Long>) args[0]), ClientUniAppHookProxy.serializeConfig));
                    break;
                case "onUserOnlineEvent":
                    array.add(JSONObject.toJSONString(Util.convertUserOnlineMap((Map<String, UserOnlineState>) args[0]), ClientUniAppHookProxy.serializeConfig));
                    break;
                case "onReceiveMessage":
                    List<Message> messages = (List<Message>) args[0];
                    boolean hasMore = (boolean) args[1];
                    List<JSMessage> jsMessages = Util.messagesToJSMessages(messages);
                    array.add(JSONObject.toJSONString(jsMessages, ClientUniAppHookProxy.serializeConfig));
                    array.add(hasMore);
                    break;
                default:
                    for (Object e : args) {
                        if (e instanceof ConversationInfo){
                            array.add(JSONObject.toJSONString(JSConversationInfo.fromConversationInfo((ConversationInfo) e), ClientUniAppHookProxy.serializeConfig));
                        }else {
                            array.add(JSONObject.toJSONString(e, ClientUniAppHookProxy.serializeConfig));
                        }
                    }
                    break;

            }
        }

        if (ClientModule.uniSDKInstance != null && !methodName.equals("onTrafficData")) {
            Log.d(TAG, MessageFormat.format("事件[{0}]:{1}", methodName, array.toJSONString()));
            JSONObject object = new JSONObject();
            object.put("args", array);
            object.put("timestamp", System.currentTimeMillis());
            ClientModule.uniSDKInstance.fireGlobalEventCallback("wfc-event", object);
        }
        return null;
    }
}

