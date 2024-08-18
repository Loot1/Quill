package fr.loot1.quill.utils;

import fr.loot1.quill.Quill;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerCacheManager implements Listener {

    private final List<String> cachedPlayerNames = new ArrayList<>();

    public PlayerCacheManager(Quill quill) {
        for (OfflinePlayer offlinePlayer : quill.getServer().getOfflinePlayers()) {
            if (offlinePlayer.getName() != null) {
                cachedPlayerNames.add(offlinePlayer.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        if (!cachedPlayerNames.contains(playerName)) {
            cachedPlayerNames.add(playerName);
        }
    }

    public List<String> getCachedPlayerNames() {
        return cachedPlayerNames;
    }

}