package club.moddedminecraft.polychat2;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mod(Polychat2.MOD_ID)
public class Polychat2 implements ClientApiBase
{
    private PolychatClient client;
    private MinecraftServer server;

    public static final String MOD_ID = "polychat2";

    private static final Logger LOGGER = LogManager.getLogger();

    public Polychat2() {
        MinecraftForge.EVENT_BUS.register(this);
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (client != null) {
            client.update();
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartedEvent event) {
        server = event.getServer();
        client = new PolychatClient(this);
        client.getCallbacks().sendServerStart();
    }

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("pcmute").executes(ctx -> {
            CommandSourceStack sender = ctx.getSource();
            ServerPlayer entity = sender.getPlayerOrException();
            UUID uuid = entity.getUUID();
            if (client.getMuteStorage().checkPlayer(uuid)) {
                client.getMuteStorage().removePlayer(uuid);
                sender.sendSuccess(new TextComponent("§9Unmuted all other servers and Discord."), false);
            }
            else {
                client.getMuteStorage().addPlayer(uuid);
                sender.sendSuccess(new TextComponent("§9Muted all other servers and Discord."), false);
            }

            return 0;
        }));
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        client.getCallbacks().cleanShutdown();
    }

    public void sendShutdown() {
        client.getCallbacks().sendServerStop();
    }

    @SubscribeEvent
    public void receiveChatMessage(ServerChatEvent event) {
        String withPrefix = client.getFormattedServerId() + " " + event.getComponent().getString();
        event.setComponent(new TextComponent(withPrefix));
        client.getCallbacks().newChatMessage(withPrefix, event.getMessage());
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        client.getCallbacks().playerEvent(event.getEntity().getName().getString(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        client.getCallbacks().playerEvent(event.getEntity().getName().getString(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    public void sendChatMessage(String message, List<UUID> uuids) {
        TextComponent string = new TextComponent(message);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (uuids.contains(player.getUUID())) {
                continue;
            }
            player.sendMessage(string, player.getUUID());
        }
        server.sendMessage(string, UUID.randomUUID());
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        String[] playerNames = server.getPlayerList().getPlayerNamesArray();
        players.ensureCapacity(playerNames.length);
        Collections.addAll(players, playerNames);
        return players;
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public CommandRunner getRunner(String comamnd) {
        return new Runner(comamnd, server);
    }
}
