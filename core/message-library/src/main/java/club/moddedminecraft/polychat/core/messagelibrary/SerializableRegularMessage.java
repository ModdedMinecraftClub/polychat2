package club.moddedminecraft.polychat.core.messagelibrary;

public class SerializableRegularMessage implements ISerializableMessage {
    private int serverId;
    private long messageTime;
    private String messageAuthor;
    private String messageContent;

    public SerializableRegularMessage() {
    }

    public SerializableRegularMessage(int serverId, long messageTime, String messageAuthor, String messageContent) {
        this.serverId = serverId;
        this.messageTime = messageTime;
        this.messageAuthor = messageAuthor;
        this.messageContent = messageContent;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageAuthor() {
        return messageAuthor;
    }

    public void setMessageAuthor(String messageAuthor) {
        this.messageAuthor = messageAuthor;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
