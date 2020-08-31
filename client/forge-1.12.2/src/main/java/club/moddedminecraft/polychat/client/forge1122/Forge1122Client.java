package club.moddedminecraft.polychat.client.forge1122;

import club.moddedminecraft.polychat.client.clientbase.ClientBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class Forge1122Client implements ClientBase {

    private final MinecraftServer server;

    public Forge1122Client(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void sendChatMessage(String message) {
        ITextComponent string = new TextComponentString(message);
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            player.sendMessage(string);
        }
        server.sendMessage(string);
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

}
