package club.moddedminecraft.polychat.core.server.handlers;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.ConcurrentLinkedDeque;

public final class GenericJdaEventHandler implements EventListener {
    private final ConcurrentLinkedDeque<GenericEvent> queue;

    public GenericJdaEventHandler(ConcurrentLinkedDeque<GenericEvent> queue) {
        this.queue = queue;
    }

    @Override
    public void onEvent(GenericEvent event) {
        queue.add(event);
    }
}
