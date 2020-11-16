package club.moddedminecraft.polychat.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.server.OnlineServer;
import com.google.protobuf.Any;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class ServerStatusMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServerStatusMessageHandler.class);
    private final HashMap<String, OnlineServer> onlineServers;
    private final TextChannel generalChannel;

    public ServerStatusMessageHandler(HashMap<String, OnlineServer> onlineServers, TextChannel generalChannel) {
        this.onlineServers = onlineServers;
        this.generalChannel = generalChannel;
    }

    @EventHandler
    public void handle(ServerProtos.ServerStatus msg, ConnectedClient author) {
        String serverId = msg.getServerId();
        OnlineServer server = onlineServers.get(serverId);
        ServerProtos.ServerStatus.ServerStatusEnum status = msg.getStatus();

        if (server != null) {
            // forward message to other MC servers;
            Any packedMsg = Any.pack(msg);
            for (OnlineServer onlineServer : onlineServers.values()) {
                ConnectedClient client = onlineServer.getClient();
                if (client != author) {
                    client.sendMessage(packedMsg.toByteArray());
                }
            }

            if (status == ServerProtos.ServerStatus.ServerStatusEnum.CRASHED || status == ServerProtos.ServerStatus.ServerStatusEnum.STOPPED) {
                onlineServers.remove(serverId);
            }

            generalChannel
                    .sendMessage("`" + "[" + serverId + "]" + " Server " + status.toString().toLowerCase() + "`")
                    .queue();
        }

        if (server == null && status == ServerProtos.ServerStatus.ServerStatusEnum.STARTED) {
            logger.error("Server with id \""
                    + serverId
                    + "\" has unexpectedly sent ServerStatus message before sending ServerInfo message.");
        }
    }
}
