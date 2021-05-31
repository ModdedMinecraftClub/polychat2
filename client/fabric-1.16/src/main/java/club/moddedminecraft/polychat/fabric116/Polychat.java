package club.moddedminecraft.polychat.fabric116;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Polychat implements ClientApiBase, ModInitializer {
    private static PolychatClient client;
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
    }

    public void onServerStarting(MinecraftServer server) {
        server.getCommandManager().getDispatcher().register(CommandManager.literal("pcmute").executes(context -> {
            ServerCommandSource sender = context.getSource();
            ServerPlayerEntity player = sender.getPlayer();
            UUID uuid = player.getUuid();
            if (client.getMuteStorage().checkPlayer(uuid)) {
                client.getMuteStorage().removePlayer(uuid);
                sender.sendFeedback(new LiteralText("§9Unmuted all other servers and Discord."), false);
            } else {
                client.getMuteStorage().addPlayer(uuid);
                sender.sendFeedback(new LiteralText("§9Muted all other servers and Discord."), false);
            }
            return 0;
        }));
    }

    public void onServerStarted(MinecraftServer server) {
        Polychat.server = server;
        client = new PolychatClient(this);
        client.getCallbacks().sendServerStart();
    }

    public void onServerStopped(MinecraftServer server) {
        client.getCallbacks().sendServerStop();
    }

    public static void onTick() {
        if (client != null) {
            try {
                client.update();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onJoin(ServerPlayerEntity player) {
        client.getCallbacks().playerEvent(player.getName().getString(),
                ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    public static void onLeave(ServerPlayerEntity player) {
        client.getCallbacks().playerEvent(player.getName().getString(),
                ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    public static void receiveChatMessage(String message, UUID uuid) {
        ServerPlayerEntity player = Polychat.server.getPlayerManager().getPlayer(uuid);
        if (player == null) {
            return;
        }
        String username = player.getName().asString();
        int offset = ("<" + username + ">").length();
        String content = message.substring(offset);
        String stripped = content.trim();
        if (stripped.startsWith("!")) {
            client.getCallbacks().newChatMessage(stripped, stripped);
            return;
        }
        client.getCallbacks().newChatMessage(message, message.substring(offset));
    }

    public static void receiveDeathMessage(String message) {
        client.getCallbacks().newChatMessage(message, "");
    }

    @Override
    public void sendChatMessage(String s, List<UUID> uuids) {
        Text string = new LiteralText(s);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (uuids.contains(player.getUuid())) {
                continue;
            }
            player.sendMessage(string, false);
        }
        Polychat.server.sendSystemMessage(string, UUID.randomUUID());
    }

    @Override
    public int getMaxPlayers() {
        return Polychat.server.getPlayerManager().getMaxPlayerCount();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        String[] playerNames = Polychat.server.getPlayerNames();
        players.ensureCapacity(playerNames.length);
        Collections.addAll(players, playerNames);
        return players;
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.INSTANCE.getConfigDir();
    }

    @Override
    public CommandRunner getRunner(String command) {
        return new Runner(command, server);
    }
}
