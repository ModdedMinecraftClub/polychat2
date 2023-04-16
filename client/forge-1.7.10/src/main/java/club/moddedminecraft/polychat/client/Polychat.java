package club.moddedminecraft.polychat.client;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;

import java.nio.file.Path;
import java.util.*;

@Mod(modid = "polychat", name = "Polychat", version = "2.0.2")
public class Polychat implements ClientApiBase {

    @NetworkCheckHandler
    public boolean checkClient(Map<String, String> map, Side side) {
        return true;
    }

    public static PolychatClient client;
    public static MinecraftServer server;

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        server = event.getServer();
        client = new PolychatClient(this);
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));
        event.registerServerCommand(new MuteCommand(client.getMuteStorage()));
    }

    public void sendShutdown() {
        client.getCallbacks().sendServerStop();
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        client.getCallbacks().sendServerStart();
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        client.getCallbacks().cleanShutdown();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent e) {
        if (client != null) {
            client.update();
        }
    }
    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        client.getCallbacks().playerEvent(event.player.getDisplayName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        client.getCallbacks().playerEvent(event.player.getDisplayName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }


    @SubscribeEvent
    public void recieveChatMessage(ServerChatEvent event) {
        String withPrefix = client.getFormattedServerId() + " " + event.component.getUnformattedTextForChat() +  event.message;
        event.component = new ChatComponentTranslation(withPrefix);

        // regex to remove things like _test_, **test**, ~~test~~, ***test*** etc
        String message = event.message.replaceAll("[~*_]{1,3}([^~*_]+)[~*_]{1,3}", "$1");
        client.getCallbacks().newChatMessage(withPrefix, message);
    }

    @Override
    public void sendChatMessage(String message, List<UUID> uuids) {
        IChatComponent string = new ChatComponentText(message);
        for (EntityPlayerMP player : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList) {
            if (uuids.contains(player.getUniqueID())) {
                continue;
            }
            player.addChatComponentMessage(string);
        }
        server.addChatMessage(string);
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        String[] playerNames = server.getAllUsernames();
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
