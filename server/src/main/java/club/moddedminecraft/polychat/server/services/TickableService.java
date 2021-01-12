package club.moddedminecraft.polychat.server.services;

/**
 * Represents a service that's ran on every tick.
 */
public interface TickableService {
    /**
     * Triggered on every tick.
     */
    void tick();
}
