package club.moddedminecraft.polychat.core.server;

import club.moddedminecraft.polychat.core.networklibrary.Message;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

import club.moddedminecraft.polychat.core.networklibrary.Server;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.security.auth.login.LoginException;

public final class PolychatServer {
    private final ConcurrentLinkedDeque<GenericEvent> queue;
    private final Server server;
    private final JDA jda;

    public static final int TICK_TIME_IN_MILLIS = 50;

    private PolychatServer() throws IOException, LoginException {
        queue = new ConcurrentLinkedDeque<GenericEvent>();
        server = new Server(5005, 128);
        jda = JDABuilder.createDefault("") // will need to be retrieved from YAML
                .addEventListeners(new GenericEventHandler(queue))
                .build();
    }

    public static void main(String[] args) {
        try {
            new PolychatServer().spin();
        } catch (IOException | LoginException e) {
            e.printStackTrace();
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

            }

            GenericEvent nextEvent;
            while ((nextEvent = queue.poll()) != null) {
                if (nextEvent instanceof MessageReceivedEvent) {
                    MessageReceivedEvent ev = (MessageReceivedEvent)nextEvent;
                    if (ev.getAuthor().isBot()) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
