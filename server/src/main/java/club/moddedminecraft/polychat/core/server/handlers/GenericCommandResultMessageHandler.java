package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.server.EventHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

public final class GenericCommandResultMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServerStatusMessageHandler.class);
    private final JDA jda;

    public GenericCommandResultMessageHandler(JDA jda) {
        this.jda = jda;
    }

    @EventHandler
    public void handle(CommandProtos.GenericCommandResult msg, ConnectedClient author) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Command executed")
                .setColor(Color.BLUE) // TODO: ask 132 for format colour comes in;
                .addField("Server", msg.getServerId(), false)
                .addField("Command output", msg.getCommandOutput(), false);
        TextChannel channelCmdOriginatedFrom = jda.getTextChannelById(msg.getDiscordChannelId());

        if (channelCmdOriginatedFrom != null) {
            channelCmdOriginatedFrom.sendMessage(eb.build()).queue();
        } else {
            logger.error("Client told me a command was sent from a Discord channel that doesn't exist.");
        }
    }
}
