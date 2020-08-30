import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;

public interface ClientBase {

    void recieveMessage(ChatProtos.ChatMessage message);

}
