package club.moddedminecraft.polychat.client.clientbase.handlers;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.util.MuteStorage;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;

public class PlayerStatusChangedMessageHandler {

    private final ClientApiBase clientApiBase;
    private final MuteStorage muteStorage;

    public PlayerStatusChangedMessageHandler(ClientApiBase clientApiBase, MuteStorage muteStorage) {
        this.clientApiBase = clientApiBase;
        this.muteStorage = muteStorage;
    }

    @EventHandler
    public void handle(ServerProtos.ServerPlayerStatusChangedEvent statusMessage, ConnectedClient author) {
        String id = statusMessage.getNewPlayersOnline().getServerId();
        String message = id + " " + statusMessage.getPlayerUsername() + " has " + statusMessage.getNewPlayerStatus().toString().toLowerCase() + " the game";
        clientApiBase.sendChatMessage(message, muteStorage.getMuteList());
    }
}
