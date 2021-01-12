package club.moddedminecraft.polychat.server.services;

/**
 * Represents a service that's ran on every tick.
 */
public interface ITickedService {
    /**
     * Triggered when the application starts the service.
     */
    void start();

    /**
     * Triggered on every tick.
     */
    void tick();
}
