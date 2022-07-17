package club.moddedminecraft.polychat.server.handlers.protomessages;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.server.OnlineServer;
import com.google.protobuf.Any;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;

public final class ChatMessageHandler {
    private final TextChannel generalChannel;
    private final HashMap<String, OnlineServer> onlineServers;

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

        // Some mods can manage to send messages that are 0 length, so instead of trying to process them and having the server crash
        // just return
        if (discordMsg.length() == 0) {
            return;
        }
        discordMsg = "`"
                + discordMsg.substring(0, msg.getMessageOffset()) +
                "` "
                + discordMsg.substring(msg.getMessageOffset());
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
