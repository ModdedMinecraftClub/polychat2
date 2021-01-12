package club.moddedminecraft.polychat.server.services;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import com.google.protobuf.Any;

import java.util.List;

public final class BroadcasterService implements ITickedService {
    private final String id;
    private final String prefix;
    private final List<String> broadcastMessages;
    private final Server server;

    private int broadcastsTimer;
    private int broadcastMsgsIndex;

    public static final int BROADCAST_EVERY_X_IN_TICKS = (10 * 60 * 20);

    public BroadcasterService(String broadcastID, String broadcastPrefix, List<String> broadcastMessages, Server server) {
        this.id = broadcastID;
        this.prefix = broadcastPrefix;
        this.broadcastMessages = broadcastMessages;
        this.server = server;

        this.broadcastsTimer = 0;
        this.broadcastMsgsIndex = 0;
    }

    public void tick() {
        if (broadcastMessages.size() == 0) {
            return;
        }

        if (broadcastsTimer == BROADCAST_EVERY_X_IN_TICKS) {
            broadcast();
            broadcastsTimer = 0;
            broadcastMsgsIndex = (broadcastMsgsIndex + 1) % broadcastMessages.size();
        } else {
            broadcastsTimer += 1;
        }
    }

    private void broadcast() {
        String broadcastMsg = broadcastMessages.get(broadcastMsgsIndex);
        ChatProtos.ChatMessage msg = ChatProtos.ChatMessage.newBuilder()
                .setServerId(id)
                .setMessage(prefix + " " + broadcastMsg)
                .setMessageOffset(5)
                .build();
        Any any = Any.pack(msg);
        server.broadcastMessageToAll(any.toByteArray());
    }
}
