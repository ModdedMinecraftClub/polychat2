package club.moddedminecraft.polychat.client.clientbase;

import java.util.ArrayList;

public abstract class CommandRunner {

    protected final ArrayList<String> output = new ArrayList<>();

    public String getOutput() {
        StringBuilder commandOutput = new StringBuilder();
        for (String output : output) {
            commandOutput.append(output).append("\n");
        }
        return commandOutput.toString();
    }

    abstract public void run();
}
