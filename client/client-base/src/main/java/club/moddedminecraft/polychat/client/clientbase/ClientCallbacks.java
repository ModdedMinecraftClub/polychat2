package club.moddedminecraft.polychat.client.clientbase;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;

import java.util.ArrayList;
import java.util.UUID;

public class ClientCallbacks {
    private final PolychatClient client;
    private boolean cleanShutdown = false;

    public ClientCallbacks(PolychatClient client) {
        this.client = client;
    }

    /**
     * Prepares a message containing the current players online
     *
     * @return list of online players
     */
    private ServerProtos.ServerPlayersOnline getPlayersOnline() {
        ArrayList<String> playersOnline = client.getClientApi().getOnlinePlayers();
        return ServerProtos.ServerPlayersOnline.newBuilder()
                .setServerId(client.getFormattedServerId())
                .setPlayersOnline(playersOnline.size())
                .addAllPlayerNames(playersOnline)
                .build();
    }

    /**
     * Send the currently online players to the server
     */
    public void sendPlayers() {
        client.sendMessage(getPlayersOnline());
    }

    /**
     * Should be called when a player joins or leaves
     *
     * @param username username of player who has joined or left
     * @param status   whether the player has joined or left
     */
    public void playerEvent(String username, ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus status) {
        ServerProtos.ServerPlayerStatusChangedEvent message = ServerProtos.ServerPlayerStatusChangedEvent.newBuilder()
                .setNewPlayerStatus(status)
                .setNewPlayersOnline(getPlayersOnline())
                .setPlayerUsername(username)
                .build();
        client.sendMessage(message);
    }

    /**
     * This method should be called each time a new chat message is recieved in game.
     *
     * @param content the raw chat message, including formatting
     * @param message the message, formatting insensitive (no author, rank, etc)
     */
    public void newChatMessage(String content, String message) {
        String rawContent = content.replaceAll("§.", "");
        String rawMessage = message.replaceAll("§.", "");
        ChatProtos.ChatMessage chatMessage = ChatProtos.ChatMessage.newBuilder()
                .setServerId(client.getFormattedServerId())
                .setMessage(content)
                .setMessageOffset(rawContent.lastIndexOf(rawMessage))
                .build();
        client.sendMessage(chatMessage);
    }

    /**
     * Send server startup message
     */
    public void sendServerStart() {
        ServerProtos.ServerStatus statusMessage = ServerProtos.ServerStatus.newBuilder()
                .setServerId(client.getServerId())
                .setStatus(ServerProtos.ServerStatus.ServerStatusEnum.STARTED)
                .build();
        client.sendMessage(statusMessage);
        client.update();
    }

    /**
     * Send server shutdown or crash message
     */
    public void sendServerStop() {
        ServerProtos.ServerStatus statusMessage = ServerProtos.ServerStatus.newBuilder()
                .setServerId(client.getServerId())
                .setStatus(cleanShutdown ? ServerProtos.ServerStatus.ServerStatusEnum.STOPPED : ServerProtos.ServerStatus.ServerStatusEnum.CRASHED)
                .build();
        client.sendMessage(statusMessage);
        client.update();
    }

    /**
     * Mark server as cleanly shutting down (rather than crashed)
     */
    public void cleanShutdown() {
        cleanShutdown = true;
    }

    /**
     * Denote player as muting or unmuting other servers
     * @param player Player who muted/unmuted other servers
     * @param mute If the player muted or unmuted
     */
    public void playerSetMute(UUID player, boolean mute) {
        if (mute) {
            client.getMuteStorage().addPlayer(player);
        } else {
            client.getMuteStorage().removePlayer(player);
        }
    }


}
