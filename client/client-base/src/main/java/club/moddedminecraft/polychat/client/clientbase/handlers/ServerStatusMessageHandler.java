package club.moddedminecraft.polychat.client.clientbase.handlers;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.util.MuteStorage;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;

public class ServerStatusMessageHandler {

    private final ClientApiBase clientApiBase;
    private final MuteStorage muteStorage;

    public ServerStatusMessageHandler(ClientApiBase clientApiBase, MuteStorage muteStorage) {
        this.clientApiBase = clientApiBase;
        this.muteStorage = muteStorage;
    }

    @EventHandler
    public void handle(ServerProtos.ServerStatus statusMessage, ConnectedClient author) {
        clientApiBase.sendChatMessage(statusMessage.getServerId() + " Server " + statusMessage.getStatus().toString().toLowerCase(), muteStorage.getMuteList());
    }
}
