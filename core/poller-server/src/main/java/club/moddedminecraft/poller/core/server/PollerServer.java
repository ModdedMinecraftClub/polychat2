package club.moddedminecraft.poller.core.server;

import net.dv8tion.jda.api.JDABuilder;

public final class PollerServer{

    public static void main(String[] args){
        JDABuilder builder = JDABuilder.createDefault(args[0]);
    }

}
