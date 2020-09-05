package club.moddedminecraft.polychat.client.clientbase;

import club.moddedminecraft.polychat.client.clientbase.handlers.ChatMessageHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.PolychatProtobufMessageDispatcher;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.Client;
import club.moddedminecraft.polychat.core.networklibrary.Message;
import com.google.protobuf.Any;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PolychatClient {

    private final ClientBase clientBase;
    private final Client client;
    private final PolychatProtobufMessageDispatcher polychatProtobufMessageDispatcher;
    private boolean cleanShutdown = false;

    // temporary fields
    private final int color;
    private final String serverId;

    /**
     * Connects to a server with the given IP, port, and buffer size.
     *
     * @param clientImpl the implementation of the client protocol
     * @param serverIp   the server IP to connect to
     * @param serverPort the server port to connect to
     * @param bufferSize The buffer size. The maximum message that can be sent is is this buffer size minus 4 bytes. It
     *                   is recommended, but not required, that the buffer size is the same on both the client(s) and
     *                   the central server, but it is not required as long as all messages are less than or equal to
     *                   both the buffer sizes minus 4 in length.
     */
    public PolychatClient(ClientBase clientImpl, String serverIp, int serverPort, int bufferSize, int color, String serverId) {
        client = new Client(serverIp, serverPort, bufferSize);
        clientBase = clientImpl;
        polychatProtobufMessageDispatcher = new PolychatProtobufMessageDispatcher();
        polychatProtobufMessageDispatcher.addEventHandler(new ChatMessageHandler(clientBase));

        // TODO: get from config
        this.color = color;
        this.serverId = serverId;

        startupMessages();
    }

    private void startupMessages() {
        ServerProtos.ServerInfo info = ServerProtos.ServerInfo.newBuilder()
                .setServerId(serverId)
                .setServerName("test client") // TODO: get from config
                .setServerAddress("test address")
                .setMaxPlayers(clientBase.getMaxPlayers())
                .build();
        Any packed = Any.pack(info);
        client.getReconnectMessageSet().add(packed.toByteArray());
    }

    /**
     * This method should be called at a consistent interval in order to process messages from the server.
     */
    public void update() {
        List<Message> messages = new ArrayList<>();
        try {
            messages = client.poll();
        } catch (IOException e) {
            System.err.println("Failed to reconnect to Polychat server");
            e.printStackTrace();
        }

        for (Message message : messages) {
            polychatProtobufMessageDispatcher.handlePolychatMessage(message);
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message the protobuf message to be sent
     */
    public void sendMessage(com.google.protobuf.Message message) {
        Any packedMessage = Any.pack(message);
        byte[] messageBytes = packedMessage.toByteArray();
        try {
            client.sendMessage(messageBytes);
        } catch (IOException e) {
            System.err.println("Failed to send message!");
            e.printStackTrace();
        }
    }

    /**
     * This method should be called each time a new chat message is recieved in game.
     *
     * @param message the raw chat message, including formatting
     * @param message the message, formatting insensitive (no author, rank, etc)
     */
    public void newChatMessage(String content, String message) {
        String rawContent = content.replaceAll("§.", "");
        String rawMessage = message.replaceAll("§.", "");
        ChatProtos.ChatMessage chatMessage = ChatProtos.ChatMessage.newBuilder()
                .setServerId(serverId)
                .setMessage(content)
                .setMessageOffset(rawContent.lastIndexOf(rawMessage))
                .build();
        sendMessage(chatMessage);
    }

    /**
     * Gets the formatted server ID ex. [A5]
     * @return formatted server id
     */
    public String getServerId() {
        return String.format("§%01x", color) + "[" + serverId + "]" + "§r";
    }

    /**
     * Send server startup message
     */
    public void sendServerStart() {
        ServerProtos.ServerStatus statusMessage = ServerProtos.ServerStatus.newBuilder()
                .setServerId(serverId)
                .setStatus(ServerProtos.ServerStatus.ServerStatusEnum.STARTED)
                .build();
        sendMessage(statusMessage);
        update();
    }

    /**
     * Send server shutdown or crash message
     */
    public void sendServerStop() {
        ServerProtos.ServerStatus statusMessage = ServerProtos.ServerStatus.newBuilder()
                .setServerId(serverId)
                .setStatus(cleanShutdown ? ServerProtos.ServerStatus.ServerStatusEnum.STOPPED : ServerProtos.ServerStatus.ServerStatusEnum.CRASHED)
                .build();
        sendMessage(statusMessage);
        update();
    }

    /**
     * Mark server as cleanly shutting down (rather than crashed)
     */
    public void cleanShutdown() {
        cleanShutdown = true;
    }

}