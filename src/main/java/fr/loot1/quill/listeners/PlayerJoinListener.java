package fr.loot1.quill.listeners;

import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    private void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("quill.notify")) {
            ApplicationList applicationList = Database.getApplicationsByStatus(true, false, false, false, 1, 0);
            int applicationCount = applicationList.getCount();
            if(applicationCount > 0) {
                player.sendMessage(Config.getColored("messages.notification-on-join").replace("%count%", String.valueOf(applicationCount)));
            }
        }
    }

}