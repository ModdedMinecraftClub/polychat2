package club.moddedminecraft.polychat.client;

import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;


public class Runner extends CommandRunner implements ICommandSender {

    private final String command;
    private final MinecraftServer server;

    public Runner(String command, MinecraftServer server) {
        this.command = command;
        this.server = server;
    }

    @Override
    public void run() {
        server.getCommandManager().executeCommand(this, command);
    }
    @Override
    public String getCommandSenderName() {
        return "Polychat";
    }

    @Override
    public IChatComponent func_145748_c_() {
        return new ChatComponentText(getCommandSenderName());
    }

    @Override
    public void addChatMessage(IChatComponent message) {
        this.output.add(message.getUnformattedTextForChat());
    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String command) {
        return true;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates();
    }

    @Override
    public World getEntityWorld() {
        return DimensionManager.getWorld(0);
    }
}
