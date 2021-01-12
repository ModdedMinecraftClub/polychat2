package club.moddedminecraft.polychat.server;

import club.moddedminecraft.polychat.core.common.YamlConfig;
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
import club.moddedminecraft.polychat.server.services.BroadcasterService;
import club.moddedminecraft.polychat.server.services.ITickedService;
import club.moddedminecraft.polychat.server.services.JdaEventQueuePollerService;
import club.moddedminecraft.polychat.server.services.ServerPollerService;
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
    private final ArrayList<ITickedService> tickedServices;

    private final static Logger logger = LoggerFactory.getLogger(PolychatServer.class);

    public static final int TICK_TIME_IN_MILLIS = 50;

    private PolychatServer() throws IOException, LoginException, InterruptedException {
        // get YAML config
        YamlConfig yamlConfig = getConfig();

        // set up TCP;
        Server server = new Server(yamlConfig.get("tcpPort"), yamlConfig.get("bufferSize"));

        // set up broadcasts
        List<String> broadcastMessages = yamlConfig.getOrDefault("broadcastMsgs", new ArrayList<String>());
        String broadcastID = yamlConfig.getOrDefault("broadcastID", "BROADCAST");
        String broadcastPrefix = yamlConfig.getOrDefault("broadcastPrefix", "[System]");

        // set up JDA event queue & servers hashmap;
        ConcurrentLinkedDeque<GenericEvent> queue = new ConcurrentLinkedDeque<GenericEvent>();
        HashMap<String, OnlineServer> onlineServers = new HashMap<>();

        // set up JDA commands;
        CommandClient commandClient = new CommandClientBuilder()
                .setOwnerId(yamlConfig.get("ownerId"))
                .setPrefix(yamlConfig.get("commandPrefix"))
                .addCommands(
                        new ExecCommand(server, onlineServers),
                        new OnlineCommand(onlineServers),
                        new RestartCommand(onlineServers),
                        new TpsCommand(onlineServers)
                )
                .build();

        // set up main JDA;
        JDA jda = JDABuilder.createDefault(yamlConfig.get("token"))
                .addEventListeners(
                        commandClient,
                        new GenericJdaEventHandler(queue)
                )
                .build()
                .awaitReady();
        TextChannel generalChannel = jda.getTextChannelById(yamlConfig.get("generalChannelId"));


        // set up Protobuf message handlers;
        PolychatProtobufMessageDispatcher polychatProtobufMessageDispatcher = new PolychatProtobufMessageDispatcher();
        polychatProtobufMessageDispatcher.addEventHandlers(
                new ChatMessageHandler(generalChannel, onlineServers),
                new PromoteMemberCommandHandler(generalChannel, onlineServers),
                new ServerInfoMessageHandler(onlineServers),
                new ServerStatusMessageHandler(onlineServers, generalChannel),
                new PlayersOnlineMessageHandler(onlineServers),
                new PlayerStatusChangedMessageHandler(onlineServers, generalChannel),
                new GenericCommandResultMessageHandler(jda)
        );

        // set up ticked services
        MessageReceivedHandler messageReceivedHandler = new MessageReceivedHandler(generalChannel, server);

        BroadcasterService broadcasterService = new BroadcasterService(broadcastID, broadcastPrefix, broadcastMessages, server);
        JdaEventQueuePollerService jdaEventQueuePollerService = new JdaEventQueuePollerService(queue, messageReceivedHandler);
        ServerPollerService serverPollerService = new ServerPollerService(server, polychatProtobufMessageDispatcher);

        tickedServices = new ArrayList<ITickedService>() {{
            add(broadcasterService);
            add(jdaEventQueuePollerService);
            add(serverPollerService);
        }};

        for (ITickedService tickedService : tickedServices) {
            tickedService.start();
        }
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
        for (ITickedService tickedService : tickedServices) {
            tickedService.tick();
        }
    }

    private YamlConfig getDefaultConfig(Path path) throws IOException {
        YamlConfig def = YamlConfig.fromInMemoryString("");
        def.set("broadcastsPrefix", "");
        def.set("token", "");
        def.set("ownerId", "");
        def.set("commandPrefix", "!");
        def.set("generalChannelId", "");
        def.set("tcpPort", 5005);
        def.set("bufferSize", 4096);
        def.set("broadcastMsgs", Arrays.asList("example broadcast message 1", "example broadcast message 2"));
        def.set("broadcastID", "BROADCAST");
        def.set("broadcastPrefix", "[System]");
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
