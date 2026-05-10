package fr.loot1.quill.managers;

import java.io.File;
import java.util.List;

import fr.loot1.quill.Quill;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Quill main;
    private final File configFile;
    private FileConfiguration config;

    public ConfigManager(Quill quill) {
        this.main = quill;
        this.configFile = new File(quill.getDataFolder(), "config.yml");
        init();
    }

    private void create() {
        main.saveDefaultConfig();
        main.getLogger().info("Configuration file added");
    }

    public void init() {
        if (!configFile.exists()) {
            create();
        }
        this.config = main.getConfig();
    }

    public void reload() {
        if (configFile.exists()) {
            main.getLogger().info("Configuration file reloaded");
        } else {
            create();
        }
        main.reloadConfig();
        this.config = main.getConfig();
    }

    public List<String> getColoredList(final String path) {
        if (config.contains(path)) {
            List<String> messages = config.getStringList(path);
            messages.replaceAll(msgToColor -> ChatColor.translateAlternateColorCodes('&', msgToColor));
            return messages;
        }
        main.getLogger().severe("This value doesn't exist in configuration file: " + path);
        return null;
    }

    public String getColored(final String path) {
        if (config.contains(path)) {
            return ChatColor.translateAlternateColorCodes('&', config.getString(path));
        }
        main.getLogger().severe("This value doesn't exist in configuration file: " + path);
        return "";
    }

    public String get(final String path) {
        if (config.contains(path)) {
            return config.getString(path);
        }
        main.getLogger().severe("This value doesn't exist in configuration file: " + path);
        return null;
    }

}