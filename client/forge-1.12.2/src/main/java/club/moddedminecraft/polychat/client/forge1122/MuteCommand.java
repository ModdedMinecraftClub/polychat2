package club.moddedminecraft.polychat.client.forge1122;

import club.moddedminecraft.polychat.client.clientbase.util.MuteStorage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuteCommand implements ICommand {
    private final MuteStorage muteStorage;

    public MuteCommand(MuteStorage storage) {
        muteStorage = storage;
    }

    @Override
    public String getName() {
        return "pcmute";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "/pcmute";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] strings) throws CommandException {
        Entity entity = sender.getCommandSenderEntity();
        if (entity == null) {
            sender.sendMessage(new TextComponentString("§cMust be run as a player"));
            return;
        }
        UUID uuid = entity.getUniqueID();
        if (muteStorage.checkPlayer(uuid)) {
            muteStorage.removePlayer(uuid);
            sender.sendMessage(new TextComponentString("§9Unmuted all other servers and Discord."));
        } else {
            muteStorage.addPlayer(uuid);
            sender.sendMessage(new TextComponentString("§9Muted all other servers and Discord."));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer minecraftServer, ICommandSender iCommandSender) {
        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings, BlockPos blockPos) {
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
