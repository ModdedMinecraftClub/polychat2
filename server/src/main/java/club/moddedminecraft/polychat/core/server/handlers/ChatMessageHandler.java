package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.server.EventHandler;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChatMessageHandler {
    private final TextChannel generalChannel;

    public ChatMessageHandler(TextChannel generalChannel) {
        this.generalChannel = generalChannel;
    }

    @EventHandler
    public void handle(ChatProtos.ChatMessage msg, ConnectedClient author) {
        String discordMsg = "`"
                + "[" + msg.getServerId() + "] "
                + "[" + msg.getMessageAuthorRank() + "] "
                + msg.getMessageAuthor() + ":"
                + "` "
                + msg.getMessageContent();
        generalChannel.sendMessage(discordMsg).queue();
    }
}
