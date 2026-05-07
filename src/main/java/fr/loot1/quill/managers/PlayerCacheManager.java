package fr.loot1.quill.managers;

import fr.loot1.quill.Quill;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCacheManager implements Listener {

    private final Set<String> cachedPlayerNames = ConcurrentHashMap.newKeySet();

    public PlayerCacheManager(Quill quill) {
        for (OfflinePlayer offlinePlayer : quill.getServer().getOfflinePlayers()) {
            if (offlinePlayer.getName() != null) {
                cachedPlayerNames.add(offlinePlayer.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cachedPlayerNames.add(event.getPlayer().getName());
    }

    public List<String> getCachedPlayerNames() {
        return new ArrayList<>(cachedPlayerNames);
    }

}