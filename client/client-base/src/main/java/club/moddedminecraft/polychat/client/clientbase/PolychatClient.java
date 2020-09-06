package club.moddedminecraft.polychat.client.clientbase;

import club.moddedminecraft.polychat.client.clientbase.handlers.ChatMessageHandler;
import club.moddedminecraft.polychat.client.clientbase.util.YamlConfig;
import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.PolychatProtobufMessageDispatcher;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.Client;
import club.moddedminecraft.polychat.core.networklibrary.Message;
import com.google.protobuf.Any;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PolychatClient {

    private final ClientBase clientBase;
    private final Client client;
    private final PolychatProtobufMessageDispatcher polychatProtobufMessageDispatcher;
    private final OnlinePlayerThread playerThread;
    private final YamlConfig config;
    private final String serverId;

    private boolean cleanShutdown = false;

    /**
     * Connects to the Polychat server based on config values
     *
     * @param clientImpl the implementation of the client protocol
     */
    public PolychatClient(ClientBase clientImpl) {
        clientBase = clientImpl;
        config = getConfig();
        client = new Client(
                config.getOrDefault("poly_address", "localhost"),
                config.getOrDefault("poly_port", 5005),
                config.getOrDefault("poly_buffersize", 32768)
        );
        polychatProtobufMessageDispatcher = new PolychatProtobufMessageDispatcher();
        playerThread = new OnlinePlayerThread(this);
        serverId = config.getOrDefault("serverId", "ID");

        polychatProtobufMessageDispatcher.addEventHandler(new ChatMessageHandler(clientBase));
        setupInfoMessage();
        playerThread.start();
    }

    /**
     * Gets config for client
     *
     * @return client config
     */
    private YamlConfig getConfig() {
        try {
            Path directory = clientBase.getConfigDirectory();
            Path configPath = directory.resolve("polychat.yml");
            directory.toFile().mkdir();

            if (configPath.toFile().createNewFile()) {
                return getDefaultConfig(configPath);
            }

            return YamlConfig.fromFilesystem(configPath);
        } catch (IOException e) {
            System.err.println("Failed to load config!");
            e.printStackTrace();
        }
        return YamlConfig.fromInMemoryString("");
    }

    /**
     * Sets up a default config
     *
     * @param path path for new config
     * @return default config
     * @throws IOException if unable to save file
     */
    private YamlConfig getDefaultConfig(Path path) throws IOException {
        YamlConfig def = YamlConfig.fromInMemoryString("");
        def.set("name", "A Minecraft Server");
        def.set("address", "example.com");
        def.set("color", 14);
        def.set("serverId", "ID");
        def.set("poly_address", "localhost");
        def.set("poly_port", 5005);
        def.set("poly_buffersize", 32768);
        def.saveToFile(path);
        return def;
    }

    /**
     * Prepares server info message for (re)connects
     */
    private void setupInfoMessage() {
        ServerProtos.ServerInfo info = ServerProtos.ServerInfo.newBuilder()
                .setServerId(serverId)
                .setServerName(config.getOrDefault("name", "DEFAULT_NAME"))
                .setServerAddress(config.getOrDefault("address", "DEFAULT_ADDRESS"))
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
     * @param content the raw chat message, including formatting
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
     *
     * @return formatted server id
     */
    public String getServerId() {
        int color = config.getOrDefault("color", 14);
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

    /**
     * Prepares a message containing the current players online
     *
     * @return list of online players
     */
    private ServerProtos.ServerPlayersOnline getPlayersOnline() {
        ArrayList<String> playersOnline = clientBase.getOnlinePlayers();
        return ServerProtos.ServerPlayersOnline.newBuilder()
                .setServerId(serverId)
                .setPlayersOnline(playersOnline.size())
                .addAllPlayerNames(playersOnline)
                .build();
    }

    /**
     * Send the currently online players to the server
     */
    public void sendPlayers() {
        sendMessage(getPlayersOnline());
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
        sendMessage(message);
    }

}