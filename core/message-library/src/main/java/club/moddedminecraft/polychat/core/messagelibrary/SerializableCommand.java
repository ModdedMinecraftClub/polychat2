package club.moddedminecraft.polychat.core.messagelibrary;

import java.util.ArrayList;

public class SerializableCommand implements ISerializableMessage {
    private String serverId;
    private String defaultCommand;
    private ArrayList<String> args;

    public SerializableCommand() {
    }

    public SerializableCommand(String serverId, String defaultCommand, ArrayList<String> args) {
        this.serverId = serverId;
        this.defaultCommand = defaultCommand;
        this.args = args;
    }


    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getDefaultCommand() {
        return defaultCommand;
    }

    public void setDefaultCommand(String defaultCommand) {
        this.defaultCommand = defaultCommand;
    }

    public ArrayList<String> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<String> args) {
        this.args = args;
    }
}
