package club.moddedminecraft.polychat.client.forge1102;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Mod(modid = Polychat.MODID, version = Polychat.VERSION)
public class Polychat implements ClientApiBase
{
    public static final String MODID = "polychat";
    public static final String VERSION = "1.0";

    private MinecraftServer server;
    private PolychatClient polychatClient;

    //Forces the server to allow clients to join without the mod installed on their client
    @NetworkCheckHandler
    public boolean checkClient(Map<String, String> map, Side side) {
        return true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if(polychatClient != null){
            polychatClient.update();
        }
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent e){
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));
        MinecraftForge.EVENT_BUS.register(this);
        server = e.getServer();
        polychatClient = new PolychatClient(this);
        e.registerServerCommand(new MuteCommand(polychatClient.getMuteStorage()));
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        polychatClient.getCallbacks().sendServerStart();
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        polychatClient.getCallbacks().cleanShutdown();
    }

    public void sendShutdown() {
        polychatClient.getCallbacks().sendServerStop();
    }

    @SubscribeEvent
    public void receiveChatMessage(ServerChatEvent event) {
        String withPrefix = polychatClient.getFormattedServerId() + " " + event.getComponent().getFormattedText();
        event.setComponent(new TextComponentString(withPrefix));

        // regex to remove things like _test_, **test**, ~~test~~, ***test*** etc
        String message = event.getMessage().replaceAll("[~*_]{1,3}([^~*_]+)[~*_]{1,3}", "$1");

        polychatClient.getCallbacks().newChatMessage(withPrefix, message);
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        polychatClient.getCallbacks().playerEvent(event.player.getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        polychatClient.getCallbacks().playerEvent(event.player.getName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    @Override
    public void sendChatMessage(String message, List<UUID> uuids) {
        ITextComponent string = new TextComponentString(message);
        for (EntityPlayerMP player : server.getPlayerList().getPlayerList()) {
            if (uuids.contains(player.getUniqueID())) {
                continue;
            }
            player.addChatMessage(string);
        }
        server.addChatMessage(string);
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<String>();
        List<String> playerNames = server.getPlayerList().getPlayerList().stream().map(EntityPlayer::getName).collect(Collectors.toList());
        players.ensureCapacity(playerNames.size());
        players.addAll(playerNames);
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
