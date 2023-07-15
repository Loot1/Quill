package fr.loot1.quill.guis;

import fr.loot1.quill.objects.ArchivedBook;
import fr.loot1.quill.objects.ArchivedBooksList;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class GuiArchives extends GuiHolder {

    final static int archivedBooksPerPage = 45;
    private int archivedBooksCount;

    protected Inventory inventory;
    protected int page = 0;

    private List<ArchivedBook> archivedBooks;

    HashMap<Integer, ArchivedBook> clickableArchivedBooks = new HashMap<>();

    @Override
    public @NotNull Inventory getInventory() {
        inventory.clear();

        int maxPages = (int) Math.ceil((double) archivedBooksCount / archivedBooksPerPage);
        page = Math.min(maxPages, page);

        for (int i = 0; i < archivedBooks.size(); i++) {
            ArchivedBook toDisplayBook = archivedBooks.get(i);
            OfflinePlayer author = toDisplayBook.getAuthor();
            inventory.setItem(i, headGui(
                    author,
                    Config.getColored("menus.all-players.items.player-head.name").replace("%player%", author.getName()).replace("%title%", toDisplayBook.getTitle()),
                    formatLore(Config.getColoredList("menus.all-players.items.player-head.lore"), toDisplayBook)
            ));
            clickableArchivedBooks.put(i, toDisplayBook);
        }

        if (page > 0) {
            inventory.setItem(47, itemGui(Material.ARROW, Config.getColored("menus.global.items.previous-page")));
        }
        inventory.setItem(49, itemGui(Material.BARRIER, Config.getColored("menus.global.items.close")));
        if (page < maxPages - 1) {
            inventory.setItem(51, itemGui(Material.ARROW, Config.getColored("menus.global.items.next-page")));
        }

        return inventory;
    }

    public GuiArchives(final Player playerWhoClicked, final ArchivedBooksList archivedBooksList) {
        archivedBooksCount = archivedBooksList.getCount();
        archivedBooks = archivedBooksList.getData();
        inventory = Bukkit.createInventory(this, 54, Config.getColored("menus.all-players.title").replace("%size%", String.valueOf(archivedBooksCount)));
        playerWhoClicked.openInventory(getInventory());
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Material clickedMaterial = event.getCurrentItem().getType();

        switch (clickedMaterial) {
            case PLAYER_HEAD:
                clickableArchivedBooks.get(event.getSlot()).open(player);
                break;
            case BARRIER:
                player.closeInventory();
                break;
            case ARROW:
                if(event.getSlot() == 47) {
                    page--;
                } else {
                    page++;
                }
                ArchivedBooksList archivedBooksList = Database.getArchivedBooks(archivedBooksPerPage, archivedBooksPerPage * page);
                archivedBooksCount = archivedBooksList.getCount();
                archivedBooks = archivedBooksList.getData();
                getInventory();
                break;
            default:
                break;
        }

    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public static int getArchivedBooksPerPage() { return archivedBooksPerPage; }

}