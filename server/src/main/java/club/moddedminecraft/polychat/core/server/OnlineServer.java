package club.moddedminecraft.polychat.core.server;

import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;

import java.util.ArrayList;
import java.util.List;

public final class OnlineServer {
    private String serverId;
    private String serverName;
    private String serverAddress;
    private int maxPlayers;
    private int playersOnline;
    private List<String> onlinePlayerNames;
    private ConnectedClient client;

    public OnlineServer(ServerProtos.ServerInfo serverInfo, ConnectedClient client) {
        this.serverId = serverInfo.getServerId();
        this.serverName = serverInfo.getServerName();
        this.serverAddress = serverInfo.getServerAddress();
        this.maxPlayers = serverInfo.getMaxPlayers();
        this.playersOnline = 0;
        this.onlinePlayerNames = new ArrayList<>();
        this.client = client;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getPlayersOnline() {
        return playersOnline;
    }

    public void setPlayersOnline(int playersOnline) {
        this.playersOnline = playersOnline;
    }

    public List<String> getOnlinePlayerNames() {
        return onlinePlayerNames;
    }

    public void setOnlinePlayerNames(List<String> onlinePlayerNames) {
        this.onlinePlayerNames = onlinePlayerNames;
    }

    public ConnectedClient getClient() {
        return client;
    }

    public void updatePlayersInfo(ServerProtos.ServerPlayersOnline serverPlayersOnlineMessage) {
        this.playersOnline = serverPlayersOnlineMessage.getPlayersOnline();
        this.onlinePlayerNames = serverPlayersOnlineMessage.getPlayerNamesList();
    }

    public String createServerChatMessage(String message) {
        return "`[" + serverId + "] " + message + "`";
    }

}
