package club.moddedminecraft.polychat.server.handlers.protomessages;

import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public final class GenericCommandResultMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServerStatusMessageHandler.class);
    private final JDA jda;

    public GenericCommandResultMessageHandler(JDA jda) {
        this.jda = jda;
    }

    @EventHandler
    public void handle(CommandProtos.GenericCommandResult msg, ConnectedClient author) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Command " + "`/" + msg.getCommand() + "`" + " executed")
                .setColor(Color.decode(msg.getColour()))
                .addField("Server", msg.getServerId(), false);

        if (!msg.getCommandOutput().isEmpty()) {
            eb.addField("Command output", msg.getCommandOutput(), false);
        }

        TextChannel channelCmdOriginatedFrom = jda.getTextChannelById(msg.getDiscordChannelId());

        if (channelCmdOriginatedFrom != null) {
            channelCmdOriginatedFrom.sendMessage(eb.build()).queue();
        } else {
            logger.error("Client told me a command was sent from a Discord channel that doesn't exist.");
        }
    }
}
