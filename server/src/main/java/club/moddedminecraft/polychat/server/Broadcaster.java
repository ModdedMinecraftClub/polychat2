package club.moddedminecraft.polychat.server;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import com.google.protobuf.Any;

import java.util.List;

public final class Broadcaster {
    private final List<String> broadcastMessages;
    private final Server server;

    private int broadcastsTimer;
    private int broadcastMsgsIndex;

    public static final int BROADCAST_EVERY_X_IN_TICKS = (10 * 60 * 20);

    public Broadcaster(List<String> broadcastMessages, Server server) {
        this.broadcastMessages = broadcastMessages;
        this.server = server;

        broadcastsTimer = 0;
        broadcastMsgsIndex = 0;
    }

    public void tick() {
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
                .setServerId("MMCC")
                .setMessage("[MMCC] " + broadcastMsg)
                .setMessageOffset(5)
                .build();
        Any any = Any.pack(msg);
        server.broadcastMessageToAll(any.toByteArray());
    }
}
