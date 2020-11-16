package club.moddedminecraft.polychat.forge116;

import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;

import java.util.UUID;

public class Runner extends CommandRunner implements ICommandSource {

    private final String command;
    private final MinecraftServer server;
    private final CommandSource source;

    public Runner(String command, MinecraftServer server) {
        this.command = command;
        this.server = server;
        ServerWorld overworld = null;
        for (ServerWorld world : server.getWorlds()) {
            if (world.getDimensionType().equals(DimensionType.OVERWORLD)) {
                overworld = world;
            }
        }
        if (overworld == null) {
            overworld = server.getWorlds().iterator().next(); // ¯\_(ツ)_/¯
        }
        this.source = new CommandSource(this, vecFromBlockPos(overworld.getSpawnPoint()), Vector2f.ZERO, overworld, 4, "Polychat", new StringTextComponent("Polychat"), server, (Entity) null);
    }

    private Vector3d vecFromBlockPos(BlockPos pos) {
        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void run() {
        server.getCommandManager().handleCommand(this.source, command);
    }

    @Override
    public void sendMessage(ITextComponent component, UUID uuid) {
        String text = component.getString();
        this.output.add(text.replaceAll("§.", ""));
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldReceiveErrors() {
        return true;
    }

    @Override
    public boolean allowLogging() {
        return true;
    }
}
