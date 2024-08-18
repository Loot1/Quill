package fr.loot1.quill.objects;

import fr.loot1.quill.Quill;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
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

    private final Database database;

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
        private String text;
        private Config config;
        private final String path;

        ApplicationStatus(int value, String path, Material material) {
            this.value = value;
            this.path = path;
            this.material = material;
        }

        public void initConfig(Config config) {
            this.config = config;
            this.text = config.getColored("settings.status." + path);
        }

        public int getValue() { return value; }

        public ItemStack getButton() {
            ItemStack button = new ItemStack(this.material);
            ItemMeta buttonMeta = button.getItemMeta();
            if (buttonMeta != null) {
                buttonMeta.setDisplayName(config.getColored("menus.application.items." + this.path + "-color") + this.text);
            }
            button.setItemMeta(buttonMeta);
            return button;
        }

        public String getText() { return text; }

        public static ApplicationStatus fromInteger(final Integer intfrom) {
            for (ApplicationStatus status : ApplicationStatus.values()) {
                if (status.value == intfrom) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value : " + intfrom);
        }
    }

    public int getId() { return id; }

    public String getDate() { return date; }

    public ApplicationStatus getStatus() { return status; }

    public String getTitle() { return title; }

    public Integer getPageNumber() { return pages.size(); }

    public OfflinePlayer getAuthor() { return player; }

    public boolean setStatus(ApplicationStatus newStatus) {
        boolean updated = database.updateApplicationStatus(id, newStatus.getValue());
        if(updated) {
            this.status = newStatus;
            return true;
        }
        return false;
    }

    public Application(ResultSet data, Quill quill) {
        this.database = quill.getDatabase();
        Config config = quill.getConfigManager();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
        try {
            this.id = data.getInt("id");
            UUID uuid = UUID.fromString(data.getString("uuid"));
            this.player = quill.getServer().getOfflinePlayer(uuid);
            this.date = dateFormat.format(data.getTimestamp("createdAt"));
            this.status = ApplicationStatus.fromInteger(data.getInt("status"));
            for (ApplicationStatus status : ApplicationStatus.values()) {
                status.initConfig(config);
            }
            this.title = data.getString("title");
            String content = data.getString("content");
            this.pages = database.decode(content);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void open(final Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if(bookMeta != null) {
            bookMeta.setTitle(title);
            bookMeta.setAuthor(player.getName());
            bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
            bookMeta.setPages(pages);
        }
        book.setItemMeta(bookMeta);
        player.openBook(book);
    }

}