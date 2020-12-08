package club.moddedminecraft.polychat.core.common.tests;

import club.moddedminecraft.polychat.core.common.AsyncTimer;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTimerTest{

    @Test
    public void asyncTimerTest(){
        try{
            AtomicInteger runCount = new AtomicInteger();

            assertEquals(runCount.get(), 0);

            AsyncTimer asyncTimer = new AsyncTimer();
            asyncTimer.registerFunction(runCount::incrementAndGet, 1000);
            asyncTimer.poll(); //run it once to set runCount to 1

            Thread.sleep(500);
            asyncTimer.poll();
            assertEquals(runCount.get(), 1);

            Thread.sleep(600); //total of about 1100 ms sleeping
            asyncTimer.poll();
            assertEquals(runCount.get(), 2);
        }catch(InterruptedException e){
            throw new RuntimeException("The test has failed because it was interrupted.", e);
        }
    }

}
