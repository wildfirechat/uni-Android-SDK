package cn.wildfirechat.uni.client.jsmodel;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;

public class JSMessage {
    public long messageId;
    public Conversation conversation;
    public String sender;
    public String[] toUsers;
    public MessagePayload content;
    public MessageDirection direction;
    public MessageStatus status;
    public long messageUid;
    public long serverTime;
    public String localExtra;

    private JSMessage(Message message) {
        this.messageId = message.messageId;
        this.conversation = message.conversation;
        this.sender = message.sender;
        this.toUsers = message.toUsers;
        this.content = message.content.encode();
        this.direction = message.direction;
        this.status = message.status;
        this.messageUid = message.messageUid;
        this.serverTime = message.serverTime;
        this.localExtra = message.localExtra;
    }


    public static JSMessage fromMessage(Message message) {
        if (message == null) {
            return null;
        }
        return new JSMessage(message);
    }
}
