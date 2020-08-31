package club.moddedminecraft.polychat.core.networklibrary;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides an interface to connect to the central server from a client process. One instance of this class
 * represents one connection with one corresponding <code>ConnectedClient</code> instance on the remote server. This
 * class will automatically re-connect in case of networking errors. Note that both incoming and outgoing messages may
 * be lost during this process.
 */
public final class Client{
    private static final long RECONNECTION_INTERVAL = 5000;
    private static long lastReconnectionAttempt = 0;
    private final String ip;
    private final int port;
    private final int bufferSize;
    private final ArrayList<byte[]> reconnectMessageSet = new ArrayList<>();
    private ConnectedClient client = null;

    /**
     * Creates a new client with the given IP, port, and buffer size. Note that the connection will not actually be made
     * until <code>poll()</code> is called, which will
     *
     * @param ip         the IP to connect to
     * @param port       the port to connect to
     * @param bufferSize The buffer size. The maximum message that can be sent is is this buffer size minus 4 bytes. It
     *                   is recommended, but not required, that the buffer size is the same on both the client(s) and
     *                   the central server, but it is not required as long as all messages are less than or equal to
     *                   both the buffer sizes minus 4 in length.
     */
    public Client(String ip, int port, int bufferSize){
        this.ip = ip;
        this.port = port;
        this.bufferSize = bufferSize;
    }

    /**
     * Polls for new messages, returning them in a possibly-empty list (if there are no new messages and/or the incoming
     * messages have been lost due to network errors)
     *
     * @return A possibly-empty list of new messages
     * @throws IOException If there are networking errors and a new connection can't be established. If there is an
     *                     error here, it is recommended that you print the exception to the console and call
     *                     <code>poll()</code> again with the normal schedule.
     */
    public List<Message> poll() throws IOException{
        ArrayList<Message> messages = new ArrayList<>();

        if(client == null || !client.poll(messages)){
            reinitializeClient(); //the new client, if it is initialized will not be used until the next call to poll()
        }

        return messages;
    }

    /**
     * Sends a message to the connected server
     *
     * @param message The message to send
     * @throws IOException If there are networking errors and a new connection can't be established. If there is an
     *                     error here, it is recommended that you print the exception to the console and call
     *                     <code>poll()</code> again with the normal schedule.
     */
    public void sendMessage(byte[] message) throws IOException{
        if(client == null){
            reinitializeClient();
        }

        if(client != null){
            client.sendMessage(message);
        }
    }

    private void reinitializeClient() throws IOException{
        if(client != null){
            client.shutdown();
        }

        client = null;
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastReconnectionAttempt >= RECONNECTION_INTERVAL){
            lastReconnectionAttempt = currentTime;
            client = new ConnectedClient(SocketChannel.open(new InetSocketAddress(ip, port)), bufferSize);
            for(byte[] message : reconnectMessageSet){
                client.sendMessage(message);
            }
        }
    }

    public ArrayList<byte[]> getReconnectMessageSet(){
        return reconnectMessageSet;
    }

}
