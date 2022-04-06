package cn.wildfirechat.uni.client;

import android.app.Activity;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

import java.util.List;

import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnConnectionStatusChangeListener;
import cn.wildfirechat.remote.SendMessageCallback;
import io.dcloud.feature.uniapp.AbsSDKInstance;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * 野火IM Android client扩展uniapp模块
 */
public class ClientModule extends UniModule implements OnConnectionStatusChangeListener {

    private UniJSCallback connectStatusListener;


    private static final String TAG = "WfcClientModule";
    static AbsSDKInstance uniSDKInstance;


    // 不知道为啥，未触发
    @Override
    public void onActivityCreate() {
        super.onActivityCreate();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        // 删除监听器
        Log.i(TAG, "应用销毁后处理");
        // ChatManager chatManager = ChatManager.Instance();
        // chatManager.removeOnReceiveMessageListener(receiveMessageListener);
    }

    // 一定要调这个函数，触发对mUniSDKInstance 的赋值
    @UniJSMethod(uiThread = false)
    public void init() {
        ClientModule.uniSDKInstance = mUniSDKInstance;
    }

    @UniJSMethod
    public void setOnConnectStatusListener(UniJSCallback callback) {
        this.connectStatusListener = callback;
    }

    @UniJSMethod(uiThread = false)
    public void connect(String imServerHost, String userId, String token) {
        if (mUniSDKInstance.getContext() instanceof Activity) {
            ChatManager.Instance().setIMServerHost(imServerHost);
            ChatManager.Instance().connect(userId, token);
        }
    }

    @UniJSMethod
    public void testAsyncFunc(String xxx, UniJSCallback callback) {

    }

    @UniJSMethod(uiThread = true)
    public void sendMessage(String strConv, String strCont, List<String> toUsers, int expireDuration, JSCallback preparedCB, JSCallback progressCB, JSCallback successCB, JSCallback failCB) {
        Log.d(TAG, "sendMessage " + strCont + " " + strCont + " " + toUsers + " " + expireDuration);
        Conversation conversation = JSONObject.parseObject(strConv, Conversation.class);
        MessagePayload messagePayload = JSONObject.parseObject(strCont, MessagePayload.class);
        MessageContent messageContent = ChatManager.Instance().messageContentFromPayload(messagePayload, ChatManager.Instance().getUserId());
        ChatManager.Instance().sendMessage(conversation, messageContent, toUsers.toArray(new String[0]), expireDuration, new SendMessageCallback() {
            @Override
            public void onSuccess(long messageUid, long timestamp) {
                if (successCB != null) {
                    JSONArray array = new JSONArray();
                    array.add(messageUid + "");
                    array.add(timestamp);
                    successCB.invoke(array);
                }
            }

            @Override
            public void onFail(int errorCode) {
                if (failCB != null) {
                    failCB.invoke(errorCode);
                }
            }

            @Override
            public void onPrepare(long messageId, long savedTime) {
                if (preparedCB != null) {
                    JSONArray array = new JSONArray();
                    array.add(messageId);
                    array.add(savedTime);
                    preparedCB.invoke(array);
                }
            }

            @Override
            public void onProgress(long uploaded, long total) {
                if (progressCB != null) {
                    JSONArray array = new JSONArray();
                    array.add(uploaded);
                    array.add(total);
                    progressCB.invokeAndKeepAlive(array);
                }
            }
        });
    }

    // ui 线程的方法异步执行
    // 非 ui 线程的方法同步执行
    @UniJSMethod(uiThread = false)
    public String getClientId() {
        Log.d(TAG, "getClientId " + ChatManager.Instance().getClientId());
        return ChatManager.Instance().getClientId();
    }

    @Override
    public void onConnectionStatusChange(int status) {
        Log.d(TAG, "onConnectionStatusChange " + status + " " + (this.connectStatusListener == null));
        if (this.connectStatusListener != null) {
            this.connectStatusListener.invokeAndKeepAlive(status);
        }
    }
}
