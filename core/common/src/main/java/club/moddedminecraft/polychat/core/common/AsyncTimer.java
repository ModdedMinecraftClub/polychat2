package club.moddedminecraft.polychat.core.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;

public final class AsyncTimer{
    private final ArrayList<TimerEntry> timerEntries = new ArrayList<>();

    public void poll(){
        long currentTime = System.currentTimeMillis();
        for(TimerEntry entry : timerEntries){
            entry.runIfRequired(currentTime);
        }
    }

    public void registerFunction(Runnable runnable, long intervalInMilliseconds){
        timerEntries.add(new TimerEntry(runnable, intervalInMilliseconds));
    }

    public void registerFunctionsViaAnnotations(Object object){
        Class<? extends Object> clazz = object.getClass();
        for(Method method : clazz.getDeclaredMethods()){
            TimedFunction annotation = method.getAnnotation(TimedFunction.class);
            if(annotation != null){
                if(annotation.interval() < 0){
                    throw new RuntimeException("You must specify a non-negative interval with the TimerEntry annotation.");
                }
                registerFunction(() -> {
                    try{
                        method.invoke(object);
                    }catch(Throwable t){
                        throw new RuntimeException("Timer threw an exception", t);
                    }
                }, annotation.interval());
            }
        }
    }

    private static final class TimerEntry{
        private final Runnable runnable;
        private final long interval;
        private long nextRun = 0;

        public TimerEntry(Runnable runnable, long interval){
            this.runnable = runnable;
            this.interval = interval;
        }

        public void runIfRequired(long currentTime){
            if(currentTime > nextRun){
                runnable.run();
                nextRun = currentTime+interval;
            }
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface TimedFunction{
        long interval();
    }

}
