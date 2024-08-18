package fr.loot1.quill;

import fr.loot1.quill.commands.QuillCommand;
import fr.loot1.quill.listeners.GuiListener;
import fr.loot1.quill.listeners.PlayerJoinListener;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import fr.loot1.quill.utils.PlayerCacheManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Quill extends JavaPlugin {

    private Config configManager;
    private Database database;
    private PlayerCacheManager playerCacheManager;

    @Override
    public void onEnable() {
        this.configManager = new Config(this);
        this.database = new Database(this);
        this.playerCacheManager = new PlayerCacheManager(this);

        getCommand("quill").setExecutor(new QuillCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(playerCacheManager, this);
    }

    @Override
    public void onDisable() {
        this.database.disconnect();
    }

    public Config getConfigManager() {
        return this.configManager;
    }

    public Database getDatabase() {
        return this.database;
    }

    public PlayerCacheManager getPlayerCacheManager() {
        return this.playerCacheManager;
    }

}
