package fr.loot1.quill.guis;

import fr.loot1.quill.Quill;
import fr.loot1.quill.managers.ConfigManager;
import fr.loot1.quill.managers.DatabaseManager;
import fr.loot1.quill.objects.Application;
import fr.loot1.quill.objects.ApplicationList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GuiPaginatedApplications extends GuiHolder {

    protected int applicationCount = 0;
    protected Inventory inventory;
    protected int page = 0;
    protected List<Application> applications = new ArrayList<>();
    protected final HashMap<Integer, Application> clickableApplications = new HashMap<>();

    protected final Quill main;
    protected final ConfigManager configManager;
    protected final DatabaseManager databaseManager;

    protected GuiPaginatedApplications(Quill quill) {
        this.main = quill;
        this.configManager = quill.getConfigManager();
        this.databaseManager = quill.getDatabaseManager();
    }

    protected abstract void fillContent();

    protected abstract int getCloseSlot();

    protected abstract void refreshAsync();

    protected void fillExtra() {}

    protected boolean handleExtraClick(InventoryClickEvent event) { return false; }

    protected int getPreviousPageSlot() { return 47; }

    protected int getNextPageSlot() { return 51; }

    @Override
    public @NotNull Inventory getInventory() {
        inventory.clear();
        clickableApplications.clear();

        int maxPages = applicationCount == 0 ? 1 : (int) Math.ceil((double) applicationCount / APPLICATIONS_PER_PAGE);
        page = Math.clamp(page, 0, maxPages - 1);

        fillContent();
        fillExtra();

        if (page > 0) {
            inventory.setItem(getPreviousPageSlot(),
                    itemGui(Material.ARROW, configManager.getColored("menus.global.items.previous-page")));
        }
        inventory.setItem(getCloseSlot(),
                itemGui(Material.BARRIER, configManager.getColored("menus.global.items.close")));
        if (page < maxPages - 1) {
            inventory.setItem(getNextPageSlot(),
                    itemGui(Material.ARROW, configManager.getColored("menus.global.items.next-page")));
        }

        return inventory;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (handleExtraClick(event)) return;

        if (event.getCurrentItem() == null) return;
        final int slot = event.getSlot();
        final Player player = (Player) event.getWhoClicked();
        final Material material = event.getCurrentItem().getType();

        Application app = clickableApplications.get(slot);
        if (app != null) {
            handleApplicationClick(player, app, event.getClick());
            return;
        }

        if (material == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (material == Material.ARROW) {
            if (slot == getPreviousPageSlot()) page--;
            else page++;
            refreshAsync();
        }
    }

    protected void handleApplicationClick(Player player, Application app, ClickType click) {
        if (click.isLeftClick()) {
            app.open(player);
        } else if (click.isRightClick()) {
            new GuiApplicationManager(player, app, main, this);
        }
    }

    protected void finishAsyncReload(ApplicationList result) {
        if (result == null) return;
        main.getServer().getScheduler().runTask(main, () -> {
            applicationCount = result.getCount();
            applications = result.getData();
            getInventory();
        });
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }
}