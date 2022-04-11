import ConnectionStatus from "./wfc/client/connectionStatus";
import Vue from 'vue'
import wfc from "./wfc/client/wfc";
import EventType from "./wfc/client/wfcEvent";
import ConversationType from "./wfc/model/conversationType";
import {eq, gt, numberValue, stringValue} from "./wfc/util/longUtil";
import helper from "./pages/util/helper";
import convert from './vendor/pinyin'
import GroupType from "./wfc/model/groupType";
// import {imageThumbnail, videoDuration, videoThumbnail} from "./ui/util/imageUtil";
import MessageContentMediaType from "./wfc/messages/messageContentMediaType";
import Conversation from "./wfc/model/conversation";
import MessageContentType from "./wfc/messages/messageContentType";
import Message from "./wfc/messages/message";
import ImageMessageContent from "./wfc/messages/imageMessageContent";
import VideoMessageContent from "./wfc/messages/videoMessageContent";
import FileMessageContent from "./wfc/messages/fileMessageContent";
import ForwardType from "@/pages/conversation/message/forward/ForwardType";
import TextMessageContent from "./wfc/messages/textMessageContent";
import SearchType from "./wfc/model/searchType";
import Config from "./config";
import {getItem, removeItem, setItem} from "./pages/util/storageHelper";
import CompositeMessageContent from "./wfc/messages/compositeMessageContent";
// import {getConversationPortrait} from "./ui/util/imageUtil";
import DismissGroupNotification from "./wfc/messages/notification/dismissGroupNotification";
import KickoffGroupMemberNotification from "./wfc/messages/notification/kickoffGroupMemberNotification";
import QuitGroupNotification from "./wfc/messages/notification/quitGroupNotification";

/**
 * 一些说明
 * _开头的字段，是为了UI层展示方便，而打补丁上出去的
 * __开头的字段，仅内部使用
 * _开头的函数，是内部函数
 * 外部不直接更新字段，而是通过提供各种action方法更新
 */
let store = {
    debug: true,
    state: {
        conversation: {
            _wfc: wfc,
            currentConversationInfo: null,
            conversationInfoList: [],
            currentConversationMessageList: [],
            currentConversationOldestMessageId: 0,
            currentConversationOldestMessageUid: 0,

            currentConversationDeliveries: null,
            currentConversationRead: null,

            // TODO 调用setUserEnableReceipt时，需要更新
            isMessageReceiptEnable: false,

            inputtingUser: null,
            inputClearHandler: null,

            shouldAutoScrollToBottom: true,

            previewMediaItems: [],
            previewMediaIndex: null,

            enableMessageMultiSelection: false,
            quotedMessage: null,

            downloadingMessages: [],

            currentVoiceMessage: null,

            _reset() {
                this.currentConversationInfo = null;
                this.conversationInfoList = []
                this.currentConversationMessageList = [];
                this.currentConversationOldestMessageId = 0;
                this.currentConversationOldestMessageUid = 0;
                this.currentConversationDeliveries = null;
                this.currentConversationRead = null;
                this.isMessageReceiptEnable = false;
                this.inputtingUser = null;
                this.inputClearHandler = null;
                this.shouldAutoScrollToBottom = true;
                this.previewMediaItems = [];
                this.previewMediaIndex = null;
                this.enableMessageMultiSelection = false;
                this.quotedMessage = null;
                this.downloadingMessages = [];
                this.currentVoiceMessage = null;
            }
        },

        contact: {
            currentFriendRequest: null,
            currentGroup: null,
            currentFriend: null,

            expandFriendRequestList: false,
            expandFriendList: true,
            expandGroup: false,

            unreadFriendRequestCount: 0,
            friendList: [],
            friendRequestList: [],
            favGroupList: [],
            favContactList: [],

            selfUserInfo: null,
            _reset() {
                this.currentFriendRequest = null;
                this.currentGroup = null;
                this.currentFriend = null;

                this.expandFriendRequestList = false;
                this.expandFriendList = true;
                this.expandGroup = false;

                this.unreadFriendRequestCount = 0;
                this.friendList = [];
                this.friendRequestList = [];
                this.favGroupList = [];
                this.favContactList = [];

                this.selfUserInfo = null;
            }
        },

        search: {
            query: null,
            show: false,
            userSearchResult: [],
            contactSearchResult: [],
            groupSearchResult: [],
            conversationSearchResult: [],
            messageSearchResult: [],

            _reset() {
                this.query = null;
                this.show = false;
                this.userSearchResult = [];
                this.contactSearchResult = [];
                this.groupSearchResult = [];
                this.conversationSearchResult = [];
                this.messageSearchResult = [];

            }
        },

        pick: {
            users: [],
            conversations: [],
            messages: [],

            _reset() {
                this.users = [];
                this.conversations = [];
                this.messages = [];

            }
        },

        misc: {
            connectionStatus: ConnectionStatus.ConnectionStatusUnconnected,
            enableNotification: true,
            enableMinimize: getItem('minimizable') === '1',
            enableNotificationMessageDetail: true,
            enableCloseWindowToExit: false,
            enableAutoLogin: false,
            uploadBigFiles: [],
            wfc: wfc,
            config: Config,
            userOnlineStateMap: new Map(),

            _reset() {
                this.connectionStatus = ConnectionStatus.ConnectionStatusUnconnected;
                this.enableNotification = true;
                this.enableMinimize = getItem('minimizable') === '1';
                this.enableNotificationMessageDetail = true;
                this.enableCloseWindowToExit = false;
                this.enableAutoLogin = false;
                this.uploadBigFiles = [];
                this.wfc = wfc;
                this.config = Config;
                this.userOnlineStateMap = new Map();
            }
        },
    },

    init() {
        console.log('init store')
        // 目前，通知只可能在主窗口触发
        wfc.eventEmitter.on(EventType.ConnectionStatusChanged, (status) => {
            miscState.connectionStatus = status;
            if (status === ConnectionStatus.ConnectionStatusConnected) {
                this._loadDefaultData();

            } else if (status === ConnectionStatus.ConnectionStatusLogout) {
                _reset();
            } else if (status === ConnectionStatus.ConnectionStatusSecretKeyMismatch
                || status === ConnectionStatus.ConnectionStatusLogout
                || status === ConnectionStatus.ConnectionStatusTokenIncorrect
                || status === ConnectionStatus.ConnectionStatusRejected) {
                removeItem("userId");
                removeItem('token')
                _reset();
            }
        });

        wfc.eventEmitter.on(EventType.UserInfosUpdate, (userInfos) => {
            // TODO optimize
            this._loadDefaultConversationList();
            this._loadCurrentConversationMessages();
            this._loadFriendList();
            this._loadFriendRequest();
            this._loadSelfUserInfo();
            // TODO 其他相关逻辑
        });

        wfc.eventEmitter.on(EventType.SettingUpdate, () => {
            this._loadDefaultConversationList();
            this._loadFavContactList();
            this._loadFavGroupList();
            // 清除远程消息时，WEB SDK会同时触发ConversationInfoUpdate 和 setting更新，但PC SDK不会，只会触发setting更新
        });

        wfc.eventEmitter.on(EventType.FriendRequestUpdate, (newFrs) => {
            this._loadFriendRequest();
        });

        wfc.eventEmitter.on(EventType.FriendListUpdate, (updatedFriendIds) => {
            this._loadFriendList();
            this._loadFriendRequest();
            this._loadFavContactList();
            this._loadDefaultConversationList();
            this._loadCurrentConversationMessages();
        });

        wfc.eventEmitter.on(EventType.GroupInfosUpdate, (groupInfos) => {
            // TODO optimize
            this._loadDefaultConversationList();
            this._loadFavGroupList();
            // TODO 其他相关逻辑

        });

        wfc.eventEmitter.on(EventType.ChannelInfosUpdate, (groupInfos) => {
            this._loadDefaultConversationList();
        });

        wfc.eventEmitter.on(EventType.ConversationInfoUpdate, (conversationInfo) => {
            this._loadDefaultConversationList();
            if (conversationState.currentConversationInfo && conversationState.currentConversationInfo.conversation.equal(conversationInfo.conversation)) {
                this._loadCurrentConversationMessages();
            }
        });

        console.log('store', 'listen receiveMessage')
        wfc.eventEmitter.on(EventType.ReceiveMessage, (msg, hasMore) => {
            console.log('receiveMessage', hasMore);
            if (!hasMore) {
                this._loadDefaultConversationList();
            }
            if (conversationState.currentConversationInfo && msg.conversation.equal(conversationState.currentConversationInfo.conversation)) {
                if (msg.messageContent instanceof DismissGroupNotification
                    || (msg.messageContent instanceof KickoffGroupMemberNotification && msg.messageContent.kickedMembers.indexOf(wfc.getUserId()) >= 0)
                    || (msg.messageContent instanceof QuitGroupNotification && msg.messageContent.operator === wfc.getUserId())
                ) {
                    this.setCurrentConversationInfo(null);
                    return;
                }
                // 移动端，目前只有单聊会发送typing消息
                if (msg.messageContent.type === MessageContentType.Typing) {
                    let groupId = msg.conversation.type === 1 ? msg.conversation.target : '';
                    let userInfo = wfc.getUserInfo(msg.from, groupId)
                    userInfo = Object.assign({}, userInfo);
                    userInfo._displayName = wfc.getGroupMemberDisplayNameEx(userInfo);
                    conversationState.inputtingUser = userInfo;

                    if (!conversationState.inputClearHandler) {
                        conversationState.inputClearHandler = () => {
                            conversationState.inputtingUser = null;
                        }
                    }
                    clearTimeout(conversationState.inputClearHandler);
                    setTimeout(conversationState.inputClearHandler, 3000)
                } else {
                    clearTimeout(conversationState.inputClearHandler);
                    conversationState.inputtingUser = null;
                }

                if (!this._isDisplayMessage(msg) || msg.messageContent.type === MessageContentType.RecallMessage_Notification) {
                    return;
                }
                let msgIndex = conversationState.currentConversationMessageList.findIndex(m => {
                    return m.messageId === msg.messageId;
                });
                if (msgIndex > -1) {
                    console.log('msg duplicate')
                    return;
                }

                // 会把下来加载更多加载的历史消息给清理了
                let lastTimestamp = 0;
                let msgListLength = conversationState.currentConversationMessageList.length;
                if (msgListLength > 0) {
                    lastTimestamp = conversationState.currentConversationMessageList[msgListLength - 1].timestamp;
                }
                this._patchMessage(msg, lastTimestamp);
                conversationState.currentConversationMessageList.push(msg);
                conversationState.currentConversationOldestMessageId = conversationState.currentConversationMessageList[0].messageId;
                conversationState.currentConversationOldestMessageUid = conversationState.currentConversationMessageList[0].messageUid;
            }
        });

        wfc.eventEmitter.on(EventType.RecallMessage, (operator, messageUid) => {
            this._loadDefaultConversationList();
            if (conversationState.currentConversationInfo) {
                let msg = wfc.getMessageByUid(messageUid);
                if (msg && msg.conversation.equal(conversationState.currentConversationInfo.conversation)) {
                    if (conversationState.currentConversationMessageList) {
                        let lastTimestamp = 0;
                        conversationState.currentConversationMessageList = conversationState.currentConversationMessageList.map(msg => {
                            if (eq(msg.messageUid, messageUid)) {
                                let newMsg = wfc.getMessageByUid(messageUid);
                                this._patchMessage(newMsg, lastTimestamp);
                                return newMsg;
                            }
                            lastTimestamp = msg.timestamp;
                            return msg;
                        });
                    }
                }
            }
        });

        wfc.eventEmitter.on(EventType.UserOnlineEvent, (userOnlineStatus) => {
            userOnlineStatus.forEach(e => {
                miscState.userOnlineStateMap.set(e.userId, e);
            })
            this._loadFriendList();
        })
        // 服务端删除
        wfc.eventEmitter.on(EventType.MessageDeleted, (messageUid) => {
            this._loadDefaultConversationList();
            if (conversationState.currentConversationInfo) {

                if (conversationState.currentConversationMessageList) {
                    conversationState.currentConversationMessageList = conversationState.currentConversationMessageList.filter(msg => !eq(msg.messageUid, messageUid))
                }
            }
        });
        // 本地删除
        wfc.eventEmitter.on(EventType.DeleteMessage, (messageId) => {
            this._loadDefaultConversationList();
            if (conversationState.currentConversationInfo) {
                if (conversationState.currentConversationMessageList) {
                    conversationState.currentConversationMessageList = conversationState.currentConversationMessageList.filter(msg => msg.messageId !== messageId)
                }
            }
        });


        wfc.eventEmitter.on(EventType.SendMessage, (message) => {
            this._loadDefaultConversationList();
            if (!this._isDisplayMessage(message)) {
                return;
            }
            if (!conversationState.currentConversationInfo || !message.conversation.equal(conversationState.currentConversationInfo.conversation)) {
                console.log('not current conv')
                return;
            }
            let length = conversationState.currentConversationMessageList.length;
            let lastTimestamp = 0;
            if (length > 0) {
                let lastMessage = conversationState.currentConversationMessageList[length - 1];
                lastTimestamp = lastMessage.timestamp;
            }
            this._patchMessage(message, lastTimestamp)

            conversationState.currentConversationMessageList.push(message);
        });

        wfc.eventEmitter.on(EventType.MessageReceived, (delivery) => {
            if (conversationState.currentConversationInfo) {
                conversationState.currentConversationDeliveries = wfc.getConversationDelivery(conversationState.currentConversationInfo.conversation);
            }
        });

        wfc.eventEmitter.on(EventType.MessageRead, (readEntries) => {
            // optimization
            if (conversationState.currentConversationInfo) {
                // wfc.getConversationRead 每次返回同一个对象，只是该对象的值不一样。
                // 由于 VUE 不能检测到初始化时，不存在的属性的变更，故此处重新 new 一个新的对象，并赋值。
                // FYI:https://vuejs.org/v2/guide/reactivity.html
                conversationState.currentConversationRead = new Map(wfc.getConversationRead(conversationState.currentConversationInfo.conversation));
            }
        });

        if (wfc.getConnectionStatus() === ConnectionStatus.ConnectionStatusConnected){
            this._loadDefaultData();
        }

    },

    _loadDefaultData() {
        this._loadFavGroupList();
        this._loadFriendList();
        this._loadFavContactList();
        this._loadFriendRequest();
        this._loadSelfUserInfo();
        this._loadDefaultConversationList();
        this._loadUserLocalSettings();
        conversationState.isMessageReceiptEnable = wfc.isReceiptEnabled() && wfc.isUserReceiptEnabled();
        if (conversationState.currentConversationInfo) {
            this._loadCurrentConversationMessages();
        }
    },

    // conversation actions

    _isDisplayMessage(message) {
        // return [PersistFlag.Persist, PersistFlag.Persist_And_Count].indexOf(MessageConfig.getMessageContentPersitFlag(message.messageContent.type)) > -1;
        return message.messageId !== 0;
    },

    _loadDefaultConversationList() {
        this._loadConversationList([0, 1, 3], [0])
    },

    _loadConversationList(conversationType = [0, 1, 3], lines = [0]) {
        let conversationList = wfc.getConversationList(conversationType, lines);
        conversationList.forEach(info => {
            this._patchConversationInfo(info);
            // side affect
            if (conversationState.currentConversationInfo
                && conversationState.currentConversationInfo.conversation.equal(info.conversation)) {
                conversationState.currentConversationInfo = info;
                this._patchCurrentConversationOnlineStatus();
            }
        });
        conversationState.conversationInfoList = conversationList;
    },

    setCurrentConversation(conversation) {
        if (!conversation) {
            return;
        }
        if (conversationState.currentConversationInfo && conversation.equal(conversationState.currentConversationInfo.conversation)) {
            return;
        }
        let convs = conversationState.conversationInfoList.filter(info => info.conversation.equal(conversation));
        let info;
        if (convs && convs.length > 0) {
            info = convs[0];
        } else {
            wfc.setConversationTimestamp(conversation, new Date().getTime());
            info = wfc.getConversationInfo(conversation);
            this._patchConversationInfo(info);
            this._loadDefaultConversationList();
        }
        this.setCurrentConversationInfo(info);
    },

    setCurrentConversationInfo(conversationInfo) {
        if (!conversationInfo) {
            if (conversationState.currentConversationInfo) {
                let conversation = conversationState.currentConversationInfo.conversation;
                wfc.unwatchOnlineState(conversation.type, [conversation.target]);
            }
            conversationState.currentConversationInfo = null;
            conversationState.shouldAutoScrollToBottom = false;
            conversationState.currentConversationMessageList.length = 0;
            conversationState.currentConversationOldestMessageId = 0;
            conversationState.currentConversationOldestMessageUid = 0;
            conversationState.currentConversationDeliveries = null;
            conversationState.currentConversationRead = null;
            conversationState.enableMessageMultiSelection = false;
            return;
        }

        if (conversationState.currentConversationInfo && conversationState.currentConversationInfo.conversation.equal(conversationInfo.conversation)) {
            return;
        }

        conversationState.currentConversationInfo = conversationInfo;
        let conversation = conversationInfo.conversation;
        if (conversation.type === ConversationType.Group || (conversation.type === ConversationType.Single && !wfc.isMyFriend(conversation.target))) {
            wfc.watchOnlineState(conversation.type, [conversation.target], 1000, (states) => {
                states.forEach((e => {
                    miscState.userOnlineStateMap.set(e.userId, e);
                }))
                this._patchCurrentConversationOnlineStatus();

            }, (err) => {
                console.log('watchOnlineState error', err);
            })
        } else {
            this._patchCurrentConversationOnlineStatus();
        }
        conversationState.shouldAutoScrollToBottom = true;
        conversationState.currentConversationMessageList.length = 0;
        conversationState.currentConversationOldestMessageId = 0;
        conversationState.currentConversationOldestMessageUid = 0;
        this._loadCurrentConversationMessages();

        conversationState.currentConversationDeliveries = wfc.getConversationDelivery(conversationInfo.conversation);
        conversationState.currentConversationRead = wfc.getConversationRead(conversationInfo.conversation);

        conversationState.enableMessageMultiSelection = false;
        conversationState.quotedMessage = null;
        conversationState.currentVoiceMessage = null;

        clearTimeout(conversationState.inputClearHandler);
        conversationState.inputtingUser = null;

        pickState.messages.length = 0;
    },

    quitGroup(groupId) {
        wfc.quitGroup(groupId, [0], null, () => {
            this.setCurrentConversationInfo(null)
        }, (err) => {
            console.log('quit group error', err)
        })
    },

    toggleMessageMultiSelection(message) {
        conversationState.enableMessageMultiSelection = !conversationState.enableMessageMultiSelection;
        pickState.messages.length = 0;
        if (conversationState.enableMessageMultiSelection && message) {
            pickState.messages.push(message);
        }
    },

    selectOrDeselectMessage(message) {
        let index = pickState.messages.findIndex(m => m.messageId === message.messageId);
        if (index >= 0) {
            pickState.messages.splice(index, 1);
        } else {
            pickState.messages.push(message);
        }
    },

    deleteSelectedMessages() {
        conversationState.enableMessageMultiSelection = false;
        if (pickState.messages.length < 1) {
            return;
        }
        pickState.messages.sort((m1, m2) => m1.messageId - m2.messageId);
        pickState.messages.forEach(m => {
            wfc.deleteMessage(m.messageId);
        });
        pickState.messages.length = 0;
    },

    forwardMessage(forwardType, targetConversations, messages, extraMessageText) {
        targetConversations.forEach(conversation => {
            // let msg =new Message(conversation, message.messageContent)
            // wfc.sendMessage(msg)
            // 或者下面这种
            if (forwardType === ForwardType.NORMAL || forwardType === ForwardType.ONE_BY_ONE) {
                messages.forEach(message => {
                    wfc.sendConversationMessage(conversation, message.messageContent);
                });
            } else {
                // 合并转发
                let compositeMessageContent = new CompositeMessageContent();
                let title = '';
                let msgConversation = messages[0].conversation;
                if (msgConversation.type === ConversationType.Single) {
                    let users = store.getUserInfos([wfc.getUserId(), msgConversation.target], '');
                    title = users[0]._displayName + '和' + users[1]._displayName + '的聊天记录';
                } else {
                    title = '群的聊天记录';
                }
                compositeMessageContent.title = title;
                compositeMessageContent.messages = messages;

                wfc.sendConversationMessage(conversation, compositeMessageContent);
            }

            if (extraMessageText) {
                let textMessage = new TextMessageContent(extraMessageText)
                wfc.sendConversationMessage(conversation, textMessage);
            }
        });
    },

    forwardByCreateConversation(forwardType, users, messages, extraMessageText) {
        this.createConversation(users,
            (conversation) => {
                this.forwardMessage(forwardType, [conversation], messages, extraMessageText)
            },
            (err) => {
                console.error('createConversation error', err)
            })
    },

    setShouldAutoScrollToBottom(scroll) {
        conversationState.shouldAutoScrollToBottom = scroll;
    },

    /**
     *
     * @param src {String} 媒体url
     * @param thumbUrl {String} 缩略图url
     * @param thumb {String} base64格式的缩略图，但不包含'data:image/png;base64,'
     * @param autoplay
     */
    previewMedia(src, thumbUrl, thumb, autoplay = true) {
        conversationState.previewMediaItems.length = 0;
        conversationState.previewMediaItems.push({
            src: src,
            thumb: thumbUrl ? thumbUrl : 'data:image/png;base64,' + thumb,
            autoplay: autoplay,
        });
        conversationState.previewMediaIndex = 0;
        console.log('preview media', conversationState.previewMediaItems, conversationState.previewMediaIndex)
    },
    previewMedias(mediaItems, index) {
        conversationState.previewMediaItems.length = 0;
        conversationState.previewMediaItems.push(...mediaItems);
        conversationState.previewMediaIndex = index;
        console.log('preview medias', conversationState.previewMediaItems, conversationState.previewMediaIndex)
    },

    playVoice(message) {
        if (conversationState.currentVoiceMessage) {
            conversationState.currentVoiceMessage._isPlaying = false;
        }
        conversationState.currentVoiceMessage = message;
    },

    /**
     *
     * @param message
     * @param {Boolean} continuous  true，预览周围的媒体消息；false，只预览第一个参数传入的那条媒体消息
     */
    previewMessage(message, continuous) {
        conversationState.previewMediaItems.length = 0;
        conversationState.previewMediaIndex = 0;
        if (continuous) {
            let mediaMsgs = conversationState.currentConversationMessageList.filter(m => [MessageContentType.Image, MessageContentType.Video].indexOf(m.messageContent.type) > -1)
            let msg;
            for (let i = 0; i < mediaMsgs.length; i++) {
                msg = mediaMsgs[i];
                if (msg.messageId === message.messageId) {
                    conversationState.previewMediaIndex = i;
                }
                conversationState.previewMediaItems.push({
                    src: msg.messageContent.remotePath,
                    thumb: 'data:image/png;base64,' + msg.messageContent.thumbnail,
                    autoplay: true,
                });
            }
        } else {
            conversationState.previewMediaIndex = 0;
            conversationState.previewMediaItems.push({
                src: message.messageContent.remotePath,
                thumb: 'data:image/png;base64,' + message.messageContent.thumbnail,
                autoplay: true,
            });
        }
    },

    previewCompositeMessage(compositeMessage, focusMessageUid) {
        conversationState.previewMediaItems.length = 0;
        conversationState.previewMediaIndex = 0;

        let mediaMsgs = compositeMessage.messageContent.messages.filter(m => [MessageContentType.Image, MessageContentType.Video].indexOf(m.messageContent.type) > -1)
        let msg;
        for (let i = 0; i < mediaMsgs.length; i++) {
            msg = mediaMsgs[i];
            if (eq(msg.messageUid, focusMessageUid)) {
                conversationState.previewMediaIndex = i;
            }
            conversationState.previewMediaItems.push({
                src: msg.messageContent.remotePath,
                thumb: 'data:image/png;base64,' + msg.messageContent.thumbnail,
                autoplay: true,
            });
        }
    },

    cancelUploadBigFile(remoteUrl) {
        miscState.uploadBigFiles.forEach(upload => {
            if (upload.remoteUrl === remoteUrl) {
                let xhr = upload.xhr;
                upload.status = 3;
                upload.xhr = null;
                xhr && xhr.abort();
            }
        })
    },

    _uploadXMLHttpRequest(fileName, remoteUrl, progressCB, successCB, failCB) {
        let xhr = new XMLHttpRequest();
        xhr.upload.onprogress = (e) => {
            console.log('upload.onprogress', Math.ceil(e.loaded / e.total * 100))
            let progress = e.loaded;
            let total = e.total;
            miscState.uploadBigFiles.forEach(upload => {
                if (upload.remoteUrl === remoteUrl) {
                    upload.progress = Math.ceil(progress / total * 100)
                }
            })
            progressCB && progressCB(progress, total);
        }
        xhr.onreadystatechange = (e) => {
            console.log('onr', xhr.readyState, xhr.status, e)
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    miscState.uploadBigFiles.forEach(upload => {
                        if (upload.remoteUrl === remoteUrl) {
                            upload.progress = 100;
                            upload.status = 2;
                            upload.xhr = null;
                        }
                    })
                    console.log('upload file success', fileName, remoteUrl);
                    successCB && successCB(fileName, remoteUrl);
                    return;
                }
                console.log('upload file error', xhr.status);
                miscState.uploadBigFiles.forEach(upload => {
                    if (upload.remoteUrl === remoteUrl) {
                        // status:1 上传中，2 上传成功 3 上传失败
                        upload.status = 3;
                        upload.xhr = null;
                    }
                })
                failCB && failCB(-1);
            }
        }
        xhr.onerror = e => {
            miscState.uploadBigFiles.forEach(upload => {
                if (upload.remoteUrl === remoteUrl) {
                    // status:1 上传中，2 上传成功 3 上传失败
                    upload.status = 3;
                    upload.xhr = null;
                }
            })
            failCB && failCB(e);
        }

        return xhr;
    },

    uploadBigFile(file, mediaType, progressCB, successCB, failCB) {
        wfc.getUploadMediaUrl(file.name, mediaType, `application/octet-stream`, (uploadUrl, remoteUrl, backUploadUrl, serverType) => {
            let xhr;
            if (serverType === 1) {
                // qiniu
                let ss = uploadUrl.split('?');
                let url = ss[0];
                let token = ss[1];
                let key = ss[2];
                xhr = this._uploadXMLHttpRequest(file.name, remoteUrl, progressCB, successCB, failCB);

                let formData = new FormData();
                formData.append('key', key)
                formData.append('token', token)
                formData.append('file', file)
                xhr.open('POST', url);
                xhr.setRequestHeader("content-disposition", `attachment; filename="${encodeURI(file.name)}"`);
                xhr.send(formData);
            } else {
                // 野火专业存储或阿里云
                xhr = this._uploadXMLHttpRequest(file.name, remoteUrl, progressCB, successCB, failCB);
                xhr.open('PUT', uploadUrl);

                xhr.setRequestHeader("content-type", `application/octet-stream`);
                xhr.send(file);
            }

            miscState.uploadBigFiles.push({
                remoteUrl: remoteUrl,
                name: file.name,
                size: file.size,
                _sizeStr: helper.humanSize(file.size),
                _fileIconName: helper.getFiletypeIcon(file.name.substring(file.name.lastIndexOf('.'))),
                status: 1,
                progress: 0,
                xhr: xhr,
            });
        }, (e) => {
            console.log('getUploadMediaUrl e', e)
        })
    },

    sendBigFile(conversation, file) {
        console.log('upload and then send big file')
        this.uploadBigFile(file, 4)
    },

    /**
     *
     * @param conversation
     * @param {File | string} file html File 类型或者url，绝对路径只在electron里面生效
     * @return {Promise<boolean>}
     */
    async sendFile(conversation, file) {
        console.log('send file', file)
        if (file.size && file.size > 100 * 1024 * 1024) {
            if (wfc.isSupportBigFilesUpload()) {
                this.sendBigFile(conversation, file);
            } else {
                console.log('file too big, and not support upload big file')
            }
            return true;
        }

        let fileOrLocalPath = null;
        let remotePath = null;
        if (typeof file === 'string') {
            if (!file.startsWith('http')) {
                fileOrLocalPath = file;
            } else {
                remotePath = file;
            }

            file = {
                path: file,
                name: file.substring((file.lastIndexOf('/') + 1))
            }
        } else {
            fileOrLocalPath = file;
        }
        let msg = new Message();
        msg.conversation = conversation;

        let mediaType = helper.getMediaType(file.name.split('.').slice(-1).pop());
        // todo other file type
        let messageContentmediaType = {
            'pic': MessageContentMediaType.Image,
            'video': MessageContentMediaType.Video,
            'doc': MessageContentMediaType.File,
        }[mediaType];

        let messageContent;
        switch (messageContentmediaType) {
            case MessageContentMediaType.Image:
                let iThumbnail = await imageThumbnail(file);
                if (iThumbnail === null) {
                    return false;
                }
                // let img64 = self.imgDataUriToBase64(imageThumbnail);
                messageContent = new ImageMessageContent(fileOrLocalPath, remotePath, iThumbnail.split(',')[1]);
                break;
            case MessageContentMediaType.Video:
                let vThumbnail = await videoThumbnail(file);
                let duration = await videoDuration(file)
                duration = Math.ceil(duration * 1000);
                if (vThumbnail === null) {
                    return false;
                }
                // let video64 = self.imgDataUriToBase64(videoThumbnail);
                messageContent = new VideoMessageContent(fileOrLocalPath, remotePath, vThumbnail.split(',')[1]);
                break;
            case MessageContentMediaType.File:
                messageContent = new FileMessageContent(fileOrLocalPath, remotePath);
                break;
            default:
                return false;
        }
        msg.messageContent = messageContent;
        wfc.sendMessage(msg,
            (messageId) => {
                console.log('sf, p', messageId)

            },
            (progress, total) => {
                // console.log('sf pp', progress, total)
            },
            (messageUid) => {
                console.log('sf s', messageUid)

            },
            (error) => {
                console.log('sf e', error)

            }
        );
    },

    quoteMessage(message) {
        conversationState.quotedMessage = message;
    },

    getConversationInfo(conversation) {
        let info = wfc.getConversationInfo(conversation);
        return this._patchConversationInfo(info, false);
    },

    /**
     * 获取会话消息
     * @param {Conversation} conversation 会话
     * @param {number} fromIndex 其实消息的 messageId
     * @param {boolean} before 获取其实消息之前，还是之后的消息
     * @param {string} withUser 过滤该用户发送或接收的消息
     * @param {function (Message[]) } callback 消息列表会回调
     */
    getMessages(conversation, fromIndex = 0, before = true, withUser = '', callback) {
        let lmsgs = wfc.getMessages(conversation, fromIndex, before, 20);
        if (lmsgs.length > 0) {
            lmsgs = lmsgs.map(m => this._patchMessage(m, 0));
            setTimeout(() => callback && callback(lmsgs), 200)
        } else {
            callback && callback([]);
            // 只获取本地的消息
            // wfc.loadRemoteConversationMessages(conversation, fromUid, 20,
            //     (msgs) => {
            //         callback(msgs.map(m => this._patchMessage(m, 0)))
            //     },
            //     (error) => {
            //         callback([])
            //     });
        }
    },

    _loadCurrentConversationMessages() {
        if (!conversationState.currentConversationInfo) {
            return;
        }
        let conversation = conversationState.currentConversationInfo.conversation;
        let msgs = wfc.getMessages(conversation, 0, true, 20);
        let lastTimestamp = 0;
        msgs.forEach(m => {
            this._patchMessage(m, lastTimestamp);
            lastTimestamp = m.timestamp;
        });
        conversationState.currentConversationMessageList = msgs;
        if (msgs.length) {
            conversationState.currentConversationOldestMessageId = msgs[0].messageId;
            conversationState.currentConversationOldestMessageUid = msgs[0].messageUid;
        }
    },

    _onloadConversationMessages(conversation, messages) {
        let loadNewMsg = false;
        if (conversation.equal(conversationState.currentConversationInfo.conversation)) {
            let lastTimestamp = 0;
            let newMsgs = [];
            conversationState.currentConversationOldestMessageUid = messages[0].messageUid;
            messages.forEach(m => {
                let index = conversationState.currentConversationMessageList.findIndex(cm => eq(cm.messageUid, m.messageUid))
                if (index === -1) {
                    this._patchMessage(m, lastTimestamp);
                    lastTimestamp = m.timestamp;
                    newMsgs.push(m);
                    loadNewMsg = true;
                }
            });
            conversationState.currentConversationMessageList = newMsgs.concat(conversationState.currentConversationMessageList);
            if (newMsgs.length) {
                conversationState.currentConversationOldestMessageId = newMsgs[0].messageId;
            }
        }
        return loadNewMsg;
    },

    loadConversationHistoryMessages(loadedCB, completeCB) {
        if (!conversationState.currentConversationInfo) {
            return;
        }
        let conversation = conversationState.currentConversationInfo.conversation;
        console.log('loadConversationHistoryMessage', conversation, conversationState.currentConversationOldestMessageId, stringValue(conversationState.currentConversationOldestMessageUid));
        let lmsgs = wfc.getMessages(conversation, conversationState.currentConversationOldestMessageId, true, 20);
        if (lmsgs.length > 0) {
            let loadNewMsg = this._onloadConversationMessages(conversation, lmsgs)
            if (!loadNewMsg) {
                setTimeout(() => completeCB(), 200)
            } else {
                setTimeout(() => loadedCB(), 200)
            }
        } else {
            wfc.loadRemoteConversationMessages(conversation, [], conversationState.currentConversationOldestMessageUid, 20,
                (msgs) => {
                    if (msgs.length === 0) {
                        completeCB();
                    } else {
                        msgs = msgs.filter(m => m.messageId !== 0);
                        this._onloadConversationMessages(conversation, msgs);
                        this._loadDefaultConversationList();
                        loadedCB();
                    }
                },
                (error) => {
                    completeCB();
                });
        }
    },

    setConversationTop(conversation, top) {
        wfc.setConversationTop(conversation, top,
            () => {
                this._loadDefaultConversationList();
            },
            (err) => {
                console.log('setConversationTop error', err)
            });
    },

    setConversationSilent(conversation, silent) {
        wfc.setConversationSlient(conversation, silent,
            () => {
                this._loadDefaultConversationList();
            },
            (err) => {
                console.log('setConversationSilent error', err)
            });
    },

    removeConversation(conversation) {
        wfc.removeConversation(conversation, false);
        if (conversationState.currentConversationInfo && conversationState.currentConversationInfo.conversation.equal(conversation)) {
            this.setCurrentConversationInfo(null);
        }
        this._loadDefaultConversationList();
    },

    getMessageById(messageId) {
        let msg = wfc.getMessageById(messageId);
        if (msg) {
            this._patchMessage(msg, 0);
        }
        return msg;
    },

    getMessageByUid(messageUid) {
        let msg = wfc.getMessageByUid(messageUid);
        if (msg) {
            this._patchMessage(msg, 0);
        }
        return msg;
    },

    _patchMessage(m, lastTimestamp = 0) {
        // TODO
        // _from
        // _showTime
        m._from = wfc.getUserInfo(m.from, false, m.conversation.type === ConversationType.Group ? m.conversation.target : '');
        if (m.conversation.type === ConversationType.Group) {
            m._from._displayName = wfc.getGroupMemberDisplayNameEx(m._from);
        } else {
            m._from._displayName = wfc.getUserDisplayNameEx(m._from);
        }
        if (numberValue(lastTimestamp) > 0 && numberValue(m.timestamp) - numberValue(lastTimestamp) > 5 * 60 * 1000) {
            m._showTime = true;
        }
        m._timeStr = helper.timeFormat(m.timestamp)

        if (m.messageContent instanceof CompositeMessageContent) {
            this._patchCompositeMessageContent(m.messageContent);
        }

        // TODO 如果Im server支持备选网络，需要根据当前的网络情况，判断当前是处于主网络，还是备选网络，并动态修改媒体类消息的remotePath，不然可能会出现不能正常加载的情况
        // 如何判断是主网络，还是备选网络，这儿提供一种思路：分别通过主网络和备选网络测试访问im server的/api/version接口
        // 判断是主网络，还是备选网络，一般启动的时候，检测到网络网络变化的时候，在判断一次。
        // if(m.messageContent instanceof MediaMessageContent){
        // TODO 动态修改remotePath
        // }

        return m;
    },

    _patchCompositeMessageContent(compositeMessageContent) {
        let messages = compositeMessageContent.messages;
        messages.forEach(m => {
            this._patchMessage(m, 0)
        })
    },

    _patchConversationInfo(info, patchLastMessage = true) {
        if (info.conversation.type === ConversationType.Single) {
            info.conversation._target = wfc.getUserInfo(info.conversation.target, false);
            info.conversation._target._displayName = wfc.getUserDisplayNameEx(info.conversation._target);
        } else if (info.conversation.type === ConversationType.Group) {
            info.conversation._target = wfc.getGroupInfo(info.conversation.target, false);
            info.conversation._target._isFav = wfc.isFavGroup(info.conversation.target);
            info.conversation._target._displayName = info.conversation._target.name;
        } else if (info.conversation.type === ConversationType.Channel) {
            info.conversation._target = wfc.getChannelInfo(info.conversation.target, false);
            info.conversation._target._displayName = info.conversation._target.name;
        }
        if (!info.conversation._target.portrait) {
            info.conversation._target.portrait = Config.DEFAULT_PORTRAIT_URL;
            // getConversationPortrait(info.conversation).then((portrait => {
            //     info.conversation._target.portrait = portrait;
            // }))
        }
        if (gt(info.timestamp, 0)) {
            info._timeStr = helper.dateFormat(info.timestamp);
        } else {
            info._timeStr = '';
        }

        if (info.lastMessage && info.lastMessage.conversation !== undefined && patchLastMessage) {
            this._patchMessage(info.lastMessage, 0)
        }

        if (info.unreadCount) {
            info._unread = info.unreadCount.unread + info.unreadCount.unreadMention + info.unreadCount.unreadMentionAll;
        }

        return info;
    },

    addDownloadingMessage(messageId) {
        conversationState.downloadingMessages.push({
            messageId: messageId,
            receivedBytes: 0,
            totalBytes: Number.MAX_SAFE_INTEGER,
        });
        console.log('add downloading')
    },

    isDownloadingMessage(messageId) {
        return conversationState.downloadingMessages.findIndex(dm => dm.messageId === messageId) >= 0;
    },

    getDownloadingMessageStatus(messageId) {
        return conversationState.downloadingMessages.find(dm => dm.messageId === messageId);
    },

    // contact actions

    _loadSelfUserInfo() {
        contactState.selfUserInfo = wfc.getUserInfo(wfc.getUserId(), false);
    },

    _loadFriendList() {
        let friends = wfc.getMyFriendList(false);
        let fileHelperIndex = friends.indexOf(Config.FILE_HELPER_ID);
        if (fileHelperIndex < 0) {
            friends.push(Config.FILE_HELPER_ID);
        }
        if (friends && friends.length > 0) {
            let friendList = wfc.getUserInfos(friends, '');
            contactState.friendList = this._patchAndSortUserInfos(friendList, '');
        }
    },

    getUserOnlineState(userId) {
        let userOnlineState = miscState.userOnlineStateMap.get(userId);
        if (userOnlineState) {
            return userOnlineState.desc();
        }
        return '';
    },

    _patchCurrentConversationOnlineStatus() {
        let convInfo = conversationState.currentConversationInfo;
        if (convInfo && convInfo.conversation.type === ConversationType.Single) {
            // 在 将 object 和 ui 绑定之前， 向 object 中新增的属性是 reactive 的，但绑定之后，才新增的属性，不是 reactive 的，
            // 故需要通过下面这种方法，让其成为 reactive 的属性
            // conversationState.currentConversationInfo.conversation._targetOnlineStateDesc = userOnlineStatus.desc();
            Vue.set(conversationState.currentConversationInfo.conversation, '_targetOnlineStateDesc', this.getUserOnlineState(convInfo.conversation.target))
        } else {
            //TODO
        }
    },
    _loadFriendRequest() {
        let requests = wfc.getIncommingFriendRequest()

        requests.sort((a, b) => numberValue(b.timestamp) - numberValue(a.timestamp))
        requests = requests.length >= 20 ? requests.slice(0, 20) : requests;
        let uids = [];
        requests.forEach(fr => {
            uids.push(fr.target);
        });
        let userInfos = wfc.getUserInfos(uids, '')
        requests.forEach(fr => {
            let userInfo = userInfos.find((u => u.uid === fr.target));
            fr._target = userInfo;
        });

        contactState.friendRequestList = requests;
        contactState.unreadFriendRequestCount = wfc.getUnreadFriendRequestCount();
    },

    _patchAndSortUserInfos(userInfos, groupId = '', compareFn) {
        userInfos = userInfos.map(u => {
            if (groupId) {
                u._displayName = wfc.getGroupMemberDisplayNameEx(u);
            } else {
                u._displayName = wfc.getUserDisplayNameEx(u);
            }
            u._pinyin = convert(u._displayName, {style: 0}).join('').trim().toLowerCase();
            let firstLetter = u._pinyin[0];
            if (firstLetter >= 'a' && firstLetter <= 'z') {
                u.__sortPinyin = 'a' + u._pinyin;
            } else {
                u.__sortPinyin = 'z' + u._pinyin;
            }
            u._firstLetters = convert(u._displayName, {style: 4}).join('').trim().toLowerCase();
            return u;
        });
        if (compareFn) {
            userInfos = userInfos.sort(compareFn);
        } else {
            userInfos = userInfos.sort((a, b) => a.__sortPinyin.localeCompare(b.__sortPinyin));
        }

        userInfos.forEach(u => {
            let uFirstLetter = u.__sortPinyin[1];
            if (uFirstLetter >= 'a' && uFirstLetter <= 'z') {
                u._category = uFirstLetter;
            } else {
                u._category = '#';
            }
            u._userOnlineStatusDesc = this.getUserOnlineState(u.uid);
        });
        return userInfos;
    },

    _loadFavGroupList() {
        contactState.favGroupList = wfc.getFavGroupList();
    },

    _loadFavContactList() {
        let favUserIds = wfc.getFavUsers();
        if (favUserIds.length > 0) {
            contactState.favContactList = this.getUserInfos(favUserIds, '')
            contactState.favContactList.forEach(u => {
                u._category = '☆ 星标朋友';
            })
        }
    },

    reloadFavGroupList() {
        this._loadFavGroupList();
    },

    setCurrentFriendRequest(friendRequest) {
        contactState.currentFriendRequest = friendRequest;
        contactState.currentFriend = null;
        contactState.currentGroup = null;
    },

    setCurrentFriend(friend) {
        contactState.currentFriendRequest = null;
        contactState.currentFriend = friend;
        contactState.currentGroup = null;
    },

    setCurrentGroup(group) {
        contactState.currentFriendRequest = null;
        contactState.currentFriend = null;
        contactState.currentGroup = group;
    },

    toggleGroupList() {
        contactState.expandGroup = !contactState.expandGroup;
    },

    toggleFriendRequestList() {
        contactState.expandFriendRequestList = !contactState.expandFriendRequestList;
    },

    toggleFriendList() {
        contactState.expandFriendList = !contactState.expandFriendList;
    },

    // search actions
    toggleSearchView(show) {
        console.log('ts', show, searchState.show);
        searchState.show = show
    },

    setSearchQuery(query) {
        searchState.query = query;
        if (query) {
            console.log('search', query)
            searchState.contactSearchResult = this.filterContact(query);
            searchState.groupSearchResult = this.filterGroupConversation(query);
            searchState.conversationSearchResult = this.filterConversation(query);
            // searchState.messageSearchResult = this.searchMessage(query);
            // 默认不搜索新用户
            // this.searchUser(query);

        } else {
            searchState.contactSearchResult = [];
            searchState.conversationSearchResult = [];
            searchState.groupSearchResult = [];
            searchState.messageSearchResult = [];
            searchState.userSearchResult = [];
        }
    },

    searchUser(query) {
        console.log('search user', query)
        wfc.searchUser(query, SearchType.General, 0, ((keyword, userInfos) => {
            console.log('search user result', query, userInfos)
            if (searchState.query === keyword) {
                searchState.userSearchResult = userInfos;
            }
        }), (err) => {
            console.log('search user error', query, err)
            if (searchState.query === query) {
                searchState.userSearchResult = [];
            }
        });
    },

    // TODO 到底是什么匹配了
    filterContact(query) {
        let result = contactState.friendList.filter(u => {
            return u._displayName.indexOf(query) > -1 || u._firstLetters.indexOf(query) > -1 || u._pinyin.indexOf(query) > -1
        });

        console.log('friend searchResult', result)
        return result;
    },

    searchFiles(keyword, beforeMessageUid, successCB, failCB) {
        if (!keyword) {
            return;
        }
        wfc.searchFiles(keyword, null, '', beforeMessageUid, 0, 20,
            (files) => {
                this._patchFileRecords(files);
                successCB && successCB(files);
            },
            (errorCode) => {
                console.log('search file error', errorCode);
                failCB && failCB(errorCode);
            })
    },

    filterUsers(users, filter) {
        if (!users || !filter || !filter.trim()) {
            return users;
        }
        let queryPinyin = convert(filter, {style: 0}).join('').trim().toLowerCase();
        let result = users.filter(u => {
            return u._displayName.indexOf(filter) > -1 || u._displayName.indexOf(queryPinyin) > -1
                || u._pinyin.indexOf(filter) > -1 || u._pinyin.indexOf(queryPinyin) > -1
                || u._firstLetters.indexOf(filter) > -1 || u._firstLetters.indexOf(queryPinyin) > -1
        });
        return result;
    },

    // TODO 匹配类型，是群名称匹配上了，还是群成员的名称匹配上了？
    // 目前只搜索群名称
    filterFavGroup(query) {
        console.log('to search group', contactState.favGroupList)
        let queryPinyin = convert(query, {style: 0}).join('').trim().toLowerCase();
        let result = contactState.favGroupList.filter(g => {
            let groupNamePinyin = convert(g.name, {style: 0}).join('').trim().toLowerCase();
            return g.name.indexOf(query) > -1 || g.name.indexOf(queryPinyin) > -1
                || groupNamePinyin.indexOf(query) > -1 || groupNamePinyin.indexOf(queryPinyin) > -1
        });

        console.log('group searchResult', result)
        return result;
    },

    // TODO
    filterConversation(query) {
        return conversationState.conversationInfoList.filter(info => {
            let displayNamePinyin = convert(info.conversation._target._displayName, {style: 0}).join('').trim().toLowerCase();
            let firstLetters = convert(info.conversation._target._displayName, {style: 4}).join('').trim().toLowerCase();
            return info.conversation._target._displayName.indexOf(query) > -1 || displayNamePinyin.indexOf(query.toLowerCase()) > -1 || firstLetters.indexOf(query) > -1
        })
    },

    filterGroupConversation(query) {
        // query = query.toLowerCase();
        // let groups = conversationState.conversationInfoList.filter(info => info.conversation.type === ConversationType.Group).map(info => info.conversation._target);
        // return groups.filter(groupInfo => {
        //     let namePinyin = convert(groupInfo.name, {style: 0}).join('').trim().toLowerCase();
        //     let firstLetters = convert(groupInfo.name, {style: 4}).join('').trim().toLowerCase();
        //     return groupInfo.name.indexOf(query) > -1 || namePinyin.indexOf(query) > -1 || firstLetters.indexOf(query) > -1
        // })
        let gsr = wfc.searchGroups(query)
        return gsr.map(r => r.groupInfo);
    },

    searchMessage(conversation, query) {
        let msgs = wfc.searchMessage(conversation, query)
        msgs = msgs.reverse();
        return msgs.map(m => this._patchMessage(m, 0));
    },

    searchConversation(query, types = [0, 1, 2], lines = [0, 1, 2]) {
        let results = wfc.searchConversation(query, types, lines);
        return results.map(r => {
            let info = wfc.getConversationInfo(r.conversation);
            r._conversationInfo = this._patchConversationInfo(info, false);
            return r;
        })
    },

    // pick actions
    pickOrUnpickUser(user) {
        let index = pickState.users.findIndex(u => u.uid === user.uid);
        if (index >= 0) {
            pickState.users = pickState.users.filter(u => user.uid !== u.uid)
        } else {
            pickState.users.push(user);
        }
    },

    isUserPicked(user) {
        let index = pickState.users.findIndex(u => u.uid === user.uid);
        return index >= 0;
    },

    pickOrUnpickConversation(conversation) {
        let index = pickState.conversations.findIndex(c => (conversation.target === c.target && conversation.line === c.line && conversation.type === c.type))
        if (index >= 0) {
            pickState.conversations = pickState.conversations.filter(c => !(conversation.target === c.target && conversation.line === c.line && conversation.type === c.type))
        } else {
            pickState.conversations.push(conversation);
        }
    },

    // misc actions
    createConversation(users, successCB, failCB) {
        if (users.length === 1) {
            let conversation = new Conversation(ConversationType.Single, users[0].uid, 0);
            this.setCurrentConversation(conversation);
            successCB && successCB(conversation);
            return;
        }

        let groupName = contactState.selfUserInfo.displayName;
        let groupMemberIds = [];
        let groupMemberPortraits = [contactState.selfUserInfo.portrait];
        for (let i = 0; i < users.length; i++) {
            groupMemberIds.push(users[i].uid)
            if (i <= 3) {
                groupName += '、' + users[i].displayName;
            }
            if (i < 8) {
                groupMemberPortraits.push(users[i].portrait)
            }
        }
        groupName = groupName.substr(0, groupName.length - 1);

        wfc.createGroup(null, GroupType.Restricted, groupName, null, null, groupMemberIds, null, [0], null,
            (groupId) => {
                this._loadDefaultConversationList();
                let conversation = new Conversation(ConversationType.Group, groupId, 0)
                this.setCurrentConversation(conversation);
                successCB && successCB(conversation);
            }, (error) => {
                console.log('create group error', error)
                failCB && failCB(error);
            });
    },

    _loadUserLocalSettings() {
        let userId = wfc.getUserId();
        // 默认允许通知
        let setting = getItem(userId + '-' + 'notification');
        miscState.enableNotification = setting === null || setting === '1'
        setting = getItem(userId + '-' + 'notificationDetail');
        miscState.enableNotificationMessageDetail = setting === null || setting === '1'
        miscState.enableCloseWindowToExit = getItem(userId + '-' + 'closeWindowToExit') === '1'
        miscState.enableAutoLogin = getItem(userId + '-' + 'autoLogin') === '1'
        setting = getItem('minimizable')
        miscState.enableMinimize = setting === null || setting === '1'
    },

    setEnableNotification(enable) {
        miscState.enableNotification = enable;
        setItem(contactState.selfUserInfo.uid + '-' + 'notification', enable ? '1' : '0')
    },

    setEnableMinimize(enable) {
        miscState.enableMinimize = enable;
        setItem('minimizable', enable ? '1' : '0')
        currentWindow.minimizable = enable;
    },

    setEnableNotificationDetail(enable) {
        miscState.enableNotificationMessageDetail = enable;
        setItem(contactState.selfUserInfo.uid + '-' + 'notificationDetail', enable ? '1' : '0')
    },

    setEnableCloseWindowToExit(enable) {
        miscState.enableCloseWindowToExit = enable;
        setItem(contactState.selfUserInfo.uid + '-' + 'closeWindowToExit', enable ? '1' : '0')
        ipcRenderer.send('enable-close-window-to-exit', enable)
    },

    setEnableAutoLogin(enable) {
        miscState.enableAutoLogin = enable;
        setItem(contactState.selfUserInfo.uid + '-' + 'autoLogin', enable ? '1' : '0')
    },

    // clone一下，别影响到好友列表
    getUserInfos(userIds, groupId) {
        let userInfos = wfc.getUserInfos(userIds, groupId);
        let userInfosCloneCopy = userInfos.map(u => Object.assign({}, u));
        return this._patchAndSortUserInfos(userInfosCloneCopy, groupId);
    },

    // clone一下，别影响到好友列表
    getGroupMemberUserInfos(groupId, includeSelf = true, sortByPinyin = false) {

        let memberIds = wfc.getGroupMemberIds(groupId);
        let userInfos = wfc.getUserInfos(memberIds, groupId);
        if (!includeSelf) {
            userInfos = userInfos.filter(u => u.uid !== wfc.getUserId())
        }
        let userInfosCloneCopy = userInfos.map(u => Object.assign({}, u));
        if (sortByPinyin) {
            return this._patchAndSortUserInfos(userInfosCloneCopy, groupId);
        } else {
            let compareFn = (u1, u2) => {
                let index1 = memberIds.findIndex(id => id === u1.uid)
                let index2 = memberIds.findIndex(id => id === u2.uid)
                return index1 - index2;
            }
            return this._patchAndSortUserInfos(userInfosCloneCopy, groupId, compareFn);
        }
    },

    // clone一下，别影响到好友列表
    getConversationMemberUsrInfos(conversation) {
        let userInfos = [];
        if (conversation.type === 0) {
            if (conversation.target !== contactState.selfUserInfo.uid) {
                userInfos.push(wfc.getUserInfo(wfc.getUserId(), false));
            }
            userInfos.push(wfc.getUserInfo(conversation.target, false));
            let userInfosCloneCopy = userInfos.map(u => Object.assign({}, u));
            userInfos = this._patchAndSortUserInfos(userInfosCloneCopy, '');
        } else if (conversation.type === 1) {
            userInfos = this.getGroupMemberUserInfos(conversation.target, true);
        }
        return userInfos;
    },

    getMyFileRecords(beforeUid, count, successCB, failCB) {
        if (!successCB) {
            return;
        }
        wfc.getMyFileRecords(beforeUid, 0, count, fileRecords => {
            this._patchFileRecords(fileRecords)
            successCB(fileRecords);
        }, failCB)
    },

    getConversationFileRecords(conversation, fromUser, beforeMessageUid, count, successCB, failCB) {
        wfc.getConversationFileRecords(conversation, fromUser, beforeMessageUid, 0, count, fileRecords => {
            this._patchFileRecords(fileRecords)
            successCB(fileRecords);
        }, failCB);
    },

    deleteFriend(target) {
        wfc.deleteFriend(target, () => {
            wfc.removeConversation(new Conversation(ConversationType.Single, target, 0), true);
            this._loadDefaultConversationList();
        }, (err) => {
            console.log('deleteFriend error', err);
        });
    },

    _patchFileRecords(fileRecords) {
        fileRecords.forEach(fileRecord => {
            let groupId = fileRecord.conversation.type === 1 ? fileRecord.conversation.target : '';
            if (groupId) {
                fileRecord._userDisplayName = wfc.getGroupMemberDisplayName(groupId, fileRecord.userId);
            } else {
                fileRecord._userDisplayName = wfc.getUserDisplayName(fileRecord.userId);
            }
            let conversationInfo = wfc.getConversationInfo(fileRecord.conversation);
            this._patchConversationInfo(conversationInfo, false);

            if (fileRecord.conversation.type === 0) {
                fileRecord._conversationDisplayName = '与' + conversationInfo.conversation._target._displayName + '的聊天';
            } else {
                fileRecord._conversationDisplayName = conversationInfo.conversation._target._displayName;
            }
            if (fileRecord.name.indexOf(FileMessageContent.FILE_NAME_PREFIX) === 0) {
                fileRecord.name = fileRecord.name.substring(fileRecord.name.indexOf(FileMessageContent.FILE_NAME_PREFIX) + FileMessageContent.FILE_NAME_PREFIX.length);
            }
            fileRecord._timeStr = helper.dateFormat(fileRecord.timestamp);
            fileRecord._sizeStr = helper.humanSize(fileRecord.size)
            fileRecord._fileIconName = helper.getFiletypeIcon(fileRecord.name.substring(fileRecord.name.lastIndexOf('.')))
        });
    },

    clearConversationUnreadStatus(conversation) {
        let info = wfc.getConversationInfo(conversation);
        if (info && (info.unreadCount.unread + info.unreadCount.unreadMention + info.unreadCount.unreadMentionAll) > 0) {
            wfc.clearConversationUnreadStatus(conversation);
        }
    },
}

let conversationState = store.state.conversation;
let contactState = store.state.contact;
let searchState = store.state.search;
let pickState = store.state.pick;
let miscState = store.state.misc;

function _reset() {
    conversationState._reset();
    contactState._reset();
    searchState._reset();
    pickState._reset();
    miscState._reset();
}

export default store
