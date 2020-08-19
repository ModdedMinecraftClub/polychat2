package club.moddedminecraft.polychat.core.networklibrary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.List;

/**
 * This class represents a currently connected client to the server. This class is not to be used (except as an
 * implementation detail) outside of the server process.
 */
public final class ConnectedClient{
    private static final int MESSAGE_HEADER_LENGTH_IN_BYTES = 4;
    private final SocketChannel socketChannel;
    private ClientStatus currentStatus = ClientStatus.CONNECTING;
    private ByteBuffer readBuffer;
    private ByteBuffer sendBuffer;
    private ArrayDeque<byte[]> sendQueue;

    ConnectedClient(SocketChannel socketChannel, int bufferSize) throws IOException{
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        readBuffer = ByteBuffer.allocateDirect(bufferSize);
        sendBuffer = ByteBuffer.allocateDirect(bufferSize);
        sendQueue = new ArrayDeque<>();

        //setup initial state for poll() function
        readBuffer.clear();
        readBuffer.limit(MESSAGE_HEADER_LENGTH_IN_BYTES);

        sendBuffer.clear();
        sendBuffer.limit(0);
    }

    boolean poll(List<Message> messageList){
        if(currentStatus == ClientStatus.CONNECTING){
            if(socketChannel.isConnected()){
                currentStatus = ClientStatus.RECEIVING_LENGTH;
            }else{
                return true;
            }
        }

        //handle disconnects
        if(!socketChannel.isConnected()){
            return false;
        }

        try{
            //read incoming messages
            int readAmount;
            while((readAmount = socketChannel.read(readBuffer)) > 0){
                if(!readBuffer.hasRemaining()){
                    readBuffer.flip();
                    switch(currentStatus){
                        case RECEIVING_LENGTH:{//just finished receiving a length
                            //get data
                            int messageLength = readBuffer.getInt();

                            //prepare for incoming data
                            readBuffer.clear();
                            readBuffer.limit(messageLength);
                            currentStatus = ClientStatus.RECEIVING_MESSAGE;
                        }
                        break;
                        case RECEIVING_MESSAGE:{ //just finished receiving a message
                            //get new data
                            byte[] messageData = new byte[readBuffer.limit()];
                            readBuffer.get(messageData, 0, messageData.length);
                            messageList.add(new Message(this, messageData));

                            //prepare for incoming data
                            readBuffer.clear();
                            readBuffer.limit(MESSAGE_HEADER_LENGTH_IN_BYTES); //4 bytes is hte size of an int, and the message length is an int
                            currentStatus = ClientStatus.RECEIVING_LENGTH;
                        }
                        break;
                        default:
                            throw new IOException("Unknown state!");
                    }
                }
            }
            if(readAmount < 0){
                return false;
            }

            //write outgoing messages
            outer:
            while(true){
                //send anything that's left until OS buffer fills
                while(sendBuffer.hasRemaining()){
                    int sent = socketChannel.write(sendBuffer);
                    if(sent == 0){
                        break outer;
                    }else if(sent < 0){
                        return false;
                    }
                }

                //setup new message
                if(sendQueue.isEmpty()){
                    break;
                }else{
                    sendBuffer.clear();
                    byte[] nextMessage = sendQueue.pop();
                    sendBuffer.putInt(nextMessage.length);
                    sendBuffer.put(nextMessage);
                    sendBuffer.flip();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    void shutdown() throws IOException{
        socketChannel.close();
    }

    /**
     * Schedules a message to be sent to this client. Note that the message will not actually be sent until appropriate
     * calls to <code>poll()</code>. An arbitrary but finite number of calls may be needed — it is guaranteed that the
     * messages will eventually be sent with repeated calls to <code>poll()</code>, but not after any particular
     * number.
     *
     * @param data The message to send
     */
    public void sendMessage(byte[] data){
        sendQueue.add(data);
    }

    private enum ClientStatus{
        CONNECTING,
        RECEIVING_LENGTH,
        RECEIVING_MESSAGE
    }

}
