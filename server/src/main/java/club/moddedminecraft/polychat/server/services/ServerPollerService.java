package club.moddedminecraft.polychat.server.services;

import club.moddedminecraft.polychat.core.messagelibrary.PolychatProtobufMessageDispatcher;
import club.moddedminecraft.polychat.core.networklibrary.Message;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ServerPollerService implements ITickedService {
    private final Server server;
    private final PolychatProtobufMessageDispatcher dispatcher;
    private final static Logger logger = LoggerFactory.getLogger(ServerPollerService.class);

    public ServerPollerService(Server server, PolychatProtobufMessageDispatcher dispatcher) {
        this.server = server;
        this.dispatcher = dispatcher;
    }

    @Override
    public void start() {}

    @Override
    public void tick() {
        try {
            for (Message message : server.poll()) {
                dispatcher.handlePolychatMessage(message);
            }
        } catch (IOException e) {
            logger.error("Error while handling an incoming Protobuf message", e);
        }
    }
}
