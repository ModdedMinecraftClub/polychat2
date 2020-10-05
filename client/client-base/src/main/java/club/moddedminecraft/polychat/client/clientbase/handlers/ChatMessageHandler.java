package club.moddedminecraft.polychat.client.clientbase.handlers;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.util.MuteStorage;
import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;

public class ChatMessageHandler {

    private final ClientApiBase clientApiBase;
    private final MuteStorage muteStorage;

    public ChatMessageHandler(ClientApiBase clientApiBase, MuteStorage muteStorage) {
        this.clientApiBase = clientApiBase;
        this.muteStorage = muteStorage;
    }

    @EventHandler
    public void handle(ChatProtos.ChatMessage chatMessage, ConnectedClient author) {
        clientApiBase.sendChatMessage(chatMessage.getMessage(), muteStorage.getMuteList());
    }
}
