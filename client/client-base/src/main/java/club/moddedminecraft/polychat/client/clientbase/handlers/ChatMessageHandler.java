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
        clientBase.sendChatMessage(chatMessage.getMessage());
    }
}
