package club.moddedminecraft.polychat.forge116;

import club.moddedminecraft.polychat.core.messagelibrary.MessageLibraryExampleClass;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("polychat")
public class Polychat {
    public Polychat() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
    }

}
