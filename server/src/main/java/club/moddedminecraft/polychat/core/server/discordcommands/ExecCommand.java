package club.moddedminecraft.polychat.core.server.discordcommands;

import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.core.server.OnlineServer;
import com.google.protobuf.Any;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;

public class ExecCommand extends Command {

    private final HashMap<String, OnlineServer> onlineServers;

    public ExecCommand(HashMap<String, OnlineServer> onlineServers) {
        this.onlineServers = onlineServers;
        this.name = "exec";
        this.help = "Executes a command in-game";
        this.arguments = "<server id> <command> <arg1> <arg2> ...";
        this.guildOnly = true;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        MessageEmbed argsErrEb = new EmbedBuilder()
                .setTitle("Error")
                .setDescription("Invalid arguments.")
                .addField("Correct usage", this.name + " " + this.arguments, false)
                .setColor(Color.RED)
                .build();

        // if no args send error msg;
        if (event.getArgs().isEmpty()) {
            event.reply(argsErrEb);
            return;
        }

        // split args into an array;
        String[] args = event.getArgs().split("\\s+");

        // if no <server id> and <command> send error msg;
        if (args.length < 2) {
            event.reply(argsErrEb);
            return;
        }

        // extract all the info from args array;
        String id = args[0];
        String command = args[1];
        String[] commandArgs = Arrays.copyOfRange(args, 2, args.length);
        // join in-game command args into a string, because that's what protobuf msg uses;
        String commandArgsString = String.join(" ", commandArgs);

        OnlineServer server = onlineServers.get(id);
        if (server == null) {
            EmbedBuilder errEb = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Server with ID " + "`" + id + "`" + " not found.")
                    .setColor(Color.RED);
            event.reply(errEb.build());
        } else {
            CommandProtos.GenericCommand cmd = CommandProtos.GenericCommand.newBuilder()
                    .setDiscordChannelId(event.getChannel().getId())
                    .setDiscordCommandName("exec")
                    .setDefaultCommand(command)
                    .setArgs(commandArgsString)
                    .build();
            Any any = Any.pack(cmd);
            server.getClient().sendMessage(any.toByteArray());
        }
    }
}
