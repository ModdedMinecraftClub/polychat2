package club.moddedminecraft.polychat.client.clientbase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface ClientApiBase {

    /**
     * Send a chat message to online players
     * @param message Message to be sent
     * @param skipPlayers Players to not send message to
     */
    void sendChatMessage(String message, List<UUID> skipPlayers);

    /**
     * Get the max players for the server
     * @return Maximum players that can join the server
     */
    int getMaxPlayers();

    /**
     * Get the currently online players
     * @return List of online players
     */
    ArrayList<String> getOnlinePlayers();

    /**
     * Get the path to the configuration directory
     * @return Path to config directory
     */
    Path getConfigDirectory();

    /**
     * Get the runner for an in-game command
     * @param command
     */
    CommandRunner getRunner(String command);

}
