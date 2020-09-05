package club.moddedminecraft.polychat.client.clientbase;

public class OnlinePlayerThread {

    private static final long SLEEP_TIME_SECONDS = 5 * 60;
    private final PolychatClient client;
    private final Thread thread;

    public OnlinePlayerThread(PolychatClient client) {
        this.client = client;
        this.thread = new Thread(this::run);
    }

    public void start() {
        this.thread.start();
    }

    public void interrupt() {
        this.thread.interrupt();
    }

    private void run() {
        while (true) {
            try {
                client.sendPlayers();
                Thread.sleep(SLEEP_TIME_SECONDS * 1000);
            } catch (InterruptedException ignored) {
            }
        }

    }

}
