package fr.loot1.quill.objects;

import fr.loot1.quill.Quill;
import fr.loot1.quill.managers.ConfigManager;
import fr.loot1.quill.managers.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class Application {

    private final DatabaseManager databaseManager;

    private final int id;
    private final String date;
    private ApplicationStatus status;
    private final String title;
    private final List<String> pages;
    private final OfflinePlayer player;

    public enum ApplicationStatus {
        WAITING(0, "waiting", Material.CLOCK),
        ACCEPTED(1, "accepted", Material.SLIME_BALL),
        REFUSED(2, "refused", Material.MAGMA_CREAM),
        ARCHIVED(3, "archived", Material.BOOK);

        private final int value;
        private final Material material;
        private final String path;

        ApplicationStatus(int value, String path, Material material) {
            this.value = value;
            this.path = path;
            this.material = material;
        }

        public int getValue() { return value; }

        /** Crée le bouton GUI en lisant la config à chaque appel (reload-safe). */
        public ItemStack getButton(ConfigManager configManager) {
            String text = getText(configManager);
            ItemStack button = new ItemStack(this.material);
            ItemMeta buttonMeta = button.getItemMeta();
            if (buttonMeta != null) {
                buttonMeta.setDisplayName(
                        configManager.getColored("menus.application.items." + this.path + "-color") + text);
            }
            button.setItemMeta(buttonMeta);
            return button;
        }

        /** Retourne le libellé traduit du statut (reload-safe). */
        public String getText(ConfigManager configManager) {
            return configManager.getColored("settings.status." + path);
        }

        public static ApplicationStatus fromInteger(final Integer intfrom) {
            for (ApplicationStatus status : ApplicationStatus.values()) {
                if (status.value == intfrom) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value : " + intfrom);
        }    }

    public int getId() { return id; }

    public String getDate() { return date; }

    public ApplicationStatus getStatus() { return status; }

    public String getTitle() { return title; }

    public Integer getPageNumber() { return pages.size(); }

    public OfflinePlayer getAuthor() { return player; }

    public boolean setStatus(ApplicationStatus newStatus) {
        boolean updated = databaseManager.updateApplicationStatus(id, newStatus.getValue());
        if (updated) {
            this.status = newStatus;
            return true;
        }
        return false;
    }

    public Application(ResultSet data, Quill quill) {
        this.databaseManager = quill.getDatabaseManager();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
        try {
            this.id = data.getInt("id");
            UUID uuid = UUID.fromString(data.getString("uuid"));
            this.player = quill.getServer().getOfflinePlayer(uuid);
            this.date = dateFormat.format(data.getTimestamp("createdAt"));

            int statusInt = data.getInt("status");
            ApplicationStatus parsedStatus;
            try {
                parsedStatus = ApplicationStatus.fromInteger(statusInt);
            } catch (IllegalArgumentException e) {
                quill.getLogger().warning("Unknown status value " + statusInt
                        + " for application #" + this.id + ", defaulting to ARCHIVED.");
                parsedStatus = ApplicationStatus.ARCHIVED;
            }
            this.status = parsedStatus;

            this.title = data.getString("title");
            this.pages = databaseManager.decode(data.getString("content"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void open(final Player viewer) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setTitle(title);
            String authorName = player.getName();
            bookMeta.setAuthor(authorName != null ? authorName : "Unknown");
            bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
            bookMeta.setPages(pages);
        }
        book.setItemMeta(bookMeta);
        viewer.openBook(book);
    }

}