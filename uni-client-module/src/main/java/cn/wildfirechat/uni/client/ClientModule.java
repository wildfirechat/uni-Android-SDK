package cn.wildfirechat.uni.client;

import android.app.Activity;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.wildfirechat.remote.ChatManager;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 *  野火IM Android client扩展uniapp模块
 */
public class ClientModule extends UniModule {


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
        ChatManagerHolder.mUniSDKInstance = mUniSDKInstance;
    }


    @UniJSMethod(uiThread = false)
    public void connect(JSONObject jsonObject) {
        if (mUniSDKInstance.getContext() instanceof Activity) {
            // 通讯连接
            String userId = jsonObject.getString("userId");
            String token = jsonObject.getString("token");
            if (null == userId || null == token) {
                Log.e(TAG, "没有野火的 userId 或者 token");
                return;
            }

            ChatManagerHolder.gChatManager.connect(userId, token);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "连接状态: " + String.valueOf(ChatManagerHolder.gChatManager.getConnectionStatus()));
                }
            }, 1000); // 延时1秒
            // 推送设置设备类型和通讯token
            /*ChatManagerHolder.gChatManager
                    .setDeviceToken(token, PushService.getPushServiceType().ordinal());*/
        }
    }

    @UniJSMethod(uiThread = true)
    public String getClientId() {
        return ChatManager.Instance().getClientId();
    }
}
