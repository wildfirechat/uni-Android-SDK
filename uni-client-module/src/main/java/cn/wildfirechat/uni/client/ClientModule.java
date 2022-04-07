package cn.wildfirechat.uni.client;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.MessageContentMediaType;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.ChatRoomInfo;
import cn.wildfirechat.model.ChatRoomMembersInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.ConversationSearchResult;
import cn.wildfirechat.model.FileRecord;
import cn.wildfirechat.model.Friend;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.model.ModifyChannelInfoType;
import cn.wildfirechat.model.ModifyGroupInfoType;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.ModifyMyInfoType;
import cn.wildfirechat.model.Socks5ProxyInfo;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.UserOnlineState;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.GetAuthorizedMediaUrlCallback;
import cn.wildfirechat.remote.GetChatRoomInfoCallback;
import cn.wildfirechat.remote.GetChatRoomMembersInfoCallback;
import cn.wildfirechat.remote.GetFileRecordCallback;
import cn.wildfirechat.remote.GetGroupInfoCallback;
import cn.wildfirechat.remote.GetGroupMembersCallback;
import cn.wildfirechat.remote.GetMessageCallback;
import cn.wildfirechat.remote.GetOneRemoteMessageCallback;
import cn.wildfirechat.remote.GetRemoteMessageCallback;
import cn.wildfirechat.remote.GetUploadUrlCallback;
import cn.wildfirechat.remote.GetUserInfoCallback;
import cn.wildfirechat.remote.SearchChannelCallback;
import cn.wildfirechat.remote.SearchUserCallback;
import cn.wildfirechat.remote.SendMessageCallback;
import cn.wildfirechat.remote.UserSettingScope;
import cn.wildfirechat.remote.WatchOnlineStateCallback;
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


    // ui 线程的方法异步执行
    // 非 ui 线程的方法同步执行
    @UniJSMethod(uiThread = false)
    public String getClientId() {
        Log.d(TAG, "getClientId " + ChatManager.Instance().getClientId());
        return ChatManager.Instance().getClientId();
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
        Conversation conversation = parseObject(strConv, Conversation.class);
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

    @UniJSMethod(uiThread = false)
    public String getUserSetting(int scope, String key) {
        return ChatManager.Instance().getUserSetting(scope, key);
    }

    @UniJSMethod(uiThread = false)
    public String getUserSettings(int scope) {
        Map<String, String> settings = ChatManager.Instance().getUserSettings(scope);
        JSONArray array = ClientUniAppHookProxy.strStrMap2Array(settings);
        return JSONObject.toJSONString(array, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void setUserSetting(int scope, String key, String value, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setUserSetting(scope, key, value, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void modifyMyInfo(int type, String value, JSCallback successCB, JSCallback failCB) {
        List<ModifyMyInfoEntry> entries = new ArrayList<>();
        ModifyMyInfoEntry entry = new ModifyMyInfoEntry(ModifyMyInfoType.type(type), value);
        entries.add(entry);
        ChatManager.Instance().modifyMyInfo(entries, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public boolean isGlobalSlient() {
        return ChatManager.Instance().isGlobalSilent();
    }

    @UniJSMethod(uiThread = true)
    public void setGlobalSlient(boolean silent, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setGlobalSilent(silent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public boolean isHiddenNotificationDetail() {
        return ChatManager.Instance().isHiddenNotificationDetail();
    }

    @UniJSMethod(uiThread = true)
    public void setHiddenNotificationDetail(boolean hide, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setHiddenNotificationDetail(hide, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public boolean isHiddenGroupMemberName(String groupId) {
        // TODO
        return false;
    }

    @UniJSMethod(uiThread = false)
    public boolean isUserReceiptEnabled() {
        return ChatManager.Instance().isUserEnableReceipt();
    }

    @UniJSMethod(uiThread = true)
    public void setUserReceiptEnable(boolean enable, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().setUserEnableReceipt(enable, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void joinChatroom(String chatroomId, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().joinChatRoom(chatroomId, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void quitChatroom(String chatroomId, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().quitChatRoom(chatroomId, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void getChatroomInfo(String chatroomId, long updateDt, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().getChatRoomInfo(chatroomId, updateDt, new GetChatRoomInfoCallback() {
            @Override
            public void onSuccess(ChatRoomInfo chatRoomInfo) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(chatRoomInfo, ClientUniAppHookProxy.serializeConfig));
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
    public void getChatroomMemberInfo(String chatroomId, int maxCount, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().getChatRoomMembersInfo(chatroomId, maxCount, new GetChatRoomMembersInfoCallback() {
            @Override
            public void onSuccess(ChatRoomMembersInfo chatRoomMembersInfo) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(chatRoomMembersInfo, ClientUniAppHookProxy.serializeConfig));
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
    public void createChannel(String name, String portrait, int status, String desc, String extra, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().createChannel(null, name, portrait, desc, extra, new JSGeneralCallback2(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getChannelInfo(String channelId, boolean refresh) {
        ChannelInfo channelInfo = ChatManager.Instance().getChannelInfo(channelId, refresh);
        return JSONObject.toJSONString(channelInfo, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void modifyChannelInfo(String channelId, int type, String newValue, JSCallback successCB, JSCallback failCB) {
        ModifyChannelInfoType modifyChannelInfoType = ModifyChannelInfoType.type(type);
        ChatManager.Instance().modifyChannelInfo(channelId, modifyChannelInfoType, newValue, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void searchChannel(String keyword, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().searchChannel(keyword, new SearchChannelCallback() {
            @Override
            public void onSuccess(List<ChannelInfo> channelInfos) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(channelInfos, ClientUniAppHookProxy.serializeConfig));
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
    public boolean isListenedChannel(String channelId) {
        return ChatManager.Instance().isListenedChannel(channelId);
    }

    @UniJSMethod(uiThread = true)
    public void listenChannel(String channelId, boolean listen, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().listenChannel(channelId, listen, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getMyChannels() {
        List<String> channelIds = ChatManager.Instance().getMyChannels();
        return JSONObject.toJSONString(channelIds, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getListenedChannels() {
        List<String> channelIds = ChatManager.Instance().getListenedChannels();
        return JSONObject.toJSONString(channelIds, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = true)
    public void destoryChannel(String channelId, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().destoryChannel(channelId, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getConversationInfos(List<Integer> types, List<Integer> lines) {
        List<Conversation.ConversationType> conversationTypes = new ArrayList<>();
        for (Integer type : types) {
            conversationTypes.add(Conversation.ConversationType.type(type));
        }
        List<ConversationInfo> conversationInfos = ChatManager.Instance().getConversationList(conversationTypes, lines);
        return JSONObject.toJSONString(conversationInfos, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getConversationInfo(String strConv) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ConversationInfo conversationInfo = ChatManager.Instance().getConversation(conversation);
        return JSONObject.toJSONString(conversationInfo, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String searchConversation(String keyword, List<Integer> types, List<Integer> lines) {
        List<Conversation.ConversationType> conversationTypes = new ArrayList<>();
        for (Integer type : types) {
            conversationTypes.add(Conversation.ConversationType.type(type));
        }
        List<ConversationSearchResult> conversationInfos = ChatManager.Instance().searchConversation(keyword, conversationTypes, lines);
        return JSONObject.toJSONString(conversationInfos, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public void removeConversation(String strConv, boolean clearMsg) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().removeConversation(conversation, clearMsg);
    }

    @UniJSMethod(uiThread = true)
    public void setConversationTop(String strConv, boolean top, JSCallback successCB, JSCallback failCB) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().setConversationTop(conversation, top, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void setConversationSlient(String strConv, boolean silent, JSCallback successCB, JSCallback failCB) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().setConversationSilent(conversation, silent, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public void setConversationDraft(String strConv, String draft) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().setConversationDraft(conversation, draft);
    }

    @UniJSMethod(uiThread = false)
    public void setConversationTimestamp(String strConv, String timestamp) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().setConversationTimestamp(conversation, Long.parseLong(timestamp));
    }

    @UniJSMethod(uiThread = false)
    public String getUnreadCount(List<Integer> types, List<Integer> lines) {
        List<Conversation.ConversationType> conversationTypes = new ArrayList<>();
        for (Integer type : types) {
            conversationTypes.add(Conversation.ConversationType.type(type));
        }
        UnreadCount unreadCount = ChatManager.Instance().getUnreadCountEx(conversationTypes, lines);
        return JSONObject.toJSONString(unreadCount, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getConversationUnreadCount(String strConv) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        UnreadCount unreadCount = ChatManager.Instance().getUnreadCount(conversation);
        return JSONObject.toJSONString(unreadCount, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public void clearUnreadStatus(String strConv) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().clearUnreadStatus(conversation);
    }

    @UniJSMethod(uiThread = false)
    public void setLastReceivedMessageUnRead(String strConv, String messageUid, String timestamp) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().markAsUnRead(conversation, false);
    }

    @UniJSMethod(uiThread = false)
    public String getConversationRead(String strConv) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        Map<String, Long> conversationRead = ChatManager.Instance().getConversationRead(conversation);
        return JSONObject.toJSONString(ClientUniAppHookProxy.strLongMap2Array(conversationRead), ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getMessageDelivery(String strConv) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        Map<String, Long> messageDelivery = ChatManager.Instance().getMessageDelivery(conversation);
        return JSONObject.toJSONString(ClientUniAppHookProxy.strLongMap2Array(messageDelivery), ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public void clearAllUnreadStatus() {
        ChatManager.Instance().clearAllUnreadStatus();
    }

    @UniJSMethod(uiThread = false)
    public Long getConversationFirstUnreadMessageId(String strConv) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        return ChatManager.Instance().getFirstUnreadMessageId(conversation);
    }

    @UniJSMethod(uiThread = false)
    public void setMediaMessagePlayed(int messageId) {
        ChatManager.Instance().setMediaMessagePlayed(messageId);
    }

    @UniJSMethod(uiThread = false)
    public void setMessageLocalExtra(int messageId, String extra) {
        ChatManager.Instance().setMessageLocalExtra(messageId, extra);
    }

    @UniJSMethod(uiThread = false)
    public boolean isMyFriend(String userId) {
        return ChatManager.Instance().isMyFriend(userId);
    }

    @UniJSMethod(uiThread = true)
    public void sendFriendRequest(String userId, String reason, String extra, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().sendFriendRequest(userId, reason, extra, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public String getMessages(String strConv, List<Integer> types, int fromIndex, boolean before, int count, String withUser) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        CountDownLatch latch = new CountDownLatch(1);

        List<Message> messageList = new ArrayList<>();
        ChatManager.Instance().getMessages(conversation, types, fromIndex, before, count, withUser, new GetMessageCallback() {
            @Override
            public void onSuccess(List<Message> messages, boolean hasMore) {
                messageList.addAll(messages);
                if (!hasMore) {
                    latch.countDown();
                }
            }

            @Override
            public void onFail(int errorCode) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getMessagesEx(List<Integer> convTypes, List<Integer> lines, List<Integer> contentTypes, int fromIndex, boolean before, int count, String withUser) {
        CountDownLatch latch = new CountDownLatch(1);

        List<Message> messageList = new ArrayList<>();

        List<Conversation.ConversationType> conversationTypes = new ArrayList<>();
        for (Integer type : convTypes) {
            conversationTypes.add(Conversation.ConversationType.type(type));
        }
        ChatManager.Instance().getMessagesEx(conversationTypes, contentTypes, lines, fromIndex, before, count, withUser, new GetMessageCallback() {
            @Override
            public void onSuccess(List<Message> messages, boolean hasMore) {
                messageList.addAll(messages);
                if (!hasMore) {
                    latch.countDown();
                }
            }

            @Override
            public void onFail(int errorCode) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getUserMessages(String userId, String strConv, List<Integer> types, int fromIndex, boolean before, int count) {
        Conversation conversation = parseObject(strConv, Conversation.class);

        CountDownLatch latch = new CountDownLatch(1);

        List<Message> messageList = new ArrayList<>();
        ChatManager.Instance().getUserMessages(userId, conversation, fromIndex, before, count, new GetMessageCallback() {
            @Override
            public void onSuccess(List<Message> messages, boolean hasMore) {
                messageList.addAll(messages);
                if (!hasMore) {
                    latch.countDown();
                }
            }

            @Override
            public void onFail(int errorCode) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getUserMessagesEx(String userId, List<Integer> convTypes, List<Integer> lines, List<Integer> types, int fromIndex, boolean before, int count) {
        List<Conversation.ConversationType> conversationTypes = new ArrayList<>();
        for (Integer type : convTypes) {
            conversationTypes.add(Conversation.ConversationType.type(type));
        }

        CountDownLatch latch = new CountDownLatch(1);
        List<Message> messageList = new ArrayList<>();
        ChatManager.Instance().getUserMessagesEx(userId, conversationTypes, lines, types, fromIndex, before, count, new GetMessageCallback() {
            @Override
            public void onSuccess(List<Message> messages, boolean hasMore) {
                messageList.addAll(messages);
                if (!hasMore) {
                    latch.countDown();
                }
            }

            @Override
            public void onFail(int errorCode) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getMessage(long messageId) {
        Message message = ChatManager.Instance().getMessage(messageId);
        return JSONObject.toJSONString(message, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String getMessageByUid(String messageUid) {
        Message message = ChatManager.Instance().getMessageByUid(Long.parseLong(messageUid));
        return JSONObject.toJSONString(message, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String searchMessage(String strConv, String keyword) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        List<Message> messageList = ChatManager.Instance().searchMessage(conversation, keyword, false, 500, 0);
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String searchMessageEx(String strConv, String keyword, boolean desc, int limit, int offset) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        List<Message> messageList = ChatManager.Instance().searchMessage(conversation, keyword, desc, limit, offset);
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String searchMessageByTypes(String strConv, String keyword, List<Integer> contentTypes, boolean desc, int limit, int offset) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        List<Message> messageList = ChatManager.Instance().searchMessageByTypes(conversation, keyword, contentTypes, desc, limit, offset);
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    @UniJSMethod(uiThread = false)
    public String searchMessageByTypesAndTimes(String strConv, String keyword, List<Integer> contentTypes, long startTime, long endTime, boolean desc, int limit, int offset) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        List<Message> messageList = ChatManager.Instance().searchMessageByTypesAndTimes(conversation, keyword, contentTypes, startTime, endTime, desc, limit, offset);
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
    }

    public String searchMessageEx2(List<Integer> convTypes, List<Integer> lines, List<Integer> contentTypes, String keyword, int fromIndex, boolean desc, int count) {

        List<Conversation.ConversationType> conversationTypes = new ArrayList<>();
        for (Integer type : convTypes) {
            conversationTypes.add(Conversation.ConversationType.type(type));
        }
        CountDownLatch latch = new CountDownLatch(1);
        List<Message> messageList = new ArrayList<>();
        ChatManager.Instance().searchMessagesEx(conversationTypes, lines, contentTypes, keyword, fromIndex, desc, count, new GetMessageCallback() {
            @Override
            public void onSuccess(List<Message> messages, boolean hasMore) {
                messageList.addAll(messages);
                if (!hasMore) {
                    latch.countDown();
                }
            }

            @Override
            public void onFail(int errorCode) {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return JSONObject.toJSONString(messageList, ClientUniAppHookProxy.serializeConfig);
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

    @UniJSMethod(uiThread = true)
    public void recall(String messageUid, JSCallback successCB, JSCallback failCB) {
        Message message = ChatManager.Instance().getMessageByUid(Long.parseLong(messageUid));
        if (message == null) {
            message = new Message();
            message.messageUid = Long.parseLong(messageUid);
        }
        ChatManager.Instance().recallMessage(message, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void deleteRemoteMessage(String messageUid, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().deleteRemoteMessage(Long.parseLong(messageUid), new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void updateRemoteMessageContent(String messageUid, String messagePayload, boolean distribute, boolean updateLocal, JSCallback successCB, JSCallback failCB) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayload);
        ChatManager.Instance().updateRemoteMessageContent(Long.parseLong(messageUid), messageContent, distribute, updateLocal, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public void deleteMessage(long messageId) {
        Message message = ChatManager.Instance().getMessage(messageId);
        if (message == null) {
            message = new Message();
            message.messageId = messageId;
        }
        ChatManager.Instance().deleteMessage(message);
    }

    @UniJSMethod(uiThread = true)
    public void watchOnlineState(int convType, List<String> targets, int duration, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().watchOnlineState(convType, targets.toArray(new String[0]), duration, new WatchOnlineStateCallback() {
            @Override
            public void onSuccess(UserOnlineState[] userOnlineStates) {
                if (successCB != null) {
                    successCB.invoke(JSONObject.toJSONString(userOnlineStates, ClientUniAppHookProxy.serializeConfig));
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
    public void unwatchOnlineState(int convType, List<String> targets, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().unWatchOnlineState(convType, targets.toArray(new String[0]), new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public boolean isCommercialServer() {
        return ChatManager.Instance().isCommercialServer();
    }

    @UniJSMethod(uiThread = false)
    public boolean isReceiptEnabled() {
        return ChatManager.Instance().isReceiptEnabled();
    }

    @UniJSMethod(uiThread = false)
    public boolean isGlobalDisableSyncDraft() {
        return ChatManager.Instance().isGlobalDisableSyncDraft();
    }

    @UniJSMethod(uiThread = false)
    public boolean isEnableUserOnlineState() {
        // fixme
        return true;
    }

    @UniJSMethod(uiThread = true)
    public void getAuthorizedMediaUrl(String messageUid, int mediaType, String mediaPath, JSCallback successCB, JSCallback failCB) {
        MessageContentMediaType messageContentMediaType = MessageContentMediaType.mediaType(mediaType);
        ChatManager.Instance().getAuthorizedMediaUrl(Long.parseLong(messageUid), messageContentMediaType, mediaPath, new GetAuthorizedMediaUrlCallback() {
            @Override
            public void onSuccess(String url, String backupUrl) {
                if (successCB != null) {
                    JSONArray array = new JSONArray();
                    array.add(url);
                    array.add(backupUrl);
                    successCB.invoke(array);
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
    public boolean isSupportBigFilesUpload() {
        return ChatManager.Instance().isSupportBigFilesUpload();
    }

    @UniJSMethod(uiThread = true)
    public void getUploadMediaUrl(String fileName, int mediaType, String contentType, JSCallback successCB, JSCallback failCB) {
        MessageContentMediaType messageContentMediaType = MessageContentMediaType.mediaType(mediaType);
        ChatManager.Instance().getUploadUrl(fileName, messageContentMediaType, contentType, new GetUploadUrlCallback() {
            @Override
            public void onSuccess(String uploadUrl, String remoteUrl, String backUploadupUrl, int serverType) {
                if (successCB != null) {
                    JSONArray array = new JSONArray();
                    array.add(uploadUrl);
                    array.add(remoteUrl);
                    array.add(backUploadupUrl);
                    array.add(serverType);
                    successCB.invoke(array);
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
    public void getConversationFiles(String strConv, String fromUser, String beforeUid, int count, JSCallback successCB, JSCallback failCB) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().getConversationFileRecords(conversation, fromUser, Long.parseLong(beforeUid), count, new JSGetFileRecordCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void getMyFiles(String beforeUid, int count, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().getMyFileRecords(Long.parseLong(beforeUid), count, new JSGetFileRecordCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void deleteFileRecord(String messageUid, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().deleteFileRecord(Long.parseLong(messageUid), new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public void clearMessages(String strConv) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().clearMessages(conversation);
    }

    @UniJSMethod(uiThread = true)
    public void clearRemoteConversationMessages(String strConv, JSCallback successCB, JSCallback failCB) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().clearRemoteConversationMessage(conversation, new JSGeneralCallback(successCB, failCB));
    }

    @UniJSMethod(uiThread = false)
    public void clearMessagesByTime(String strConv, long before) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().clearMessages(conversation, before);
    }

    @UniJSMethod(uiThread = false)
    public void insertMessage(String strConv, String sender, String strMessagePayload, int status, boolean notify, long serverTime) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        MessageContent messageContent = messagePayloadStringToMessageContent(strMessagePayload);
        MessageStatus messageStatus = MessageStatus.status(status);
        ChatManager.Instance().insertMessage(conversation, sender, messageContent, messageStatus, notify, serverTime);
    }

    @UniJSMethod(uiThread = false)
    public void updateMessage(long messageId, String messagePayload) {
        MessageContent messageContent = messagePayloadStringToMessageContent(messagePayload);
        ChatManager.Instance().updateMessage(messageId, messageContent);
    }

    @UniJSMethod(uiThread = false)
    public void updateMessageStatus(long messageId, int status) {
        MessageStatus messageStatus = MessageStatus.status(status);
        ChatManager.Instance().updateMessage(messageId, messageStatus);
    }

    @UniJSMethod(uiThread = true)
    public void uploadMedia(String fileName, String data, int mediaType, JSCallback successCB, JSCallback failCB, JSCallback progressCB) {
        byte[] mediaData = Base64.decode(data, Base64.DEFAULT);
        ChatManager.Instance().uploadMedia(fileName, mediaData, mediaType, new JSGeneralCallback2(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void sendConferenceRequest(long sessionId, String roomId, String request, String data, boolean advance, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().sendConferenceRequest(sessionId, roomId, request, data, new JSGeneralCallback2(successCB, failCB));
    }

    @UniJSMethod(uiThread = true)
    public void searchFiles(String keyword, String strConv, String fromUser, String beforeUid, int count, JSCallback successCB, JSCallback failCB) {
        Conversation conversation = parseObject(strConv, Conversation.class);
        ChatManager.Instance().searchFileRecords(keyword, conversation, fromUser, Long.parseLong(beforeUid), count, new JSGetFileRecordCallback(successCB, failCB));

    }

    @UniJSMethod(uiThread = true)
    public void searchMyFiles(String keyword, String beforeMessageUid, int count, JSCallback successCB, JSCallback failCB) {
        ChatManager.Instance().searchMyFileRecords(keyword, Long.parseLong(beforeMessageUid), count, new JSGetFileRecordCallback(successCB, failCB));
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

    private static class JSGetFileRecordCallback implements GetFileRecordCallback {

        private JSCallback successCB = null;
        private JSCallback failCB = null;

        public JSGetFileRecordCallback(JSCallback successCB, JSCallback failCB) {
            this.successCB = successCB;
            this.failCB = failCB;
        }

        @Override
        public void onSuccess(List<FileRecord> records) {
            if (successCB != null) {
                successCB.invoke(JSONObject.toJSONString(records, ClientUniAppHookProxy.serializeConfig));
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
