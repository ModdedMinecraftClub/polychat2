package club.moddedminecraft.polychat.core.networklibrary.tests;

import club.moddedminecraft.polychat.core.networklibrary.Client;
import club.moddedminecraft.polychat.core.networklibrary.Message;
import club.moddedminecraft.polychat.core.networklibrary.Server;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SendReceiveTest{

    @Test
    public void sendReceiveTest() throws IOException, InterruptedException{
        byte[] exampleByteArray = "This is a test string.".getBytes(StandardCharsets.UTF_8);

        Server server = new Server(16384, 1024);
        server.poll(); //make sure that the socket is bound

        Client client = new Client("localhost", 16384, 1024);

        //from client to server
        client.sendMessage(exampleByteArray);
        long loopStart = System.currentTimeMillis();
        List<Message> receivedMessages;
        while((receivedMessages = server.poll()).size() < 1){
            client.poll(); //needed to make sure that the client moves along too
            if(System.currentTimeMillis()-loopStart > 10000){
                throw new IOException("Unit test fails because message did not get through after 10 seconds.");
            }
        }
        assertEquals(1, receivedMessages.size());
        assertArrayEquals(exampleByteArray, receivedMessages.get(0).getData());

        //from server to client
        byte[] exampleByteArray2 = "This is another test string.".getBytes(StandardCharsets.UTF_8);
        server.broadcastMessage(exampleByteArray2);
        while((receivedMessages = client.poll()).size() < 1){
            server.poll();
            if(System.currentTimeMillis()-loopStart > 10000){
                throw new IOException("Unit test fails because message did not get through after 10 seconds.");
            }
        }
        assertEquals(1, receivedMessages.size());
        assertArrayEquals(exampleByteArray2, receivedMessages.get(0).getData());
    }

}
