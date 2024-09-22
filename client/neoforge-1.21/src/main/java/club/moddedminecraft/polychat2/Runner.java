package club.moddedminecraft.polychat2;

import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Runner extends CommandRunner implements CommandSource {
    private final String command;
    private final MinecraftServer server;
    private final CommandSourceStack source;

    public Runner(String command, MinecraftServer server){
        this.command = command;
        this.server = server;

        ServerLevel overworld = null;
        for(ServerLevel level : server.getAllLevels()){
            if(level.dimension().equals(Level.OVERWORLD)){
                overworld = level;
            }
        }

        if(overworld == null){
            overworld = server.getAllLevels().iterator().next();
        }

        this.source = new CommandSourceStack(
                this, Vec3.ZERO, Vec2.ZERO, overworld, 4, "Polychat", Component.literal("Polychat"), server, null
        );
    }

    @Override
    public void run() {
        var context = this.server.getCommands().getDispatcher().parse(this.command, this.source);
        this.server.getCommands().performCommand(context, this.command);
    }

    @Override
    public void sendSystemMessage(Component p_230797_) {
        String text = p_230797_.getString();
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
