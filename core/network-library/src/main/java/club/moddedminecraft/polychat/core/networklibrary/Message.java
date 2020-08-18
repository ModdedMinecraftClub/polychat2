package club.moddedminecraft.polychat.core.networklibrary;

/**
 * This class represents a message that was received. A message contains the <code>ConnectedClient</code> that it was
 * received through (This value is meaningless and may or may not be null when outside of a server process.) and the
 * data in that message.
 */
public final class Message{
    private final ConnectedClient from;
    private final byte[] data;

    Message(ConnectedClient from, byte[] data){
        this.from = from;
        this.data = data;
    }

    /**
     * Gets the <code>ConnectedClient</code> instance that this message was received through (This value is meaningless
     * and may or may not be null when outside of a server process.).
     *
     * @return The <code>ConnectedClient</code> instance that this message was recieved through.
     */
    public ConnectedClient getFrom(){
        return from;
    }

    /**
     * Gets the data of this message. This byte array is identical to the one that was sent in a remote process.
     *
     * @return The data of this message
     */
    public byte[] getData(){
        return data;
    }

}
