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
import cn.wildfirechat.model.Socks5ProxyInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetUserInfoCallback;
import cn.wildfirechat.remote.SendMessageCallback;
import io.dcloud.feature.uniapp.AbsSDKInstance;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 * 野火IM Android client扩展uniapp模块
 * <p>
 * 一些开发说明
 * 1. 同步方法需要添加{@code @UniJSMethod(uiThread = false)} 注解
 * 2. 异步方法需要添加{@code @UniJSMethod(uiThread = false)} 注解
 * 3. 非基本类型的返回值，转换成 String 类型返回
 */
public class ClientModule extends UniModule {

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


    @UniJSMethod(uiThread = false)
    public void setLastReceivedMessageUnRead(String strConv, String messageUid, String timestamp) {
        Conversation conversation = JSONObject.parseObject(strConv, Conversation.class);
        ChatManager.Instance().markAsUnRead(conversation, false);
    }

    @UniJSMethod(uiThread = false)
    public void registerMessageFlag(int type, int flag) {
        // TODO
    }

    @UniJSMethod(uiThread = false)
    public void useSM4() {
        ChatManager.Instance().useSM4();
    }

    @UniJSMethod(uiThread = false)
    public void setProxyInfo(String host, String ip, int port, String userName, String password) {
        Socks5ProxyInfo proxyInfo = new Socks5ProxyInfo(host, ip, port, userName, password);
        ChatManager.Instance().setProxyInfo(proxyInfo);
    }

    @UniJSMethod(uiThread = false)
    public void disconnect(int code) {
        ChatManager.Instance().disconnect(false, false);
    }

    @UniJSMethod(uiThread = false)
    public long getServerDeltaTime() {
        return ChatManager.Instance().getServerDeltaTime();
    }

    @UniJSMethod(uiThread = false)
    public int getConnectionStatus() {
        return ChatManager.Instance().getConnectionStatus();
    }

    @UniJSMethod(uiThread = false)
    public void setBackupAddressStrategy(int strategy) {
        ChatManager.Instance().setBackupAddressStrategy(strategy);
    }

    @UniJSMethod(uiThread = false)
    public void setBackupAddress(String backupHost, int backPort) {
        ChatManager.Instance().setBackupAddress(backupHost, backPort);
    }

    @UniJSMethod(uiThread = false)
    public void setUserAgent(String userAgent) {
        ChatManager.Instance().setProtoUserAgent(userAgent);
    }

    @UniJSMethod(uiThread = false)
    public void addHttpHeader(String header, String value) {
        ChatManager.Instance().addHttpHeader(header, value);
    }

    @UniJSMethod(uiThread = false)
    public String getUserInfo(String userId, boolean refresh, String groupId) {
        UserInfo userInfo = ChatManager.Instance().getUserInfo(userId, groupId, refresh);
        return JSONObject.toJSONString(userInfo, ClientUniAppHookProxy.serializeConfig)
    }

    @UniJSMethod(uiThread = false)
    public String getUserInfos(List<String> userIds, String groupId) {
        List<UserInfo> userInfos = ChatManager.Instance().getUserInfos(userIds, groupId);
        return JSONObject.toJSONString(userInfos, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void getUserInfoEx(String userId, boolean refresh, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().getUserInfo(userId, refresh, new GetUserInfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(userInfo, ClientUniAppHookProxy.serializeConfig));
                }
            }

            @Override
            public void onFail(int errorCode) {
                if (failCB != null) {
                    failCB.invoke(errorCode);
                }
            }
        });

    }
}
