package cn.wildfirechat.uni.client;

import android.app.Activity;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.Friend;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.model.ModifyGroupInfoType;
import cn.wildfirechat.model.Socks5ProxyInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.GetGroupInfoCallback;
import cn.wildfirechat.remote.GetGroupMembersCallback;
import cn.wildfirechat.remote.GetOneRemoteMessageCallback;
import cn.wildfirechat.remote.GetRemoteMessageCallback;
import cn.wildfirechat.remote.GetUserInfoCallback;
import cn.wildfirechat.remote.SearchUserCallback;
import cn.wildfirechat.remote.SendMessageCallback;
import cn.wildfirechat.remote.UserSettingScope;
import io.dcloud.feature.uniapp.AbsSDKInstance;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
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


    private static final String TAG = "WfcClientModule";
    static AbsSDKInstance uniSDKInstance;

    private String userId;
    private String token;

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

    @UniJSMethod(uiThread = false)
    public void connect(String imServerHost, String userId, String token) {
        this.userId = userId;
        this.token = token;
        if (mUniSDKInstance.getContext() instanceof Activity) {
            ChatManager.Instance().setIMServerHost(imServerHost);
            ChatManager.Instance().connect(userId, token);
        }
    }

    @UniJSMethod(uiThread = true)
    public void sendMessage(String strConv, String messagePayloadString, List<String> toUsers, int expireDuration, JSCallback preparedCB, JSCallback progressCB, JSCallback successCB, JSCallback failCB) {
        Log.d(TAG, "sendMessage " + messagePayloadString + " " + messagePayloadString + " " + toUsers + " " + expireDuration);
        Conversation conversation = parseObject(strConv, Conversation.class);
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
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
        return JSONObject.toJSONString(userInfo, ClientUniAppHookProxy.serializeConfig);
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

    @UniJSMethod(uiThread = true)
    public void searchUser(String keyword, int searchType, int page, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().searchUser(keyword, ChatManager.SearchUserType.type(searchType), page, new SearchUserCallback() {
            @Override
            public void onSuccess(List<UserInfo> userInfos) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(userInfos, ClientUniAppHookProxy.serializeConfig));
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

    @UniJSMethod(uiThread = false)
    public String searchFriends(String keyword) {
        List<UserInfo> friends = ChatManager.Instance().searchFriends(keyword);
        return JSONObject.toJSONString(friends, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String searchGroups(String keyword) {
        List<GroupSearchResult> groupSearchResults = ChatManager.Instance().searchGroups(keyword);
        return JSONObject.toJSONString(groupSearchResults, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getIncommingFriendRequest() {
        List<FriendRequest> friendRequests = ChatManager.Instance().getFriendRequest(true);
        return JSONObject.toJSONString(friendRequests, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getOutgoingFriendRequest() {
        List<FriendRequest> friendRequests = ChatManager.Instance().getFriendRequest(false);
        return JSONObject.toJSONString(friendRequests, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getFriendRequest(String userId, boolean incoming) {
        FriendRequest friendRequest = ChatManager.Instance().getFriendRequest(userId, incoming);
        return JSONObject.toJSONString(friendRequest, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void loadFriendRequestFromRemote() {
        ChatManager.Instance().loadFriendRequestFromRemote();
    }

    @UniJSMethod(uiThread = false)
    public String getFavUsers() {
        Map<String, String> userIdMap = ChatManager.Instance().getUserSettings(UserSettingScope.FavoriteUser);
        List<String> userIds = new ArrayList<>();
        if (userIdMap != null && !userIdMap.isEmpty()) {
            for (Map.Entry<String, String> entry : userIdMap.entrySet()) {
                if (entry.getValue().equals("1")) {
                    userIds.add(entry.getKey());
                }
            }
        }
        return JSONObject.toJSONString(userIds, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public boolean isFavUser(String userId) {
        return ChatManager.Instance().isFavUser(userId);
    }

    @UniJSMethod(uiThread = true)
    public void setFavUser(String userId, boolean fav, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setFavUser(userId, fav, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void getRemoteMessages(String strConv, String beforeUid, int count, JSCallback successCB, JSCallback failCB) {
        Conversation conversation = JSONObject.parseObject(strConv, Conversation.class);
        ChatManager.Instance().getRemoteMessages(conversation, null, Long.parseLong(beforeUid), count, new GetRemoteMessageCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(messages, ClientUniAppHookProxy.serializeConfig));
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

    @UniJSMethod(uiThread = true)
    public void getRemoteMessage(String messageUid, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().getRemoteMessage(Long.parseLong(messageUid), new GetOneRemoteMessageCallback() {
            @Override
            public void onSuccess(Message messages) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(messages, ClientUniAppHookProxy.serializeConfig));
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

    @UniJSMethod(uiThread = false)
    public int getUnreadFriendRequestStatus() {
        return ChatManager.Instance().getUnreadFriendRequestStatus();
    }

    @UniJSMethod(uiThread = true)
    public void clearUnreadFriendRequestStatus() {
        ChatManager.Instance().clearUnreadFriendRequestStatus();
    }

    @UniJSMethod(uiThread = true)
    public void deleteFriend(String userId, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().deleteFriend(userId, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void handleFriendRequest(String userId, boolean accept, String extra, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().handleFriendRequest(userId, accept, extra, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public boolean isBlackListed(String userId) {
        return ChatManager.Instance().isBlackListed(userId);
    }

    @UniJSMethod(uiThread = false)
    public String getBlackList() {
        List<String> list = ChatManager.Instance().getBlackList(false);
        return JSONObject.toJSONString(list, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void setBlackList(String userId, boolean block, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setBlackList(userId, block, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getMyFriendList(boolean refresh) {
        List<String> list = ChatManager.Instance().getMyFriendList(refresh);
        return JSONObject.toJSONString(list, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getFriendList(boolean refresh) {
        List<Friend> list = ChatManager.Instance().getFriendList(refresh);
        return JSONObject.toJSONString(list, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getFriendAlias(String userId) {
        return ChatManager.Instance().getFriendAlias(userId);
    }

    @UniJSMethod(uiThread = true)
    public void setFriendAlias(String userId, String alias, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setFriendAlias(userId, alias, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void createGroup(String groupId, int type, String name, String portrait, String groupExtra, List<String> memberIds, String memberExtra, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        GroupInfo.GroupType groupType = GroupInfo.GroupType.type(type);
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().createGroup(groupId, name, portrait, groupType, groupExtra, memberIds, memberExtra, lines, messageContent, new JSGeneralCallback2(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void setGroupManager(String groupId, boolean isSet, List<String> memberIds, List<Integer> lines, String messagePayloadStr, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadStr);
        ChatManager.Instance().setGroupManager(groupId, isSet, memberIds, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void allowGroupMember(String groupId, boolean isSet, List<String> memberIds, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().allowGroupMember(groupId, isSet, memberIds, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void muteGroupMember(String groupId, boolean isSet, List<String> memberIds, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().muteGroupMember(groupId, isSet, memberIds, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getGroupInfo(String groupId, boolean refresh) {
        GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(groupId, refresh);
        return JSONObject.toJSONString(groupInfo, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void getGroupInfoEx(String groupId, boolean refresh, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().getGroupInfo(groupId, refresh, new GetGroupInfoCallback() {
            @Override
            public void onSuccess(GroupInfo groupInfo) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(groupInfo, ClientUniAppHookProxy.serializeConfig));
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

    @UniJSMethod(uiThread = true)
    public void addMembers(String groupId, List<String> memberIds, String extra, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().addGroupMembers(groupId, memberIds, extra, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getGroupMembers(String groupId, boolean refresh) {
        List<GroupMember> groupMembers = ChatManager.Instance().getGroupMembers(groupId, refresh);
        return JSONObject.toJSONString(groupMembers, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getGroupMember(String groupId, String memberId) {
        GroupMember groupMember = ChatManager.Instance().getGroupMember(groupId, memberId);
        return JSONObject.toJSONString(groupMember, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void getGroupMembersEx(String groupId, boolean refresh, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().getGroupMembers(groupId, refresh, new GetGroupMembersCallback() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(groupMembers, ClientUniAppHookProxy.serializeConfig));
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

    @UniJSMethod(uiThread = true)
    public void kickoffMembers(String groupId, List<String> memberIds, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().removeGroupMembers(groupId, memberIds, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void quitGroup(String groupId, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().quitGroup(groupId, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void dismissGroup(String groupId, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().dismissGroup(groupId, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void modifyGroupInfo(String groupId, int type, String newValue, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ModifyGroupInfoType modifyGroupInfoType = ModifyGroupInfoType.type(type);
        ChatManager.Instance().modifyGroupInfo(groupId, modifyGroupInfoType, newValue, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void modifyGroupAlias(String groupId, String alias, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().modifyGroupAlias(groupId, alias, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void modifyGroupMemberAlias(String groupId, String memberId, String alias, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().modifyGroupMemberAlias(groupId, memberId, alias, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void modifyGroupMemberExtra(String groupId, String extra, String alias, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().modifyGroupMemberExtra(groupId, extra, alias, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void transferGroup(String groupId, String newOwner, List<Integer> lines, String messagePayloadString, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayloadString);
        ChatManager.Instance().transferGroup(groupId, newOwner, lines, messageContent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getFavGroups() {
        List<String> favGroupIds = new ArrayList<>();
        Map<String, String> groupIdMap = ChatManager.Instance().getUserSettings(UserSettingScope.FavoriteGroup);
        if (groupIdMap != null && !groupIdMap.isEmpty()) {
            for (Map.Entry<String, String> entry : groupIdMap.entrySet()) {
                if (entry.getValue().equals("1")) {
                    favGroupIds.add(entry.getKey());
                }
            }
        }
        return JSONObject.toJSONString(favGroupIds, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public boolean isFavGroup(String groupId) {
        return ChatManager.Instance().isFavGroup(groupId);
    }

    @UniJSMethod(uiThread = true)
    public void setFavGroup(String groupId, boolean fav, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setFavGroup(groupId, fav, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public String getUserSetting(int scope, String key) {
        return ChatManager.Instance().getUserSetting(scope, key);
    }

    private static class JSGeneralCallback implements GeneralCallback {

        private JSCallback successCB = null;
        private JSCallback failCB = null;

        public JSGeneralCallback(JSCallback successCB, JSCallback failCB) {
            this.successCB = successCB;
            this.failCB = failCB;
        }

        @Override
        public void onSuccess() {
            if (successCB != null) {
                successCB.invoke(null);
            }
        }

        @Override
        public void onFail(int errorCode) {
            if (failCB != null) {
                failCB.invoke(errorCode);
            }
        }
    }

    private static class JSGeneralCallback2 implements GeneralCallback2 {

        private JSCallback successCB = null;
        private JSCallback failCB = null;

        public JSGeneralCallback2(JSCallback successCB, JSCallback failCB) {
            this.successCB = successCB;
            this.failCB = failCB;
        }

        @Override
        public void onSuccess(String result) {
            if (successCB != null) {
                successCB.invoke(result);
            }
        }

        @Override
        public void onFail(int errorCode) {
            if (failCB != null) {
                failCB.invoke(errorCode);
            }
        }
    }

    private MessageContent messagePayloadStringToMessageContent(String text) {
        MessagePayload messagePayload = null;
        try {
            messagePayload = JSONObject.parseObject(text, MessagePayload.class);
        } catch (Exception e) {
            Log.e(TAG, "parseObject to MessagePayload exception " + text);
        }
        if (messagePayload != null) {
            return ChatManager.Instance().messageContentFromPayload(messagePayload, this.userId);
        }
        return null;
    }


    static <T> T parseObject(String text, Class<T> clazz) {
        try {
            return JSONObject.parseObject(text, clazz);
        } catch (Exception e) {
            Log.e(TAG, "parseObject exception " + clazz.getName());
        }
        return null;
    }

}
