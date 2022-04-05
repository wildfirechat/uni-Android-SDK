package cn.wildfirechat.uni.client;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.remote.ChatManager;
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
        JSONArray array = new JSONArray();
        array.add(method.getName());
        if (args != null)
            for (Object e : args) {
                array.add(JSONObject.toJSONString(e, ClientUniAppHookProxy.serializeConfig));
            }

        if (ClientModule.uniSDKInstance != null) {
            Log.d(TAG, MessageFormat.format("事件[{0}]:{1}", method.getName(), array.toJSONString()));
            JSONObject object = new JSONObject();
            object.put("args", array);
            object.put("timestamp", System.currentTimeMillis());
            ClientModule.uniSDKInstance.fireGlobalEventCallback("wfc-event", object);
        }
        return null;
    }

}

