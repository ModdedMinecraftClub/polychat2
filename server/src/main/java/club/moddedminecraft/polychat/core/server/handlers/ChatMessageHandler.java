package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.server.EventHandler;
import club.moddedminecraft.polychat.core.server.OnlineServer;
import com.google.protobuf.Any;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class ChatMessageHandler {
    private final TextChannel generalChannel;
    private final HashMap<String, OnlineServer> onlineServers;

    public ChatMessageHandler(TextChannel generalChannel, HashMap<String, OnlineServer> onlineServers) {
        this.generalChannel = generalChannel;
        this.onlineServers = onlineServers;
    }

    @EventHandler
    public void handle(ChatProtos.ChatMessage msg, ConnectedClient author) {
        sendMessageToDiscord(msg);
        sendMessageToOtherClients(msg);
    }

    private void sendMessageToDiscord(ChatProtos.ChatMessage msg) {
        String discordMsg = "`"
                + "[" + msg.getServerId() + "] "
                + "[" + msg.getMessageAuthorRank() + "] "
                + msg.getMessageAuthor() + ":"
                + "` "
                + msg.getMessageContent();
        generalChannel.sendMessage(discordMsg).queue();
    }

    private void sendMessageToOtherClients(ChatProtos.ChatMessage msg) {
        Any packedMsg = Any.pack(msg);

        for (Map.Entry<String, OnlineServer> entry : onlineServers.entrySet()) {
            String serverId = entry.getKey();
            ConnectedClient client = entry.getValue().getClient();

            if (!serverId.equals(msg.getServerId())) {
                client.sendMessage(packedMsg.toByteArray());
            }
        }
    }
}
