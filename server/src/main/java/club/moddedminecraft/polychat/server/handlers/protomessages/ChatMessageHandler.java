package club.moddedminecraft.polychat.server.handlers.protomessages;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.server.OnlineServer;
import com.google.protobuf.Any;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;

public final class ChatMessageHandler {
    private final TextChannel generalChannel;
    private final HashMap<String, OnlineServer> onlineServers;
    private final static Logger logger = LoggerFactory.getLogger(ChatMessageHandler.class);

    public ChatMessageHandler(TextChannel generalChannel, HashMap<String, OnlineServer> onlineServers) {
        this.generalChannel = generalChannel;
        this.onlineServers = onlineServers;
    }

    @EventHandler
    public void handle(ChatProtos.ChatMessage msg, ConnectedClient author) {
        sendMessageToDiscord(msg);
        sendMessageToOtherClients(msg, author);
    }

    private void sendMessageToDiscord(ChatProtos.ChatMessage msg) {
        String discordMsg = msg.getMessage().replaceAll("§.", "");
        String part1;
        String part2;
        try {
            part1 = discordMsg.substring(0, msg.getMessageOffset());
            part2 = discordMsg.substring(msg.getMessageOffset());
        } catch (StringIndexOutOfBoundsException e) {
            logger.error("STRING OUT OF BOUNDS EXCEPTION");
            logger.error("MESSAGE: " + msg.getMessage());
            logger.error("OFFSET: " + msg.getMessageOffset());
            return;
        }
        discordMsg = "`"
                + part1 +
                "` "
                + part2;
        generalChannel.sendMessage(discordMsg).queue();
    }

    private void sendMessageToOtherClients(ChatProtos.ChatMessage msg, ConnectedClient author) {
        Any packedMsg = Any.pack(msg);

        for (OnlineServer server : onlineServers.values()) {
            ConnectedClient client = server.getClient();
            if (client != author) {
                client.sendMessage(packedMsg.toByteArray());
            }
        }
    }
}
