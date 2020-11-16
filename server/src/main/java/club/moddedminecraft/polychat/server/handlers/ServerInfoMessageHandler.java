package club.moddedminecraft.polychat.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.server.OnlineServer;

import java.util.HashMap;

public final class ServerInfoMessageHandler {
    private final HashMap<String, OnlineServer> onlineServers;

    public ServerInfoMessageHandler(HashMap<String, OnlineServer> onlineServers) {
        this.onlineServers = onlineServers;
    }

    @EventHandler
    public void handle(ServerProtos.ServerInfo msg, ConnectedClient author) {
        // if old then replace with new server
        onlineServers.remove(msg.getServerId());
        OnlineServer newServer = new OnlineServer(msg, author);
        onlineServers.put(msg.getServerId(), newServer);
    }
}
