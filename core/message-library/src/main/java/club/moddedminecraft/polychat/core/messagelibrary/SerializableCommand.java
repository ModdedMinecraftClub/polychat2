package club.moddedminecraft.polychat.core.messagelibrary;

import java.util.List;

public class SerializableCommand implements ISerializableMessage {
    private String serverId;
    private String defaultCommand;
    private List<String> args;

    public SerializableCommand() {
    }

    public SerializableCommand(String serverId, String defaultCommand, List<String> args) {
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

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}
