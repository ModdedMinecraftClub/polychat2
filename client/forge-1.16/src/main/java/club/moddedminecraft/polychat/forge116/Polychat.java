package club.moddedminecraft.polychat.forge116;

import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@Mod("polychat")
public class Polychat {
    private Forge116Client forge116Client;
    private PolychatClient client;

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
        forge116Client = new Forge116Client(event.getServer());
        client = new PolychatClient(forge116Client, "localhost", 5005, 32768, 4, "Sixteen");
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        client.sendServerStart();
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        client.cleanShutdown();
    }

    public void sendShutdown() {
        client.sendServerStop();
    }

    @SubscribeEvent
    public void recieveChatMessage(ServerChatEvent event) {
        String withPrefix = client.getServerId() + " " + event.getComponent().getString();
        event.setComponent(new StringTextComponent(withPrefix));
        client.newChatMessage(withPrefix, event.getMessage());
    }

}
