package club.moddedminecraft.polychat.core.server;

import java.awt.*;
import java.util.HashMap;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.core.messagelibrary.ServerProtos;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.networklibrary.Message;

public class PolychatMessage {
    private final static Logger logger = LoggerFactory.getLogger(PolychatMessage.class);

    private final JDA jda;
    private final TextChannel generalChannel;
    private final HashMap<String, OnlineServer> onlineServers;
    private final Message message;
    private final ConnectedClient author;

    private Any packedProtoMessage;

    public PolychatMessage(JDA jda, TextChannel generalChannel, HashMap<String, OnlineServer> onlineServers, Message message) {
        this.jda = jda;
        this.generalChannel = generalChannel;
        this.onlineServers = onlineServers;
        this.message = message;
        this.author = message.getFrom();
    }

    public void handleMessage() {
        try {
            packedProtoMessage = Any.parseFrom(message.getData());

            if (packedProtoMessage.is(ChatProtos.ChatMessage.class)) {
                handleChatMessage();
            } else if (packedProtoMessage.is(ServerProtos.ServerPlayerStatusChangedEvent.class)) {
                handlePlayerStatusChangedEventMessage();
            } else if (packedProtoMessage.is(CommandProtos.GenericCommandResult.class)) {
                handleGenericCommandResultMessage();
            } else if (packedProtoMessage.is(ServerProtos.ServerPlayersOnline.class)) {
                handlePlayersOnlineMessage();
            } else if (packedProtoMessage.is(CommandProtos.PromoteMemberCommand.class)) {
                handlePromoteMemberCommand();
            } else if (packedProtoMessage.is(ServerProtos.ServerInfo.class)) {
                handleServerInfoMessage();
            } else if (packedProtoMessage.is(ServerProtos.ServerStatus.class)) {
                handleServerStatusMessage();
            } else {
                logger.warn("Unrecognized message received.");
            }

        } catch (InvalidProtocolBufferException e) {
            logger.error("Failed to parse/unpack message.", e);
        }
    }

    private void handleServerInfoMessage() throws InvalidProtocolBufferException {
        ServerProtos.ServerInfo msg = packedProtoMessage.unpack(ServerProtos.ServerInfo.class);
        // if new add to list;
        OnlineServer server = onlineServers.get(msg.getServerId());
        if (server == null) {
            OnlineServer newServer = new OnlineServer(msg, author);
            onlineServers.put(msg.getServerId(), newServer);
        }
    }

    private void handleServerStatusMessage() throws InvalidProtocolBufferException {
        ServerProtos.ServerStatus msg = packedProtoMessage.unpack(ServerProtos.ServerStatus.class);
        String serverId = msg.getServerId();
        OnlineServer server = onlineServers.get(serverId);
        ServerProtos.ServerStatus.ServerStatusEnum status = msg.getStatus();

        if (server != null) {
            if (status == ServerProtos.ServerStatus.ServerStatusEnum.CRASHED || status == ServerProtos.ServerStatus.ServerStatusEnum.STOPPED) {
                onlineServers.remove(serverId);
            }
        }

        if (server == null && status == ServerProtos.ServerStatus.ServerStatusEnum.STARTED) {
            logger.error("Server with id \""
                    + serverId
                    + "\" has unexpectedly sent ServerStatus message before sending ServerInfo message.");
        }
    }

    private void handlePlayersOnlineMessage() throws InvalidProtocolBufferException {
        ServerProtos.ServerPlayersOnline msg = packedProtoMessage.unpack(ServerProtos.ServerPlayersOnline.class);
        String serverId = msg.getServerId();
        OnlineServer server = onlineServers.get(serverId);

        if (server != null) {
            server.updatePlayersInfo(msg);
        } else {
            logger.error("Server with id \""
                    + serverId
                    + "\" has unexpectedly sent ServerPlayersOnline message despite not being marked as online. Have you sent ServerInfo message on server startup?");
        }
    }

    private void handlePlayerStatusChangedEventMessage() throws InvalidProtocolBufferException {
        ServerProtos.ServerPlayerStatusChangedEvent msg = packedProtoMessage.unpack(ServerProtos.ServerPlayerStatusChangedEvent.class);
        String serverId = msg.getNewPlayersOnline().getServerId();
        OnlineServer server = onlineServers.get(serverId);

        if (server != null) {
            server.updatePlayersInfo(msg.getNewPlayersOnline());
            ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus playerStatus = msg.getNewPlayerStatus();
            String playerUsername = msg.getPlayerUsername();
            String discordMessage;
            if (playerStatus == ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.JOINED) {
                discordMessage = server.createServerChatMessage(playerUsername + " has joined the game");
                generalChannel.sendMessage(discordMessage).queue();
            } else if (playerStatus == ServerProtos.ServerPlayerStatusChangedEvent.PlayerStatus.LEFT) {
                discordMessage = server.createServerChatMessage(playerUsername + " has left the game");
                generalChannel.sendMessage(discordMessage).queue();
            } else {
                logger.error("Server with id \""
                        + serverId
                        + "\" sent an unrecognized PlayerStatus");
            }
        } else {
            logger.error("Server with id \""
                    + serverId
                    + "\" has unexpectedly sent ServerPlayerStatusChangedEvent message despite not being marked as online. Have you sent ServerInfo message on server startup?");
        }
    }

    private void handlePromoteMemberCommand() throws InvalidProtocolBufferException {
        CommandProtos.PromoteMemberCommand msg = packedProtoMessage.unpack(CommandProtos.PromoteMemberCommand.class);
        String serverId = msg.getServerId();
        OnlineServer server = onlineServers.get(serverId);

        if (server != null) {
            CommandProtos.GenericCommand command = CommandProtos.GenericCommand.newBuilder()
                    .setDiscordChannelId(generalChannel.getId())
                    .setDiscordCommandName("!promote")
                    .setDefaultCommand("/ranks add") // TODO: ask 132 if that's the right format;
                    .setArgs(msg.getUsername() + " member")
                    .build();
            Any any = Any.pack(command);
            server.getClient().sendMessage(any.toByteArray());
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Member bot requested promoting a member on a server that isn't online and/or doesn't exist")
                    .setColor(Color.RED);
            generalChannel.sendMessage(eb.build()).queue();
        }
    }

    private void handleGenericCommandResultMessage() throws InvalidProtocolBufferException {
        CommandProtos.GenericCommandResult msg = packedProtoMessage.unpack(CommandProtos.GenericCommandResult.class);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Command executed")
                .setColor(Color.BLUE) // TODO: ask 132 for format colour comes in;
                .addField("Server", msg.getServerId(), false)
                .addField("Command output", msg.getCommandOutput(), false);
        TextChannel channelCmdOriginatedFrom = jda.getTextChannelById(msg.getDiscordChannelId());

        if (channelCmdOriginatedFrom != null) {
            channelCmdOriginatedFrom.sendMessage(eb.build()).queue();
        } else {
            logger.error("Client told me a command was sent from a Discord channel that doesn't exist.");
        }
    }

    private void handleChatMessage() throws InvalidProtocolBufferException {
        ChatProtos.ChatMessage msg = packedProtoMessage.unpack(ChatProtos.ChatMessage.class);
        String discordMsg = "`"
                + "[" + msg.getServerId() + "] "
                + "[" + msg.getMessageAuthorRank() + "] "
                + msg.getMessageAuthor() + ": "
                + "`"
                + msg.getMessageContent();
        generalChannel.sendMessage(discordMsg).queue();
    }
}
