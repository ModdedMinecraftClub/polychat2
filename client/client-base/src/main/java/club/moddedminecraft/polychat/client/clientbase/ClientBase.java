package club.moddedminecraft.polychat.client.clientbase;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;

public interface ClientBase {

    void sendChatMessage(String message);

    int getMaxPlayers();

}
