package club.moddedminecraft.polychat.server.services;

/**
 *  Represents a service that runs every x ticks.
 */
public abstract class TimedTickableService implements TickableService {

    /**
     * Amount of ticks between each run.
     */
    private final int intervalInTicks;

    private int timer;

    /**
     * Default ctor.
     *
     * @param intervalInTicks Amount of ticks between each run.
     */
    protected TimedTickableService(int intervalInTicks) {
        this.intervalInTicks = intervalInTicks;
        this.timer = 0;
    }

    @Override
    public void tick() {
        if (timer == intervalInTicks) {
            onRun();
            timer = 0;
        } else {
            timer += 1;
        }
    }


    /**
     *  Triggered every {@link TimedTickableService#intervalInTicks} ticks.
     */
    public void onRun() {}
}
