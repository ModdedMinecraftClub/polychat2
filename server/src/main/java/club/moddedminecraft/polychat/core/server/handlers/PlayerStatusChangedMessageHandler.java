package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.server.EventHandler;
import club.moddedminecraft.polychat.core.server.OnlineServer;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class PlayerStatusChangedMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(ServerStatusMessageHandler.class);
    private final HashMap<String, OnlineServer> onlineServers;
    private final TextChannel generalChannel;

    public PlayerStatusChangedMessageHandler(HashMap<String, OnlineServer> onlineServers, TextChannel generalChannel) {
        this.onlineServers = onlineServers;
        this.generalChannel = generalChannel;
    }

    @EventHandler
    public void handle(ServerProtos.ServerPlayerStatusChangedEvent msg, ConnectedClient author) {
        String serverId = msg.getNewPlayersOnline().getServerId();
        OnlineServer server = onlineServers.get(serverId);

        if (server != null) {
            server.updatePlayersInfo(msg.getNewPlayersOnline());
            ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus playerStatus = msg.getNewPlayerStatus();
            String playerUsername = msg.getPlayerUsername();
            String discordMessage;
            if (playerStatus == ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED) {
                discordMessage = server.createServerChatMessage(playerUsername + " has joined the game");
                generalChannel.sendMessage(discordMessage).queue();
            } else if (playerStatus == ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT) {
                discordMessage = server.createServerChatMessage(playerUsername + " has left the game");
                generalChannel.sendMessage(discordMessage).queue();
            } else {
                logger.error("Server with id \""
                        + serverId
                        + "\" sent an unrecognized PlayerStatus");
            }
        } else {
            logger.error("Server with id \""
                    + serverId
                    + "\" has unexpectedly sent ServerPlayerStatusChangedEvent message despite not being marked as online. Have you sent ServerInfo message on server startup?");
        }
    }
}
