package fr.loot1.quill.listeners;

import fr.loot1.quill.Quill;
import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Database database;
    private final Config config;

    public PlayerJoinListener(Quill quill) {
        this.database = quill.getDatabase();
        this.config = quill.getConfigManager();
    }

    @EventHandler
    private void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("quill.notify")) {
            ApplicationList applicationList = database.getApplicationsByStatus(true, false, false, false, 1, 0);
            int applicationCount = applicationList.getCount();
            if(applicationCount > 0) {
                player.sendMessage(config.getColored("messages.notification-on-join").replace("%count%", String.valueOf(applicationCount)));
            }
        }
    }

}