package club.moddedminecraft.polychat.server.discordcommands;

import club.moddedminecraft.polychat.server.OnlineServer;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

public class OnlineCommand extends Command {

    private final HashMap<String, OnlineServer> onlineServers;

    public OnlineCommand(HashMap<String, OnlineServer> onlineServers) {
        // command info;
        this.name = "online";
        this.help = "Returns amount of players online";
        this.guildOnly = true;

        // deps;
        this.onlineServers = onlineServers;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (onlineServers.isEmpty()) {
            EmbedBuilder errEb = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("No online servers found!")
                    .setColor(Color.RED);
            event.reply(errEb.build());
            return;
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Online players")
                .setColor(Color.GREEN);

        int totalOnline = 0;
        for (OnlineServer server : onlineServers.values()) {
            totalOnline += server.getPlayersOnline();

            String mainMsg = "[" + server.getServerId() + "] " + server.getServerName() + " [" + server.getPlayersOnline() + "/" + server.getMaxPlayers() + "]";
            String secondaryMsg = "*" + server.getServerAddress() + "*";
            List<String> onlinePlayersOnCurrentServer = server.getOnlinePlayerNames();

            if (onlinePlayersOnCurrentServer.size() != 0) {
                secondaryMsg += "\n" + String.join(", ", onlinePlayersOnCurrentServer);
            }

            eb.addField(mainMsg, secondaryMsg, false);
        }

        String description = "**Total players online:** " + totalOnline + "\n**Servers online:** " + onlineServers.keySet().size();
        eb.setDescription(description);

        event.reply(eb.build());
    }
}
