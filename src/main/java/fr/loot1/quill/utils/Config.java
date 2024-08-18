package fr.loot1.quill.utils;

import java.io.File;
import java.util.List;

import fr.loot1.quill.Quill;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private final Quill main;
    private final File configFile;
    private final FileConfiguration config;

    public Config(Quill quill) {
        this.main = quill;
        this.configFile = new File(quill.getDataFolder(), "config.yml");
        this.config = quill.getConfig();
        init();
    }

    private void create() {
        main.saveDefaultConfig();
        main.getLogger().info("Configuration file added");
    }

    public void init() {
        if(!configFile.exists()) {
            create();
        }
    }

    public void reload() {
        if(configFile.exists()) {
            main.getLogger().info("Configuration file reloaded");
        } else {
            create();
        }
        main.reloadConfig();
    }

    public List<String> getColoredList(final String path) {
        if(this.config.contains(path)) {
            List<String> messages = this.config.getStringList(path);
            messages.replaceAll(msgToColor -> ChatColor.translateAlternateColorCodes('&', msgToColor));
            return messages;
        }
        main.getLogger().severe("This value doesn't exist in configuration file :" + path);
        return null;
    }

    public String getColored(final String path) {
        if(this.config.contains(path)) {
            return ChatColor.translateAlternateColorCodes('&', this.config.getString(path));
        }
        main.getLogger().severe("This value doesn't exist in configuration file :" + path);
        return "";
    }

    public String get(final String path) {
        if(this.config.contains(path)) {
            return this.config.getString(path);
        }
        main.getLogger().severe("This value doesn't exist in configuration file :" + path);
        return null;
    }

}