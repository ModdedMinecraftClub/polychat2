package club.moddedminecraft.polychat.forge116;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mod("polychat")
public class Polychat implements ClientApiBase {
    private PolychatClient client;
    private MinecraftServer server;

    public Polychat() {
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
    public void onServerStarting(FMLServerStartedEvent event) {
        server = event.getServer();
        client = new PolychatClient(this);
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        if (client != null) {
            client.getCallbacks().sendServerStart();
        }
    }

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        if (client == null) {
            return;
        }
        event.getDispatcher().register(Commands.literal("pcmute").executes(context -> {
            CommandSource sender = context.getSource();
            ServerPlayerEntity entity = sender.asPlayer();
            UUID uuid = entity.getUniqueID();
            if (client.getMuteStorage().checkPlayer(uuid)) {
                client.getMuteStorage().removePlayer(uuid);
                sender.sendFeedback(new StringTextComponent("§9Unmuted all other servers and Discord."), false);
            } else {
                client.getMuteStorage().addPlayer(uuid);
                sender.sendFeedback(new StringTextComponent("§9Muted all other servers and Discord."), false);
            }
            return 0;
        }));
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        if (client == null) {
            return;
        }
        client.getCallbacks().cleanShutdown();
    }

    public void sendShutdown() {
        if (client == null) {
            return;
        }
        client.getCallbacks().sendServerStop();
    }

    @SubscribeEvent

    public void receiveChatMessage(ServerChatEvent event) {
        if (client == null) {
            return;
        }
        String withPrefix = client.getFormattedServerId() + " " + event.getComponent().getString();
        event.setComponent(new StringTextComponent(withPrefix));
        client.getCallbacks().newChatMessage(withPrefix, event.getMessage());
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (client == null) {
            return;
        }
        client.getCallbacks().playerEvent(event.getEntity().getName().getString(),
                ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (client == null) {
            return;
        }
        client.getCallbacks().playerEvent(event.getEntity().getName().getString(),
                ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    public void sendChatMessage(String message, List<UUID> uuids) {
        if (client == null) {
            return;
        }
        ITextComponent string = new StringTextComponent(message);
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            if (uuids.contains(player.getUniqueID())) {
                continue;
            }
            player.sendMessage(string, player.getUniqueID());
        }
        server.sendMessage(string, UUID.randomUUID());
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        if (client == null) {
            return new ArrayList();
        }
        ArrayList<String> players = new ArrayList<>();
        String[] playerNames = server.getPlayerList().getOnlinePlayerNames();
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
