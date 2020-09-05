package club.moddedminecraft.polychat.client.clientbase;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;

import java.util.ArrayList;

public interface ClientBase {

    void sendChatMessage(String message);

    int getMaxPlayers();

    ArrayList<String> getOnlinePlayers();

}
