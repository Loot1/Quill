package fr.loot1.quill.guis;

import fr.loot1.quill.Quill;
import fr.loot1.quill.objects.Application;
import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.utils.GlowHelper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class GuiApplications extends GuiPaginatedApplications {

    private final List<Boolean> activeButtons = Arrays.asList(false, false, false, false);

    @Override
    protected int getCloseSlot() { return 53; }

    @Override
    protected int getPreviousPageSlot() { return 50; }

    public GuiApplications(final Player playerWhoClicked, final ApplicationList applicationList, Quill quill) {
        super(quill);
        applicationCount = applicationList.getCount();
        applications = applicationList.getData();
        inventory = Bukkit.createInventory(this, 54, configManager.getColored("menus.all-players.title"));
        playerWhoClicked.openInventory(getInventory());
    }

    @Override
    protected void fillContent() {
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            OfflinePlayer author = app.getAuthor();
            inventory.setItem(i, playerHeadGui(
                    author,
                    configManager.getColored("menus.all-players.items.player-head.name")
                            .replace("%player%", author.getName() != null ? author.getName() : "?")
                            .replace("%title%", app.getTitle()),
                    formatLore(configManager.getColoredList("menus.all-players.items.player-head.lore"), app, configManager)
            ));
            clickableApplications.put(i, app);
        }
    }

    @Override
    protected void fillExtra() {
        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            inventory.setItem(45 + status.getValue(),
                    activeButtons.get(status.getValue())
                            ? GlowHelper.glow(status.getButton(configManager))
                            : status.getButton(configManager));
        }
    }

    @Override
    protected boolean handleExtraClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot >= 45 && slot <= 48) {
            int statusValue = slot - 45;
            activeButtons.set(statusValue, !activeButtons.get(statusValue));
            refreshAsync();
            return true;
        }
        return false;
    }

    @Override
    protected void refreshAsync() {
        final int currentPage  = this.page;
        final boolean w    = activeButtons.get(0);
        final boolean a    = activeButtons.get(1);
        final boolean r    = activeButtons.get(2);
        final boolean arch = activeButtons.get(3);

        main.getServer().getScheduler().runTaskAsynchronously(main, () ->
                finishAsyncReload(databaseManager.getApplicationsByStatus(
                        w, a, r, arch, APPLICATIONS_PER_PAGE, APPLICATIONS_PER_PAGE * currentPage)));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return super.getInventory();
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        super.onInventoryDrag(event);
    }

}