package fr.loot1.quill.guis;

import fr.loot1.quill.objects.Application;
import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import fr.loot1.quill.utils.GlowHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GuiApplications extends GuiHolder {

    final static int applicationsPerPage = 45;
    private int applicationCount;

    protected Inventory inventory;
    protected int page = 0;

    private List<Application> applications;

    HashMap<Integer, Application> clickableApplications = new HashMap<>();

    List<Boolean> activeButtons = Arrays.asList(false, false, false, false);

    @Override
    public @NotNull Inventory getInventory() {
        inventory.clear();

        int maxPages = (int) Math.ceil((double) applicationCount / applicationsPerPage);
        page = Math.min(maxPages, page);

        for (int i = 0; i < applications.size(); i++) {
            Application toDisplayBook = applications.get(i);
            OfflinePlayer author = toDisplayBook.getAuthor();
            inventory.setItem(i, playerHeadGui(
                    author,
                    Config.getColored("menus.all-players.items.player-head.name").replace("%player%", author.getName()).replace("%title%", toDisplayBook.getTitle()),
                    formatLore(Config.getColoredList("menus.all-players.items.player-head.lore"), toDisplayBook)
            ));
            clickableApplications.put(i, toDisplayBook);
        }

        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            inventory.setItem(45 + status.getValue(), activeButtons.get(status.getValue()) ? GlowHelper.glow(status.getButton()) : status.getButton());
        }

        if (page > 0) {
            inventory.setItem(50, itemGui(Material.ARROW, Config.getColored("menus.global.items.previous-page")));
        }
        inventory.setItem(53, itemGui(Material.BARRIER, Config.getColored("menus.global.items.close")));
        if (page < maxPages - 1) {
            inventory.setItem(51, itemGui(Material.ARROW, Config.getColored("menus.global.items.next-page")));
        }

        return inventory;
    }

    public GuiApplications(final Player playerWhoClicked, final ApplicationList applicationList) {
        applicationCount = applicationList.getCount();
        applications = applicationList.getData();
        inventory = Bukkit.createInventory(this, 54, Config.getColored("menus.all-players.title"));
        playerWhoClicked.openInventory(getInventory());
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        final int slot = event.getSlot();

        if(slot > 44 && slot < 49) {
            final int clickedValue = slot - 45;
            activeButtons.set(clickedValue, !activeButtons.get(clickedValue));
            refresh();
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final Material clickedMaterial = event.getCurrentItem().getType();

        switch (clickedMaterial) {
            case PLAYER_HEAD:
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
                refresh();
                break;
            default:
                break;
        }

    }

    private void refresh() {
        ApplicationList applicationList = Database.getApplicationsByStatus(activeButtons.get(0), activeButtons.get(1), activeButtons.get(2), activeButtons.get(3), applicationsPerPage, applicationsPerPage * page);
        applicationCount = applicationList.getCount();
        applications = applicationList.getData();
        getInventory();
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public static int getApplicationsPerPage() { return applicationsPerPage; }

}