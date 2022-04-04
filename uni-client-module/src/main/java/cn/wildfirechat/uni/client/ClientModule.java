package cn.wildfirechat.uni.client;

import android.app.Activity;
import android.util.Log;

import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnConnectionStatusChangeListener;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * 野火IM Android client扩展uniapp模块
 */
public class ClientModule extends UniModule implements OnConnectionStatusChangeListener {

    private UniJSCallback connectStatusListener;


    private static final String TAG = "WfcClientModule";

    /**
     * sdk后处理
     */
    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        // 删除监听器
        Log.i(TAG, "应用销毁后处理");
        // ChatManager chatManager = ChatManager.Instance();
        // chatManager.removeOnReceiveMessageListener(receiveMessageListener);
    }

    @UniJSMethod(uiThread = false)
    public void init() {
        Log.d(TAG, "init");
        ChatManagerHolder.mUniSDKInstance = mUniSDKInstance;
        ChatManager.Instance().addConnectionChangeListener(this);
        // TODO
    }


    @UniJSMethod
    public void setOnConnectStatusListener(UniJSCallback callback) {
        this.connectStatusListener = callback;
    }

    @UniJSMethod(uiThread = false)
    public void connect(String imServerHost, String userId, String token) {
        if (mUniSDKInstance.getContext() instanceof Activity) {
            ChatManager.Instance().setIMServerHost(imServerHost);
            ChatManagerHolder.gChatManager.connect(userId, token);
        }
    }

    @UniJSMethod
    public void testAsyncFunc(String xxx, UniJSCallback callback) {

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
