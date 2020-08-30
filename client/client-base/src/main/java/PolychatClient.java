import club.moddedminecraft.polychat.core.networklibrary.Client;
import club.moddedminecraft.polychat.core.networklibrary.Message;

import java.io.IOException;
import java.util.List;

public class PolychatClient {

    private final ClientBase impl;
    private final Client client;

    public PolychatClient(ClientBase impl, String serverIp, int serverPort, int bufferSize) {
        this.client = new Client(serverIp, serverPort, bufferSize);
        this.impl = impl;
    }

    public void update() {
        List<Message> messages = null;
        try {
            messages = client.poll();
        } catch (IOException e) {
            System.err.println("Failed to reconnect to Polychat server");
        }
        if (messages == null) {
            throw new Error("Failed to recieve messages from client");
        }

        for (Message message : messages) {

        }
    }

}
