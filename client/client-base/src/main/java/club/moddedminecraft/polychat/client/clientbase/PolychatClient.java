package club.moddedminecraft.polychat.client.clientbase;

import club.moddedminecraft.polychat.client.clientbase.handlers.ChatMessageHandler;
import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.PolychatProtobufMessageDispatcher;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.Client;
import club.moddedminecraft.polychat.core.networklibrary.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PolychatClient {

    private final ClientBase clientBase;
    private final Client client;
    private final PolychatProtobufMessageDispatcher polychatProtobufMessageDispatcher;

    // temporary fields
    private final String color;
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
    public PolychatClient(ClientBase clientImpl, String serverIp, int serverPort, int bufferSize, String color, String serverId) {
        client = new Client(serverIp, serverPort, bufferSize);
        clientBase = clientImpl;
        polychatProtobufMessageDispatcher = new PolychatProtobufMessageDispatcher();
        polychatProtobufMessageDispatcher.addEventHandler(new ChatMessageHandler(clientBase));

        ServerProtos.ServerInfo info = ServerProtos.ServerInfo.newBuilder()
                .setServerId(serverId)
                .setServerName("test client")
                .setServerAddress("test address")
                .setMaxPlayers(clientBase.getMaxPlayers())
                .build();
        sendMessage(info);

        // temporary
        this.color = color;
        this.serverId = serverId;
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
        }

        for (Message message : messages) {
            System.out.println(message);
            polychatProtobufMessageDispatcher.handlePolychatMessage(message);
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message the protobuf message to be sent
     */
    public void sendMessage(com.google.protobuf.Message message) {
        byte[] messageBytes = message.toByteArray();
        try {
            client.sendMessage(messageBytes);
        } catch (IOException e) {
            System.err.println("Failed to send message!");
        }
    }

    /**
     * This method should be called each time a new chat message is recieved in game.
     *
     * @param message The raw chat message, including formatting. Should be in the following format:
     *                [Rank] Username: Message
     * @param author  the author of the message
     */
    public void newChatMessage(String message, String author) {
        String id = "§" + color + serverId;
        String rank;
        String content;
        try {
            rank = message.substring(0, message.indexOf(author) - 1);
            content = message.substring(message.indexOf(":") + 2);
        } catch (StringIndexOutOfBoundsException e) {
            System.err.println("Failed to process message, salvaging chat message as best as possible...");
            rank = "test";
            content = message;
            author = "test";
        }
        ChatProtos.ChatMessage chatMessage = ChatProtos.ChatMessage.newBuilder()
                .setServerId(id)
                .setMessageAuthorRank(rank)
                .setMessageAuthor(author)
                .setMessageContent(content)
                .build();
        sendMessage(chatMessage);
    }

}
