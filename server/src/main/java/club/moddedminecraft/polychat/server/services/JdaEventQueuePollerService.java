package club.moddedminecraft.polychat.server.services;

import club.moddedminecraft.polychat.server.handlers.jdaevents.MessageReceivedHandler;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.ConcurrentLinkedDeque;

public class JdaEventQueuePollerService implements TickableService {
    private final ConcurrentLinkedDeque<GenericEvent> queue;
    private final MessageReceivedHandler messageReceivedHandler;

    public JdaEventQueuePollerService(ConcurrentLinkedDeque<GenericEvent> queue, MessageReceivedHandler messageReceivedHandler) {
        this.queue = queue;
        this.messageReceivedHandler = messageReceivedHandler;
    }

    @Override
    public void tick() {
        GenericEvent nextEvent;
        while ((nextEvent = queue.poll()) != null) {
            if (nextEvent instanceof MessageReceivedEvent) {
                MessageReceivedEvent ev = (MessageReceivedEvent) nextEvent;
                messageReceivedHandler.handle(ev);
            }
        }
    }
}
