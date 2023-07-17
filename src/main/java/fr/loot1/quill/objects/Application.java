package fr.loot1.quill.objects;

import fr.loot1.quill.Quill;
import fr.loot1.quill.utils.Database;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class Application {

    final static Quill main = Quill.getInstance();

    private final String date;
    private final int status;
    private final String title;
    private final List<String> pages;

    private final OfflinePlayer player;

    public OfflinePlayer getAuthor() { return player; }

    public String getDate() { return date; }

    public Integer getStatus() { return status; }

    public String getTitle() { return title; }

    public Integer getPageNumber() { return pages.size(); }

    public Application(ResultSet data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
        try {
            UUID uuid = UUID.fromString(data.getString("uuid"));
            this.player = main.getServer().getOfflinePlayer(uuid);
            this.date = dateFormat.format(data.getTimestamp("createdAt"));
            this.status = data.getInt("status");
            this.title = data.getString("title");
            String content = data.getString("content");
            this.pages = Database.decode(content);
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
