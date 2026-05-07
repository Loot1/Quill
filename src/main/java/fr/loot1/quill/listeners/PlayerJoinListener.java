package fr.loot1.quill.listeners;

import fr.loot1.quill.Quill;
import fr.loot1.quill.managers.ConfigManager;
import fr.loot1.quill.managers.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Quill main;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public PlayerJoinListener(Quill quill) {
        this.main = quill;
        this.databaseManager = quill.getDatabaseManager();
        this.configManager = quill.getConfigManager();
    }

    @EventHandler
    private void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("quill.notify")) return;
        main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            int count = databaseManager.countWaitingApplications();
            if (count > 0) {
                main.getServer().getScheduler().runTask(main, () -> {
                    if (player.isOnline()) {
                        player.sendMessage(configManager.getColored("messages.notification-on-join")
                                .replace("%count%", String.valueOf(count)));
                    }
                });
            }
        });
    }

}