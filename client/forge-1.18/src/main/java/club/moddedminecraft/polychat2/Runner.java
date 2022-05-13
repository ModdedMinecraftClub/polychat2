package club.moddedminecraft.polychat2;

import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class Runner extends CommandRunner implements CommandSource {

    private final String command;
    private final MinecraftServer server;
    private final CommandSourceStack source;

    public Runner(String command, MinecraftServer server) {
        this.command = command;
        this.server = server;
        ServerLevelData overworld = server.getWorldData().overworldData();
        ServerLevel ow = null;

        for (ServerLevel level : server.getAllLevels()) {
            if(level.dimension().equals(Level.OVERWORLD)) {
                ow = level;
            }
        }
        if (ow == null){
            ow = server.getAllLevels().iterator().next();
        }

        this.source = new CommandSourceStack(
                this, new Vec3(overworld.getXSpawn(), overworld.getYSpawn(), overworld.getZSpawn()),
                Vec2.ZERO, ow, 4, "Polychat", new TextComponent("Polychat"), server, (Entity) null
        );
    }

    @Override
    public void run() {
        server.getCommands().performCommand(this.source, command);
    }

    @Override
    public void sendMessage(Component p_80166_, UUID p_80167_) {
        String text = p_80166_.getString();
        this.output.add(text.replaceAll("§.", ""));
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    @Override
    public boolean alwaysAccepts() {
        return true;
    }
}
