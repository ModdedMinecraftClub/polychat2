package club.moddedminecraft.polychat.client.bukkit1710;

import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Polychat extends JavaPlugin implements Listener {
    private PolychatClient client;

    @Override
    public void onEnable() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));
        getServer().getScheduler().runTaskTimer(this, this::onTick, 0, 1);

        client = new PolychatClient(new Bukkit1710Client(this));

        getServer().getPluginManager().registerEvents(this, this);

        client.sendServerStart();
    }

    @Override
    public void onDisable() {
        client.cleanShutdown();
    }

    public void sendShutdown() {
        client.sendServerStop();
    }

    public void onTick() {
        client.update();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String formatWithPrefix = client.getServerId() + " " + event.getFormat();
        event.setFormat(formatWithPrefix);

        String fullMessage = String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage());
        client.newChatMessage(fullMessage, event.getMessage());
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        client.playerEvent(event.getPlayer().getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
        getDataFolder();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        client.playerEvent(event.getPlayer().getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

}
