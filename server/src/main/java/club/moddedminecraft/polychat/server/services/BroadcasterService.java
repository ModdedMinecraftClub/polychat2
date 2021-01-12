package club.moddedminecraft.polychat.server.services;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import com.google.protobuf.Any;

import java.util.List;

public final class BroadcasterService extends TimedTickableService {
    private final String id;
    private final String prefix;
    private final List<String> broadcastMessages;
    private final Server server;

    private int broadcastMsgsIndex;

    public static final int INTERVAL_IN_TICKS = (10 * 60 * 20);

    public BroadcasterService(String broadcastID, String broadcastPrefix, List<String> broadcastMessages, Server server) {
        super(INTERVAL_IN_TICKS);

        this.id = broadcastID;
        this.prefix = broadcastPrefix;
        this.broadcastMessages = broadcastMessages;
        this.server = server;

        this.broadcastMsgsIndex = 0;
    }

    @Override
    public void onRun() {
        if (broadcastMessages.size() == 0) {
            return;
        }

        broadcast();
        broadcastMsgsIndex = (broadcastMsgsIndex + 1) % broadcastMessages.size();
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
