package club.moddedminecraft.polychat.core.server;

import club.moddedminecraft.polychat.core.messagelibrary.MessageLibraryExampleClass;
import net.dv8tion.jda.api.JDABuilder;

public final class PolychatServer{

    public static void main(String[] args){
        MessageLibraryExampleClass messageLibraryExampleClass = new MessageLibraryExampleClass();
        NetworkLibraryExampleClass networkLibraryExampleClass = new NetworkLibraryExampleClass();
        JDABuilder builder = JDABuilder.createDefault(args[0]);
    }

}
