package club.moddedminecraft.polychat.client;

import club.moddedminecraft.polychat.client.clientbase.util.MuteStorage;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuteCommand implements ICommand {

    private final MuteStorage muteStorage;

    public MuteCommand(MuteStorage storage) {
        muteStorage = storage;
    }

    @Override
    public String getCommandName() {
        return "pcmute";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/pcmute";
    }

    @Override
    public List getCommandAliases() {
        return new ArrayList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.addChatMessage(new ChatComponentText("§cMust be run as a player"));
            return;
        }
        UUID uuid = ((EntityPlayerMP) sender).getUniqueID();
        if (muteStorage.checkPlayer(uuid)) {
            muteStorage.removePlayer(uuid);
            sender.addChatMessage(new ChatComponentText("§9Unmuted all other servers and Discord."));
        } else {
            muteStorage.addPlayer(uuid);
            sender.addChatMessage(new ChatComponentText("§9Muted all other servers and Discord."));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return new ArrayList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
