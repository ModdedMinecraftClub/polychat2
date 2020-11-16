package club.moddedminecraft.polychat.client.forge1122;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.nio.file.Path;
import java.util.*;

@Mod(modid = Polychat.MODID, name = Polychat.NAME, version = Polychat.VERSION)
public class Polychat implements ClientApiBase {
    public static final String MODID = "polychat";
    public static final String NAME = "Forge 1.12.2 implementation of the Polychat client";
    public static final String VERSION = "2.0.0";

    private PolychatClient client;
    private MinecraftServer server;

    public Polychat() {
        MinecraftForge.EVENT_BUS.register(this);
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));
    }

    //Forces the server to allow clients to join without the mod installed on their client
    @NetworkCheckHandler
    public boolean checkClient(Map<String, String> map, Side side) {
        return true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (client != null) {
            client.update();
        }
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        server = event.getServer();
        client = new PolychatClient(this);
        event.registerServerCommand(new MuteCommand(client.getMuteStorage()));
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        client.getCallbacks().sendServerStart();
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        client.getCallbacks().cleanShutdown();
    }

    public void sendShutdown() {
        client.getCallbacks().sendServerStop();
    }

    @SubscribeEvent
    public void recieveChatMessage(ServerChatEvent event) {
        String withPrefix = client.getFormattedServerId() + " " + event.getComponent().getFormattedText();
        event.setComponent(new TextComponentString(withPrefix));
        client.getCallbacks().newChatMessage(withPrefix, event.getMessage());
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        client.getCallbacks().playerEvent(event.player.getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        client.getCallbacks().playerEvent(event.player.getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    @Override
    public void sendChatMessage(String message, List<UUID> uuids) {
        ITextComponent string = new TextComponentString(message);
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (uuids.contains(player.getUniqueID())) {
                continue;
            }
            player.sendMessage(string);
        }
        server.sendMessage(string);
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        String[] playerNames = server.getPlayerList().getOnlinePlayerNames();
        players.ensureCapacity(playerNames.length);
        Collections.addAll(players, playerNames);
        return players;
    }

    @Override
    public Path getConfigDirectory() {
        return Loader.instance().getConfigDir().toPath();
    }

    @Override
    public CommandRunner getRunner(String command) {
        return new Runner(command, server);
    }
}
