package fr.loot1.quill.utils;

import java.io.File;
import java.util.List;

import fr.loot1.quill.Quill;
import org.bukkit.ChatColor;

public class Config {

    final static Quill main = Quill.getInstance();
    final static File configFile = new File(main.getDataFolder(), "config.yml");

    private static void create() {
        main.saveDefaultConfig();
        main.getLogger().info("Configuration file added");
    }

    public static void init() {
        if(!configFile.exists()) {
            create();
        }
    }

    public static void reload() {
        if(configFile.exists()) {
            main.getLogger().info("Configuration file reloaded");
        } else {
            create();
        }
        main.reloadConfig();
    }

    public static List<String> getColoredList(final String path) {
        if(main.getConfig().contains(path)) {
            List<String> messages = main.getConfig().getStringList(path);
            messages.replaceAll(msgToColor -> ChatColor.translateAlternateColorCodes('&', msgToColor));
            return messages;
        }
        main.getLogger().severe("This value doesn't exist in configuration file :" + path);
        return null;
    }

    public static String getColored(final String path) {
        if(main.getConfig().contains(path)) {
            return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(path));
        }
        main.getLogger().severe("This value doesn't exist in configuration file :" + path);
        return "";
    }

    public static String get(final String path) {
        if(main.getConfig().contains(path)) {
            return main.getConfig().getString(path);
        }
        main.getLogger().severe("This value doesn't exist in configuration file :" + path);
        return null;
    }

}