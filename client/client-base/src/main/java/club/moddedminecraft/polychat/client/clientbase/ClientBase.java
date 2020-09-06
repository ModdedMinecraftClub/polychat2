package club.moddedminecraft.polychat.client.clientbase;

import java.nio.file.Path;
import java.util.ArrayList;

public interface ClientBase {

    void sendChatMessage(String message);

    int getMaxPlayers();

    ArrayList<String> getOnlinePlayers();

    Path getConfigDirectory();

}
