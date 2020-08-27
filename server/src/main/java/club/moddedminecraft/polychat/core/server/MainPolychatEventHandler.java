package club.moddedminecraft.polychat.core.server;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;

public final class MainPolychatEventHandler {

    @EventHandler
    public void handleChatMessage(ChatProtos.ChatMessage message) {
        System.out.println(message);
    }

}
