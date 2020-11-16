package club.moddedminecraft.polychat.server.discordcommands;

import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import club.moddedminecraft.polychat.server.OnlineServer;
import com.google.protobuf.Any;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

public class ExecCommand extends Command {

    private final Server networkServer;
    private final HashMap<String, OnlineServer> onlineServers;

    public ExecCommand(Server networkServer, HashMap<String, OnlineServer> onlineServers) {
        this.networkServer = networkServer;
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
        String id = args[0].toUpperCase();
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        OnlineServer server = onlineServers.get(id);
        if (server == null && !id.equals("<ALL>")) {
            EmbedBuilder errEb = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Server with ID " + "`" + id + "`" + " not found.")
                    .setColor(Color.RED);
            event.reply(errEb.build());
        } else {
            CommandProtos.GenericCommand cmd = CommandProtos.GenericCommand.newBuilder()
                    .setDiscordChannelId(event.getChannel().getId())
                    .setDiscordCommandName("exec")
                    .setDefaultCommand("$args")
                    .addAllArgs(Arrays.asList(commandArgs))
                    .build();
            Any any = Any.pack(cmd);
            byte[] bytes = any.toByteArray();

            if (id.equals("<ALL>")) {
                networkServer.broadcastMessageToAll(bytes);
            } else {
                server.getClient().sendMessage(any.toByteArray());
            }
        }
    }
}
