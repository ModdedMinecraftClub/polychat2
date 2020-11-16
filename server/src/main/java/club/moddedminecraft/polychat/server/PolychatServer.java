package club.moddedminecraft.polychat.server;

import club.moddedminecraft.polychat.core.common.YamlConfig;
import club.moddedminecraft.polychat.core.messagelibrary.PolychatProtobufMessageDispatcher;
import club.moddedminecraft.polychat.server.discordcommands.ExecCommand;
import club.moddedminecraft.polychat.server.discordcommands.OnlineCommand;
import club.moddedminecraft.polychat.server.discordcommands.RestartCommand;
import club.moddedminecraft.polychat.server.discordcommands.TpsCommand;

import club.moddedminecraft.polychat.server.handlers.jdaevents.GenericJdaEventHandler;
import club.moddedminecraft.polychat.server.handlers.jdaevents.MessageReceivedHandler;
import club.moddedminecraft.polychat.server.handlers.protomessages.*;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import club.moddedminecraft.polychat.core.networklibrary.Server;
import club.moddedminecraft.polychat.core.networklibrary.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class PolychatServer {
    private final ConcurrentLinkedDeque<GenericEvent> queue;
    private final Server server;
    private final PolychatProtobufMessageDispatcher polychatProtobufMessageDispatcher;
    private final JDA jda;
    private final HashMap<String, OnlineServer> onlineServers;
    private final TextChannel generalChannel;
    private final MessageReceivedHandler messageReceivedHandler;

    private final static Logger logger = LoggerFactory.getLogger(PolychatServer.class);
    public static final int TICK_TIME_IN_MILLIS = 50;

    private PolychatServer() throws IOException, LoginException, InterruptedException {
        // get YAML config;
        logger.info(Paths.get("").toAbsolutePath().toString());
        YamlConfig yamlConfig = YamlConfig.fromFilesystem(Paths.get("config.yml"));

        // set up TCP;
        server = new Server(yamlConfig.get("tcpPort"), yamlConfig.get("bufferSize"));

        // set up JDA event queue & servers hashmap;
        queue = new ConcurrentLinkedDeque<GenericEvent>();
        onlineServers = new HashMap<>();

        // set up JDA commands;
        CommandClient commandClient = new CommandClientBuilder()
                .setOwnerId(yamlConfig.get("ownerId")) // will need to be retrieved from YAML;
                .setPrefix(yamlConfig.get("prefix"))
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
            for (Message message : server.poll()) {
                polychatProtobufMessageDispatcher.handlePolychatMessage(message);
            }

            GenericEvent nextEvent;
            while ((nextEvent = queue.poll()) != null) {
                if (nextEvent instanceof MessageReceivedEvent) {
                    MessageReceivedEvent ev = (MessageReceivedEvent)nextEvent;
                    messageReceivedHandler.handle(ev);
                }
            }
        } catch (IOException e) {
            logger.error("Error occurred in Polychat server event loop", e);
        }
    }
}
