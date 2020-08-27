package club.moddedminecraft.polychat.core.server;

import club.moddedminecraft.polychat.core.server.handlers.PromoteMemberCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import club.moddedminecraft.polychat.core.networklibrary.Server;
import club.moddedminecraft.polychat.core.networklibrary.Message;
import club.moddedminecraft.polychat.core.server.handlers.ChatMessageHandler;
import club.moddedminecraft.polychat.core.server.handlers.GenericJdaEventHandler;

import javax.security.auth.login.LoginException;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class PolychatServer {
    private final ConcurrentLinkedDeque<GenericEvent> queue;
    private final Server server;
    private final PolychatMessageBus polychatMessageBus;
    private final JDA jda;
    private final HashMap<String, OnlineServer> onlineServers;
    private final TextChannel generalChannel;

    public static final int TICK_TIME_IN_MILLIS = 50;

    private PolychatServer() throws IOException, LoginException, InterruptedException {
        // set up jda;
        queue = new ConcurrentLinkedDeque<GenericEvent>();
        onlineServers = new HashMap<>();
        jda = JDABuilder.createDefault("") // will need to be retrieved from YAML;
                .addEventListeners(new GenericJdaEventHandler(queue))
                .build()
                .awaitReady();
        generalChannel = jda.getTextChannelById(""); // same as above here;

        // set up TCP server;
        server = new Server(5005, 2048);

        // set up protobuf message handlers;
        polychatMessageBus = new PolychatMessageBus();
        polychatMessageBus.addEventHandlers(
                new ChatMessageHandler(generalChannel),
                new PromoteMemberCommandHandler(generalChannel, onlineServers)
        );
    }

    public static void main(String[] args) {
        try {
            new PolychatServer().spin();
        } catch (IOException | LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void spin() {
        //noinspection InfiniteLoopStatement
        while (true) {
            long start = System.currentTimeMillis();
            spinOnce();
            long elapsed = System.currentTimeMillis() - start;
            long sleepFor = TICK_TIME_IN_MILLIS - elapsed;
            if (sleepFor > 0) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(sleepFor);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void spinOnce() {
        try {
            for (Message message : server.poll()) {
                polychatMessageBus.handlePolychatMessage(message);
            }

            GenericEvent nextEvent;
            while ((nextEvent = queue.poll()) != null) {
                if (nextEvent instanceof MessageReceivedEvent) {
                    MessageReceivedEvent ev = (MessageReceivedEvent)nextEvent;
                    if (ev.getAuthor().isBot()) {
                        break;
                    }
                    // TODO: Discord commands handling;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
