package club.moddedminecraft.polychat.core.server;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.ConcurrentLinkedDeque;

public class GenericEventHandler implements EventListener {
    private final ConcurrentLinkedDeque<GenericEvent> queue;

    public GenericEventHandler(ConcurrentLinkedDeque<GenericEvent> queue) {
        this.queue = queue;
    }

    @Override
    public void onEvent(GenericEvent event) {
        queue.add(event);
    }
}
