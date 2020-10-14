package club.moddedminecraft.polychat.core.server.handlers;

import club.moddedminecraft.polychat.core.messagelibrary.CommandProtos;
import club.moddedminecraft.polychat.core.messagelibrary.EventHandler;
import club.moddedminecraft.polychat.core.networklibrary.ConnectedClient;
import club.moddedminecraft.polychat.core.server.OnlineServer;
import com.google.protobuf.Any;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.HashMap;

public final class PromoteMemberCommandHandler {
    private final TextChannel generalChannel;
    private final HashMap<String, OnlineServer> onlineServers;

    public PromoteMemberCommandHandler(TextChannel generalChannel, HashMap<String, OnlineServer> onlineServers) {
        this.generalChannel = generalChannel;
        this.onlineServers = onlineServers;
    }

    @EventHandler
    public void handle(CommandProtos.PromoteMemberCommand msg, ConnectedClient author) {
        String serverId = msg.getServerId();
        OnlineServer server = onlineServers.get(serverId);

        if (server != null) {
            CommandProtos.GenericCommand command = CommandProtos.GenericCommand.newBuilder()
                    .setDiscordChannelId(generalChannel.getId())
                    .setDiscordCommandName("promote")
                    .setDefaultCommand("ranks add")
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
}
