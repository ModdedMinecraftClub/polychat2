package club.moddedminecraft.polychat.client.forge1122;

import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

@Mod(modid = Polychat.MODID, name = Polychat.NAME, version = Polychat.VERSION)
public class Polychat {
    public static final String MODID = "polychat";
    public static final String NAME = "Forge 1.12.2 implementation of the Polychat client";
    public static final String VERSION = "2.0.0";

    private Forge1122Client forge1122Client;
    private PolychatClient client;

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
        client.update();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        forge1122Client = new Forge1122Client(event.getServer());
        client = new PolychatClient(forge1122Client, "localhost", 5005, 32768, 14, "Twelve");
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        client.sendServerStart();
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        client.cleanShutdown();
    }

    public void sendShutdown() {
        client.sendServerStop();
    }

    @SubscribeEvent
    public void recieveChatMessage(ServerChatEvent event) {
        String withPrefix = client.getServerId() + " " + event.getComponent().getFormattedText();
        event.setComponent(new TextComponentString(withPrefix));
        client.newChatMessage(withPrefix, event.getMessage());
    }
}
