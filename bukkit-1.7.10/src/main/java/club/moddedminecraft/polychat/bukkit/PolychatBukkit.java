package club.moddedminecraft.polychat.bukkit;

import club.moddedminecraft.polychat.core.messagelibrary.MessageLibraryExampleClass;
import club.moddedminecraft.polychat.core.networklibrary.NetworkLibraryExampleClass;
import org.bukkit.plugin.java.JavaPlugin;

public class PolychatBukkit extends JavaPlugin{

    @Override
    public void onDisable(){
        super.onDisable();
    }

    @Override
    public void onEnable(){
        MessageLibraryExampleClass messageLibraryExampleClass = new MessageLibraryExampleClass();
        NetworkLibraryExampleClass networkLibraryExampleClass = new NetworkLibraryExampleClass();
    }
}
