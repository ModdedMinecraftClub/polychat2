package club.moddedminecraft.polychat.client.bukkit1710;

import club.moddedminecraft.polychat.client.clientbase.ClientBase;
import org.bukkit.Server;

public class Bukkit1710Client implements ClientBase {

    private final Server server;

    public Bukkit1710Client(Server server) {
        this.server = server;
    }

    @Override
    public void sendChatMessage(String message) {
        server.broadcastMessage(message);
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }
}
