package club.moddedminecraft.polychat.server.discordcommands;

import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.server.OnlineServer;
import com.google.protobuf.Any;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;

public class TpsCommand extends Command {

    private final HashMap<String, OnlineServer> onlineServers;

    public TpsCommand(HashMap<String, OnlineServer> onlineServers) {
        this.onlineServers = onlineServers;
        this.name = "tps";
        this.help = "Gets current ticks per second for a server";
        this.arguments = "<server id>";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            EmbedBuilder errEb = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("You must specify the server ID")
                    .setColor(Color.RED);
            event.reply(errEb.build());
            return;
        }

        OnlineServer server = onlineServers.get(event.getArgs());
        if (server == null) {
            EmbedBuilder errEb = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Server with ID " + "`" + event.getArgs() + "`" + " not found.")
                    .setColor(Color.RED);
            event.reply(errEb.build());
        } else {
            CommandProtos.GenericCommand cmd = CommandProtos.GenericCommand.newBuilder()
                    .setDiscordChannelId(event.getChannel().getId())
                    .setDiscordCommandName("tps")
                    .setDefaultCommand("forge tps")
                    .addAllArgs(Collections.emptyList())
                    .build();
            Any any = Any.pack(cmd);
            server.getClient().sendMessage(any.toByteArray());
        }
    }
}
