package club.moddedminecraft.polychat2;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Polychat2.MODID)
public class Polychat2 implements ClientApiBase
{
    private PolychatClient client;
    private MinecraftServer server;
    // Define mod id in a common place for everything to reference
    public static final String MODID = "polychat2";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace


    public Polychat2()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        Runtime.getRuntime().addShutdownHook(new Thread(this::sendShutdown));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if(client != null){
            client.update();
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        server = event.getServer();
        client = new PolychatClient(this);
        client.getCallbacks().sendServerStart();
    }

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event){
        event.getDispatcher()
        .register(Commands.literal("pcmute").executes(ctx -> {
            CommandSourceStack sender = ctx.getSource();
            ServerPlayer entity = sender.getPlayerOrException();
            UUID uuid = entity.getUUID();

            if(client.getMuteStorage().checkPlayer(uuid)){
                client.getMuteStorage().removePlayer(uuid);
                sender.sendSuccess(Component.literal("§9Unmuted all other servers and Discord."), false);
            }
            else {
                client.getMuteStorage().addPlayer(uuid);
                sender.sendSuccess(Component.literal("§9Muted all other servers and Discord."), false);
            }

            return 0;
        }));
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppedEvent event){
        client.getCallbacks().cleanShutdown();
    }

    private void sendShutdown(){
        client.getCallbacks().sendServerStop();
    }

    @SubscribeEvent
    public void receiveChatMessage(ServerChatEvent event){
        var originalMessage = event.getMessage().getString();
        var withPrefix = client.getFormattedServerId() + " " + originalMessage;
        event.setMessage(Component.literal(withPrefix));

        var withPrefixProtoMsg = client.getFormattedServerId() + " " + event.getUsername() + ": " + originalMessage;
        client.getCallbacks().newChatMessage(withPrefixProtoMsg, originalMessage);
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event){
        client.getCallbacks().playerEvent(event.getEntity().getName().getString(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED);
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event){
        client.getCallbacks().playerEvent(event.getEntity().getName().getString(), ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT);
    }

    @Override
    public void sendChatMessage(String message, List<UUID> uuids) {
        Component component = Component.literal(message);
        for(ServerPlayer player : server.getPlayerList().getPlayers()){
            if(uuids.contains(player.getUUID())){
                continue;
            }
            player.sendSystemMessage(component);
        }
        server.sendSystemMessage(component);
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        return Arrays.stream(server.getPlayerList().getPlayerNamesArray()).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public CommandRunner getRunner(String command) {
        return new Runner(command, server);
    }

}
