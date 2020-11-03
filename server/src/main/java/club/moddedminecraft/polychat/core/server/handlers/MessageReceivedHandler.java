package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import com.google.protobuf.Any;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageReceivedHandler {
    private final TextChannel generalChannel;
    private final Server server;

    public MessageReceivedHandler(TextChannel generalChannel, Server server) {
        this.generalChannel = generalChannel;
        this.server = server;
    }

    public void handle(MessageReceivedEvent event) {
        net.dv8tion.jda.api.entities.Message discordMsg = event.getMessage();

        // ignore messages from self and other bots;
        if (event.getAuthor().isBot()) {
            return;
        }

        // ignore commands in this event handler;
        if (discordMsg.getContentRaw().startsWith("!")) {
            return;
        }

        // ignore non-#general
        if (!discordMsg.getChannel().getId().equals(generalChannel.getId())) {
            return;
        }

        // construct Protobuf chat message from Discord message;
        String msgStringForClients =
                "[Discord] " + discordMsg.getAuthor().getName() + ": " + discordMsg.getContentRaw();
        ChatProtos.ChatMessage protoChatMessage = ChatProtos.ChatMessage.newBuilder()
                .setServerId("Discord")
                .setMessage(msgStringForClients)
                .setMessageOffset(msgStringForClients.indexOf(':'))
                .build();

        // pack the message;
        Any packedMsg = Any.pack(protoChatMessage);

        // convert the message to bytes;
        byte[] msgBytes = packedMsg.toByteArray();

        // send the message to MC clients;
        server.broadcastMessageToAll(msgBytes);
    }
}
