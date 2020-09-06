package club.moddedminecraft.polychat.forge116;

import club.moddedminecraft.polychat.client.clientbase.ClientBase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class Forge116Client implements ClientBase {

    private final MinecraftServer server;

    public Forge116Client(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void sendChatMessage(String message) {
        ITextComponent string = new StringTextComponent(message);
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            player.sendMessage(string, player.getUniqueID());
        }
        server.sendMessage(string, UUID.randomUUID());
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<>();
        Collections.addAll(players, server.getPlayerList().getOnlinePlayerNames());
        return players;
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

}
