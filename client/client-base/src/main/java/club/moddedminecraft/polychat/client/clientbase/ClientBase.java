package club.moddedminecraft.polychat.client.clientbase;

import java.util.ArrayList;

public interface ClientBase {

    void sendChatMessage(String message);

    int getMaxPlayers();

    ArrayList<String> getOnlinePlayers();

}
