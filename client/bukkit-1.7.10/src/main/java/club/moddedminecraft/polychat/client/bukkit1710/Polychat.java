package club.moddedminecraft.polychat.client.bukkit1710;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Polychat extends JavaPlugin implements Listener, ClientApiBase {
    private PolychatClient client;
    private Server server;

    @Override
    public void onEnable() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));
        getServer().getScheduler().runTaskTimer(this, this::onTick, 0, 5);

        server = getServer();
        client = new PolychatClient(this);

        getServer().getPluginManager().registerEvents(this, this);

        client.getCallbacks().sendServerStart();
    }

    @Override
    public void onDisable() {
        client.getCallbacks().cleanShutdown();
    }

    public void sendShutdown() {
        client.getCallbacks().sendServerStop();
    }

    public void onTick() {
        client.update();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String formatWithPrefix = client.getFormattedServerId() + " " + event.getFormat();
        event.setFormat(formatWithPrefix);

        String fullMessage = String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage());
        client.getCallbacks().newChatMessage(fullMessage, event.getMessage());
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        client.getCallbacks().playerEvent(event.getPlayer().getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        client.getCallbacks().playerEvent(event.getPlayer().getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pcmute")) { // If the player typed /basic then do the following, note: If you only registered this executor for one command, you don't need this
            Player player = Bukkit.getPlayer(sender.getName());
            UUID uuid = player.getUniqueId();
            if (client.getMuteStorage().checkPlayer(uuid)) {
                client.getMuteStorage().removePlayer(uuid);
                player.sendMessage("§9Unmuted all other servers and Discord.");
            } else {
                client.getMuteStorage().addPlayer(uuid);
                player.sendMessage("§9Muted all other servers and Discord.");
            }
            return true;
        }
        return false;
    }

    @Override
    public void sendChatMessage(String message, List<UUID> uuids) {
        for (Player player : server.getOnlinePlayers()) {
            if (uuids.contains(player.getUniqueId())) {
                continue;
            }
            player.sendMessage(message);
        }
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        Player[] bukkitPlayers = server.getOnlinePlayers();
        players.ensureCapacity(bukkitPlayers.length);
        for (Player player : bukkitPlayers) {
            players.add(player.getName());
        }
        return players;
    }

    @Override
    public Path getConfigDirectory() {
        return getDataFolder().toPath();
    }

}
