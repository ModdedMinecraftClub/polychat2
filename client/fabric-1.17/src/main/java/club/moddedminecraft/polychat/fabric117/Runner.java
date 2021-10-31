package club.moddedminecraft.polychat.fabric117;

import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class Runner extends CommandRunner implements CommandOutput {
    private final String command;
    private final MinecraftServer server;
    private final CommandSource source;

    public Runner(String command, MinecraftServer server) {
        this.command = command;
        this.server = server;
        ServerWorld overworld = server.getOverworld();
        this.source = new ServerCommandSource(this, vecFromBlockPos(overworld.getSpawnPos()), new Vec2f(0.0f, 0.0f),
                overworld, 4, "Polychat", new LiteralText("Polychat"), server, (Entity) null) {
        };
    }

    private Vec3d vecFromBlockPos(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void run() {
        server.getCommandManager().execute((ServerCommandSource) this.source, command);
    }

    @Override
    public void sendSystemMessage(Text message, UUID sender) {
        String text = message.getString();
        this.output.add(text.replaceAll("§.", ""));
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldTrackOutput() {
        return true;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }

    @Override
    public boolean cannotBeSilenced() {
        return CommandOutput.super.cannotBeSilenced();
    }
}
