package club.moddedminecraft.polychat.fabric117;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import static net.minecraft.server.command.CommandManager.literal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Polychat implements ModInitializer, ClientApiBase {
    static private PolychatClient client;
    private MinecraftServer server;

    public static PolychatClient getClient() {
        return client;
    }

    @Override
    public void onInitialize() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));

        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            dispatcher.register(literal("pcmute").executes(context -> {
                // TODO:
                CommandSource sender = context.getSource();
                System.out.println(sender.getPlayerNames());

                return 0;
            }));
        }));

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
        ServerPlayConnectionEvents.JOIN.register(this::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(this::onLeave);
    }


    @Override
    public void sendChatMessage(String s, List<UUID> uuids) {
        LiteralText message = new LiteralText(s);
        for (ServerPlayerEntity player: server.getPlayerManager().getPlayerList()) {
            if (uuids.contains(player.getUuid())) {
                continue;
            }
            player.sendMessage(message, false);
        }
        server.sendSystemMessage(message, UUID.randomUUID());
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayerCount();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        String[] playerNames = server.getPlayerNames();
        players.ensureCapacity(playerNames.length);
        Collections.addAll(players, playerNames);
        return players;
    }

    @Override
    public Path getConfigDirectory() {
        return Path.of("./config");
    }

    @Override
    public CommandRunner getRunner(String s) {
        return new Runner(s, server);
    }

    public void sendShutdown() {
        client.getCallbacks().sendServerStop();
    }

    private void onServerStarted(MinecraftServer ser) {
        server = ser;
        client = new PolychatClient(this);
    }

    private void onServerStopped(MinecraftServer ser) {
        client.getCallbacks().cleanShutdown();
    }

    private void recieveChatMessage() {

    }

    private void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        client.getCallbacks().playerEvent(handler.getPlayer().getEntityName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    private void onLeave(ServerPlayNetworkHandler handler, MinecraftServer server) {
        client.getCallbacks().playerEvent(handler.getPlayer().getEntityName(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    private void onTick(MinecraftServer server) {
        if (client != null) {
            client.update();
        }
    }
}
