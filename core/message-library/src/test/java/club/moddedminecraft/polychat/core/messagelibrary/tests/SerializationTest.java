package club.moddedminecraft.polychat.core.messagelibrary.tests;

import club.moddedminecraft.polychat.core.messagelibrary.SerializationUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;

public class SerializationTest{

    private static class ExampleMessage implements Serializable{
        private int exampleInt;
        private String exampleString;
        private InnerExampleMessage innerExampleMessage;

        public ExampleMessage(int exampleInt, String exampleString, InnerExampleMessage innerExampleMessage){
            this.exampleInt = exampleInt;
            this.exampleString = exampleString;
            this.innerExampleMessage = innerExampleMessage;
        }

        public int getExampleInt(){
            return exampleInt;
        }

        public void setExampleInt(int exampleInt){
            this.exampleInt = exampleInt;
        }

        public String getExampleString(){
            return exampleString;
        }

        public void setExampleString(String exampleString){
            this.exampleString = exampleString;
        }

        public InnerExampleMessage getInnerExampleMessage(){
            return innerExampleMessage;
        }

        public void setInnerExampleMessage(InnerExampleMessage innerExampleMessage){
            this.innerExampleMessage = innerExampleMessage;
        }
    }

    private static class InnerExampleMessage implements Serializable{
        private String anotherExampleString;

        public InnerExampleMessage(String anotherExampleString){
            this.anotherExampleString = anotherExampleString;
        }

        public String getAnotherExampleString(){
            return anotherExampleString;
        }

        public void setAnotherExampleString(String anotherExampleString){
            this.anotherExampleString = anotherExampleString;
        }

    }

    @Test
    public void testEncodeThenDecode(){
        //create initial instance
        ExampleMessage initialInstance = new ExampleMessage(
                10,
                "This is an example string.",
                new InnerExampleMessage(
                        "This is another example string"
                )
        );

        //encode to byte array
        byte[] serializedData = SerializationUtilities.serializableClassToByteArray(initialInstance);

        //decode to secondary instance
        ExampleMessage secondaryInstance = SerializationUtilities.deserializeClassFromByteArray(serializedData);

        //compare initial and secondary instances
        assertEquals(initialInstance.getExampleInt(), secondaryInstance.getExampleInt());
        assertEquals(initialInstance.getExampleString(), secondaryInstance.getExampleString());
        assertEquals(initialInstance.getInnerExampleMessage().getAnotherExampleString(), secondaryInstance.getInnerExampleMessage().getAnotherExampleString());
    }


}
