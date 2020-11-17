package club.moddedminecraft.polychat.server;

import club.moddedminecraft.polychat.core.common.YamlConfig;
import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.PolychatProtobufMessageDispatcher;
import club.moddedminecraft.polychat.core.networklibrary.Message;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import club.moddedminecraft.polychat.server.discordcommands.ExecCommand;
import club.moddedminecraft.polychat.server.discordcommands.OnlineCommand;
import club.moddedminecraft.polychat.server.discordcommands.RestartCommand;
import club.moddedminecraft.polychat.server.discordcommands.TpsCommand;
import club.moddedminecraft.polychat.server.handlers.jdaevents.GenericJdaEventHandler;
import club.moddedminecraft.polychat.server.handlers.jdaevents.MessageReceivedHandler;
import club.moddedminecraft.polychat.server.handlers.protomessages.*;
import com.google.protobuf.Any;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class PolychatServer {
    private final ConcurrentLinkedDeque<GenericEvent> queue;
    private final Server server;
    private final PolychatProtobufMessageDispatcher polychatProtobufMessageDispatcher;
    private final JDA jda;
    private final HashMap<String, OnlineServer> onlineServers;
    private final TextChannel generalChannel;
    private final MessageReceivedHandler messageReceivedHandler;
    private final List<String> broadcastMessages;

    private int broadcastsTimer;
    private int broadcastMsgsIndex;

    private final static Logger logger = LoggerFactory.getLogger(PolychatServer.class);

    public static final int TICK_TIME_IN_MILLIS = 50;
    public static final int BROADCAST_EVERY_X_IN_TICKS = 12000;

    private PolychatServer() throws IOException, LoginException, InterruptedException {
        // get YAML config
        YamlConfig yamlConfig = getConfig();

        // set up broadcasts
        broadcastMessages = yamlConfig.get("broadcastMsgs");
        broadcastMsgsIndex = 0;
        broadcastsTimer = 0;

        // set up TCP;
        server = new Server(yamlConfig.get("tcpPort"), yamlConfig.get("bufferSize"));

        // set up JDA event queue & servers hashmap;
        queue = new ConcurrentLinkedDeque<GenericEvent>();
        onlineServers = new HashMap<>();

        // set up JDA commands;
        CommandClient commandClient = new CommandClientBuilder()
                .setOwnerId(yamlConfig.get("ownerId")) // will need to be retrieved from YAML;
                .setPrefix(yamlConfig.get("commandPrefix"))
                .addCommands(
                        new ExecCommand(server, onlineServers),
                        new OnlineCommand(onlineServers),
                        new RestartCommand(onlineServers),
                        new TpsCommand(onlineServers)
                )
                .build();

        // set up main JDA;
        jda = JDABuilder.createDefault(yamlConfig.get("token")) // will need to be retrieved from YAML;
                .addEventListeners(
                        commandClient,
                        new GenericJdaEventHandler(queue)
                )
                .build()
                .awaitReady();
        generalChannel = jda.getTextChannelById(yamlConfig.get("generalChannelId")); // same as above here;
        messageReceivedHandler = new MessageReceivedHandler(generalChannel, server);

        // set up Protobuf message handlers;
        polychatProtobufMessageDispatcher = new PolychatProtobufMessageDispatcher();
        polychatProtobufMessageDispatcher.addEventHandlers(
                new ChatMessageHandler(generalChannel, onlineServers),
                new PromoteMemberCommandHandler(generalChannel, onlineServers),
                new ServerInfoMessageHandler(onlineServers),
                new ServerStatusMessageHandler(onlineServers, generalChannel),
                new PlayersOnlineMessageHandler(onlineServers),
                new PlayerStatusChangedMessageHandler(onlineServers, generalChannel),
                new GenericCommandResultMessageHandler(jda)
        );
    }

    public static void main(String[] args) {
        try {
            new PolychatServer().spin();
        } catch (IOException | LoginException | InterruptedException e) {
            logger.error("Error while starting Polychat server", e);
        }
    }

    private void spin() {
        while (true) {
            long start = System.currentTimeMillis();
            spinOnce();
            long elapsed = System.currentTimeMillis() - start;
            long sleepFor = TICK_TIME_IN_MILLIS - elapsed;
            if (sleepFor > 0) {
                try {
                    Thread.sleep(sleepFor);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void spinOnce() {
        try {
            if (broadcastsTimer == BROADCAST_EVERY_X_IN_TICKS) {
                String broadcastMsg = broadcastMessages.get(broadcastMsgsIndex);

                ChatProtos.ChatMessage msg = ChatProtos.ChatMessage.newBuilder()
                        .setServerId("MMCC")
                        .setMessage("[MMCC] " + broadcastMsg)
                        .setMessageOffset(5)
                        .build();
                Any any = Any.pack(msg);
                server.broadcastMessageToAll(any.toByteArray());

                broadcastsTimer = 0;
                broadcastMsgsIndex = (broadcastMsgsIndex + 1) % broadcastMessages.size();
            } else {
                broadcastsTimer += 1;
            }

            for (Message message : server.poll()) {
                polychatProtobufMessageDispatcher.handlePolychatMessage(message);
            }

            GenericEvent nextEvent;
            while ((nextEvent = queue.poll()) != null) {
                if (nextEvent instanceof MessageReceivedEvent) {
                    MessageReceivedEvent ev = (MessageReceivedEvent) nextEvent;
                    messageReceivedHandler.handle(ev);
                }
            }
        } catch (IOException e) {
            logger.error("Error occurred in Polychat server event loop", e);
        }
    }

    private YamlConfig getDefaultConfig(Path path) throws IOException {
        YamlConfig def = YamlConfig.fromInMemoryString("");
        def.set("token", "");
        def.set("ownerId", "");
        def.set("commandPrefix", "!");
        def.set("generalChannelId", "");
        def.set("tcpPort", 5005);
        def.set("bufferSize", 4096);
        def.set("broadcastMsgs", Arrays.asList("example broadcast message 1", "example broadcast message 2"));
        def.saveToFile(path);
        return def;
    }

    public YamlConfig getConfig() {
        try {
            Path configPath = Paths.get("polychat.yml");

            if (configPath.toFile().createNewFile()) {
                getDefaultConfig(configPath);
                logger.error("You must have a config to use polychat! Creating default polychat.yml...");
                System.exit(0);
            }

            return YamlConfig.fromFilesystem(configPath);
        } catch (IOException e) {
            logger.error("Failed to create a new config!");
            e.printStackTrace();
            System.exit(1);
        }
        return YamlConfig.fromInMemoryString("");
    }
}
