package club.moddedminecraft.polychat.client.clientbase.handlers;

import club.moddedminecraft.polychat.client.clientbase.ClientBase;
import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;

public class ChatMessageHandler {

    private final ClientBase clientBase;

    public ChatMessageHandler(ClientBase clientBase) {
        this.clientBase = clientBase;
    }

    @EventHandler
    public void handle(ChatProtos.ChatMessage chatMessage, ConnectedClient author) {
        StringBuilder message = new StringBuilder();
        message.append("[").append(chatMessage.getServerId()).append("] "); // [ID]
        message.append("[").append(chatMessage.getMessageAuthorRank()).append("] "); // [RANK]
        message.append(chatMessage.getMessageAuthor()).append(": "); // author:
        message.append(chatMessage.getMessageContent());
        clientBase.sendChatMessage(message.toString());
    }
}
