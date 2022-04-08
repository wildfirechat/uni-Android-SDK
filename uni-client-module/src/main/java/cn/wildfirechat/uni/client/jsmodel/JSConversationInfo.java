package cn.wildfirechat.uni.client.jsmodel;

import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UnreadCount;

public class JSConversationInfo {
    public Conversation conversation;
    public JSMessage lastMessage;
    public long timestamp;
    public String draft;
    public UnreadCount unreadCount;
    public boolean isTop;
    public boolean isSilent;

    private JSConversationInfo(ConversationInfo info) {
        this.conversation = info.conversation;
        this.lastMessage = JSMessage.fromMessage(info.lastMessage);
        this.timestamp = info.timestamp;
        this.draft = info.draft;
        this.unreadCount = info.unreadCount;
        this.isTop = info.isTop;
        this.isSilent = info.isSilent;
    }

    public static JSConversationInfo fromConversationInfo(ConversationInfo info) {
        if (info == null) {
            return null;
        }
        return new JSConversationInfo(info);
    }

}
