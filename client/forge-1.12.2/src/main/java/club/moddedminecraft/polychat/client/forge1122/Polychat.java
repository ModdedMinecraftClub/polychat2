package club.moddedminecraft.polychat.client.forge1122;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Polychat.MODID, name = Polychat.NAME, version = Polychat.VERSION, serverSideOnly = true)
public class Polychat {
    public static final String MODID = "polychat";
    public static final String NAME = "Forge 1.12.2 implementation of the Polychat client";
    public static final String VERSION = "2.0.0";

    public Polychat() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
    }
}
