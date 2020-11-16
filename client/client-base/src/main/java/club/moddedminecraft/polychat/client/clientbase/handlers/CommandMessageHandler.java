package club.moddedminecraft.polychat.client.clientbase.handlers;

import club.moddedminecraft.polychat.client.clientbase.ClientApiBase;
import club.moddedminecraft.polychat.client.clientbase.CommandRunner;
import club.moddedminecraft.polychat.client.clientbase.PolychatClient;
import club.moddedminecraft.polychat.core.common.YamlConfig;
import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandMessageHandler {

    private final ClientApiBase clientApiBase;
    private final PolychatClient client;

    public CommandMessageHandler(ClientApiBase clientApiBase, PolychatClient client) {
        this.clientApiBase = clientApiBase;
        this.client = client;
        System.out.println("command message handler");
    }

    public int calculateParameters(String command) {
        Pattern pattern = Pattern.compile("(\\$\\d+)");
        Matcher matcher = pattern.matcher(command);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private void sendOutput(String command, String message, String channelId) {
        YamlConfig config = client.getConfig();
        int color = config.getOrDefault("color", 14);
        color = client.getHexColor(color);
        String id = config.getOrDefault("serverId", "ID");
        CommandProtos.GenericCommandResult result = CommandProtos.GenericCommandResult.newBuilder().setServerId(id)
                .setColour(String.format("#%06X", color)).setCommandOutput(message).setCommand(command).setDiscordChannelId(channelId).build();
        client.sendMessage(result);
    }

    private String parseCommand(CommandProtos.GenericCommand commandMessage) {
        // split args into list
        List<String> args = commandMessage.getArgsList();

        // Replaces default command with override if exists
        String command = client.getConfig().getOrDefault("overrides." + commandMessage.getDiscordCommandName(),
                commandMessage.getDefaultCommand());

        int commandArgs = calculateParameters(command);
        command = command.replace("$args", String.join(" ", args));

        if (args.size() < commandArgs) {
            sendOutput(
                    command,
                    "Error parsing command: Expected at least " + commandArgs + " parameters, received " + args.size(),
                    commandMessage.getDiscordChannelId()
            );
            return null;
        }

        // get the last instance of every unique $(number)
        // ie. /ranks set $1 $2 $1 $3 returns $2 $1 $3
        Pattern pattern = Pattern.compile("(\\$\\d+)(?!.*\\1)");
        Matcher matcher = pattern.matcher(command);

        while (matcher.find()) {
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String toBeReplaced = matcher.group(i);
                String replaceWith;
                int argNum = Integer.parseInt(toBeReplaced.substring(1));
                replaceWith = args.get(argNum - 1);
                command = command.replace(toBeReplaced, replaceWith);
            }
        }
        return command;
    }


    @EventHandler
    public void handle(CommandProtos.GenericCommand commandMessage, ConnectedClient author) {
        String command = parseCommand(commandMessage);
        CommandRunner runner = clientApiBase.getRunner(command);
        runner.run();
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        sendOutput(command, runner.getOutput(), commandMessage.getDiscordChannelId());
                    }
                },
                1000
        );
    }
}
