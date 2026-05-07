package fr.loot1.quill.guis;

import fr.loot1.quill.Quill;
import fr.loot1.quill.objects.Application;
import fr.loot1.quill.objects.ApplicationList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GuiPlayerApplications extends GuiPaginatedApplications {

    private final UUID playerToCheckUUID;
    private final String playerName;

    @Override
    protected int getCloseSlot() { return 49; }

    public GuiPlayerApplications(final Player playerWhoClicked,
                                  final org.bukkit.OfflinePlayer playerToCheck,
                                  final ApplicationList applicationList,
                                  final Quill quill) {
        super(quill);
        playerToCheckUUID = playerToCheck.getUniqueId();
        playerName        = playerToCheck.getName() != null ? playerToCheck.getName() : "?";
        applicationCount  = applicationList.getCount();
        applications      = applicationList.getData();
        inventory = Bukkit.createInventory(this, 54,
                configManager.getColored("menus.player.title")
                        .replace("%player%", playerName)
                        .replace("%size%", String.valueOf(applicationCount)));
        playerWhoClicked.openInventory(getInventory());
    }

    @Override
    protected void fillContent() {
        for (int i = 0; i < applications.size(); i++) {
            Application app = applications.get(i);
            inventory.setItem(i, itemGuiWithLore(
                    Material.WRITTEN_BOOK,
                    configManager.getColored("menus.player.items.book.name")
                            .replace("%title%", app.getTitle()),
                    formatLore(configManager.getColoredList("menus.player.items.book.lore"), app, configManager)
            ));
            clickableApplications.put(i, app);
        }
    }

    @Override
    protected void refreshAsync() {
        final int currentPage = this.page;
        main.getServer().getScheduler().runTaskAsynchronously(main, () ->
                finishAsyncReload(databaseManager.getPlayerApplications(
                        playerToCheckUUID, APPLICATIONS_PER_PAGE, APPLICATIONS_PER_PAGE * currentPage)));
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