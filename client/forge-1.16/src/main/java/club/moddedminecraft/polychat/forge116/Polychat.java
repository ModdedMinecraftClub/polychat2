package club.moddedminecraft.polychat.forge116;

import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("polychat")
public class Polychat {
    private Forge116Client forge116Client;
    private PolychatClient client;

    public Polychat() {
        MinecraftForge.EVENT_BUS.register(this);
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
    public void recieveChatMessage(ServerChatEvent event) {
        System.out.println(client == null);
        String withPrefix = client.getServerId() + " " + event.getComponent().getString();
        event.setComponent(new StringTextComponent(withPrefix));
        client.newChatMessage(withPrefix, event.getMessage());
    }

}
