package fr.loot1.quill.guis;

import fr.loot1.quill.objects.Application;
import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GuiPlayerApplications extends GuiHolder {

    final static int applicationsPerPage = 45;
    private int applicationCount;

    protected Inventory inventory;
    protected int page = 0;

    private final UUID playerToCheckUUID;

    private List<Application> applications;

    HashMap<Integer, Application> clickableApplications = new HashMap<>();

    @Override
    public @NotNull Inventory getInventory() {
        inventory.clear();

        int maxPages = (int) Math.ceil((double) applicationCount / applicationsPerPage);
        page = Math.min(maxPages, page);

        for (int i = 0; i < applications.size(); i++) {
            Application toDisplayBook = applications.get(i);
            inventory.setItem(i, itemGuiWithLore(
                    Material.WRITTEN_BOOK,
                    Config.getColored("menus.player.items.book.name").replace("%title%", toDisplayBook.getTitle()),
                    formatLore(Config.getColoredList("menus.player.items.book.lore"), toDisplayBook)
            ));
            clickableApplications.put(i, toDisplayBook);
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

    public GuiPlayerApplications(final Player playerWhoClicked, final OfflinePlayer playerToCheckBooks, final ApplicationList applicationList) {
        playerToCheckUUID = playerToCheckBooks.getUniqueId();
        applicationCount = applicationList.getCount();
        applications = applicationList.getData();
        inventory = Bukkit.createInventory(this, 54, Config.getColored("menus.player.title").replace("%player%", playerToCheckBooks.getName()).replace("%size%", String.valueOf(applicationCount)));
        playerWhoClicked.openInventory(getInventory());
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Material clickedMaterial = event.getCurrentItem().getType();

        switch (clickedMaterial) {
            case WRITTEN_BOOK:
                final ClickType click = event.getClick();
                Application clickedApplication = clickableApplications.get(event.getSlot());
                if (click.isLeftClick()) {
                    clickedApplication.open(player);
                } else if (click.isRightClick()) {
                    new GuiApplicationManager(player, clickedApplication);
                }
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
                ApplicationList applicationList = Database.getPlayerApplications(playerToCheckUUID, applicationsPerPage, applicationsPerPage * page);
                applicationCount = applicationList.getCount();
                applications = applicationList.getData();
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

    public static int getApplicationsPerPage() { return applicationsPerPage; }

}