package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.server.EventHandler;
import club.moddedminecraft.polychat.core.server.OnlineServer;

import java.util.HashMap;

public class ServerInfoMessageHandler {
    private final HashMap<String, OnlineServer> onlineServers;

    public ServerInfoMessageHandler(HashMap<String, OnlineServer> onlineServers) {
        this.onlineServers = onlineServers;
    }

    @EventHandler
    public void handle(ServerProtos.ServerInfo msg, ConnectedClient author) {
        // if new add to list;
        OnlineServer server = onlineServers.get(msg.getServerId());
        if (server == null) {
            OnlineServer newServer = new OnlineServer(msg, author);
            onlineServers.put(msg.getServerId(), newServer);
        }
    }
}
