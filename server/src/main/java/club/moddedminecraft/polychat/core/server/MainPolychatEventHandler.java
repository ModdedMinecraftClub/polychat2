package club.moddedminecraft.polychat.core.server;

import club.moddedminecraft.polychat.core.messagelibrary.ChatProtos;
import club.moddedminecraft.polychat.core.networklibrary.Message;

public final class MainPolychatEventHandler {

    @EventHandler
    public void handleChatMessage(ChatProtos.ChatMessage message, Message rawMessage) {
        System.out.println(message);
        System.out.println(rawMessage.getFrom());
    }

}
