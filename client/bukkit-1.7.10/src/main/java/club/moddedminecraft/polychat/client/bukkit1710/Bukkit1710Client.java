package club.moddedminecraft.polychat.client.bukkit1710;

import club.moddedminecraft.polychat.client.clientbase.ClientBase;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.ArrayList;

public class Bukkit1710Client implements ClientBase {

    private final Polychat plugin;
    private final Server server;

    public Bukkit1710Client(Polychat plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    @Override
    public void sendChatMessage(String message) {
        server.broadcastMessage(message);
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        Player[] bukkitPlayers = server.getOnlinePlayers();
        for (Player player : bukkitPlayers) {
            players.add(player.getName());
        }
        return players;
    }

    @Override
    public Path getConfigDirectory() {
        return plugin.getDataFolder().toPath();
    }
}
