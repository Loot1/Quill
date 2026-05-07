package fr.loot1.quill;

import fr.loot1.quill.commands.QuillCommand;
import fr.loot1.quill.listeners.GuiListener;
import fr.loot1.quill.listeners.PlayerJoinListener;
import fr.loot1.quill.managers.ConfigManager;
import fr.loot1.quill.managers.DatabaseManager;
import fr.loot1.quill.managers.PlayerCacheManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.dependency.Library;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.plugin.java.annotation.plugin.author.Authors;

@Plugin(name = "Quill", version = "1.0.0")
@Description("Manages player applications submitted as signed books")
@Authors(@Author("Loot1"))
@LogPrefix("Quill")
@ApiVersion(ApiVersion.Target.v1_20)
@Library("com.zaxxer:HikariCP:7.0.2")

public class Quill extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerCacheManager playerCacheManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        try {
            this.databaseManager = new DatabaseManager(this);
        } catch (Exception e) {
            getLogger().severe("Failed to connect to the database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.playerCacheManager = new PlayerCacheManager(this);

        int pluginId = 31189;
        Metrics metrics = new Metrics(this, pluginId);

        getCommand("quill").setExecutor(new QuillCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(playerCacheManager, this);
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public PlayerCacheManager getPlayerCacheManager() {
        return this.playerCacheManager;
    }

}