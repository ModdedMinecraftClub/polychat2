package club.moddedminecraft.polychat.core.networklibrary;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/***
 * This class represents an instance of a central server for this networking library. There should be one such central
 * server in any Polychat installation. This class both accepts incoming connections and provides utility methods to
 * manage those connections. (This is not a violation of the single-responsibility principle because the single
 * responsibility is to manage the multiple connections.)
 */
public final class Server{
    private final int bufferSize;
    private final LinkedList<ConnectedClient> clients;
    private final ServerSocketChannel serverSocketChannel;

    /**
     * Initializes a new instance of this class with the given port and buffer size.
     *
     * @param port       The port to bind to. No specific IP will be bound to, so incoming connections can use any IP of
     *                   the computer that hosts the server.
     * @param bufferSize The buffer size. The maximum message that can be sent is is this buffer size minus 4 bytes. It
     *                   is recommended, but not required, that the buffer size is the same on both the client(s) and
     *                   the central server, but it is not required as long as all messages are less than or equal to
     *                   both the buffer sizes minus 4 in length.
     * @throws IOException If the given port can't be bound to.
     */
    public Server(int port, int bufferSize) throws IOException{
        this.bufferSize = bufferSize;

        clients = new LinkedList<>();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    /**
     * Polls all currently connected clients for new messages, and cleans up any clients that have had errors. When
     * clients are cleaned up, no attempt is made to re-establish a connection with the client. That responsibiliy is
     * left to the clients and implemented in the <code>Client</code> class.
     *
     * @return The list of retrieved messages. This list may be empty.
     * @throws IOException If an error occurs when dealing with the main socket to accept incoming connections. If an
     *                     exception is thrown here, incoming messages may be lost. Such an exception is not likely
     *                     recoverable, so the main program should probably crash in this case (with the exception being
     *                     printed to the console!). Client connections will be closed and the associated exception
     *                     printed to the console in case of errors with specific connections.
     */
    public List<Message> poll() throws IOException{
        //handle new clients
        SocketChannel socketChannel;
        while((socketChannel = serverSocketChannel.accept()) != null){
            try{
                ConnectedClient newClient = new ConnectedClient(socketChannel, bufferSize);
                clients.add(newClient);
            }catch(IOException e){
                e.printStackTrace(); //we don't want to take down the whole ServerSocketChannel system and thus the whole server in case of errors here
            }
        }

        //receive messages
        ArrayList<Message> receivedMessages = new ArrayList<>();
        clients.removeIf(client -> {
            if(!client.poll(receivedMessages)){
                try{
                    client.shutdown();
                }catch(IOException e){
                    e.printStackTrace(); //we've done our best effort to shut this client down.... and it will be abandoned anyway, so we have to ignore this error (we do what we must because we can)
                }
                return true;
            }else{
                return false;
            }
        });

        return receivedMessages;
    }

    /**
     * Schedules the given message to be sent to all currently-connected clients. Note that the message will not
     * actually be sent until appropriate calls to <code>poll()</code>. An arbitrary but finite number of calls may be
     * needed — it is guaranteed that the messages will eventually be sent with repeated calls to <code>poll()</code>,
     * but not after any particular number.
     *
     * @param message The message to send.
     */
    public void broadcastMessage(byte[] message){
        for(ConnectedClient client : clients){
            client.sendMessage(message);
        }
    }

    /**
     * Gets a list of currently-connected clients. You may call sendMessage() on individual clients, and you may do
     * <code>==</code>< comparison to compare if two clients match (e.g. to pair messages with clients). Note that
     * <code>==</code> comparisons are not guaranteed to work after a client disconnects and reconnects for any reason,
     * including transient network errors.
     *
     * @return A list of currently-connected clients.
     */
    public List<ConnectedClient> getClients(){
        return Collections.unmodifiableList(clients);
    }

}
