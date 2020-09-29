package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.server.EventHandler;
import club.moddedminecraft.polychat.core.server.OnlineServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class ServerStatusMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServerStatusMessageHandler.class);
    private final HashMap<String, OnlineServer> onlineServers;

    public ServerStatusMessageHandler(HashMap<String, OnlineServer> onlineServers) {
        this.onlineServers = onlineServers;
    }

    @EventHandler
    public void handle(ServerProtos.ServerStatus msg, ConnectedClient author) {
        String serverId = msg.getServerId();
        OnlineServer server = onlineServers.get(serverId);
        ServerProtos.ServerStatus.ServerStatusEnum status = msg.getStatus();

        if (server != null) {
            if (status == ServerProtos.ServerStatus.ServerStatusEnum.CRASHED || status == ServerProtos.ServerStatus.ServerStatusEnum.STOPPED) {
                onlineServers.remove(serverId);
            }
        }

        if (server == null && status == ServerProtos.ServerStatus.ServerStatusEnum.STARTED) {
            logger.error("Server with id \""
                    + serverId
                    + "\" has unexpectedly sent ServerStatus message before sending ServerInfo message.");
        }
    }
}
