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
        String frontMatter = discordMsg.substring(0, msg.getMessageOffset());
        StringBuilder msgBuilder = new StringBuilder();
        if (frontMatter.length() > 0) {
            msgBuilder.append("`").append(frontMatter).append("` ");
        }
        msgBuilder.append(discordMsg.substring(msg.getMessageOffset()));
        generalChannel.sendMessage(msgBuilder.toString()).queue();
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
