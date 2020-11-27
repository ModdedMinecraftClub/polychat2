package club.moddedminecraft.polychat.fabric116;

import java.util.UUID;

import com.mojang.brigadier.ResultConsumer;

import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;

public class Runner extends CommandRunner implements CommandOutput {

    private final String command;
    private final MinecraftServer server;
    private final ServerCommandSource source;

    public Runner(String command, MinecraftServer server) {
        this.command = command;
        this.server = server;
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        WorldProperties properties = serverWorld.getLevelProperties();
        Vec3d spawn = new Vec3d(properties.getSpawnX(), properties.getSpawnY(), properties.getSpawnZ());
        this.source = new ServerCommandSource(this, serverWorld == null ? Vec3d.ZERO : spawn, Vec2f.ZERO, serverWorld,
                4, "Polychat", new LiteralText("Polychat"), server, (Entity) null);
    }

    @Override
    public void run() {
        server.getCommandManager().execute(source, command);
    }

    @Override
    public void sendSystemMessage(Text message, UUID senderUuid) {
        output.add(message.getString());
        server.sendSystemMessage(message, senderUuid);
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return true;
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldTrackOutput() {
        return true;
    }

}
