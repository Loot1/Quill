package fr.loot1.quill;

import fr.loot1.quill.commands.QuillCommand;
import fr.loot1.quill.listeners.GuiListener;
import fr.loot1.quill.listeners.PlayerJoinListener;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import org.bukkit.plugin.java.JavaPlugin;

public class Quill extends JavaPlugin {

    private static Quill instance;

    public static Quill getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        Config.init();

        getCommand("quill").setExecutor(new QuillCommand());
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        Database.connect();
    }

    @Override
    public void onDisable() {
        Database.disconnect();
    }

}
