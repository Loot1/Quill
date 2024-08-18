package fr.loot1.quill.guis;

import fr.loot1.quill.Quill;
import fr.loot1.quill.objects.Application;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.GlowHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiApplicationManager extends GuiHolder {

    protected Inventory inventory;
    private final Application application;

    private final Config config;

    @Override
    public @NotNull Inventory getInventory() {
        inventory = Bukkit.createInventory(this, 9, config.getColored("menus.application.title").replace("%title%", application.getTitle()));

        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            inventory.setItem(status.getValue(), status == application.getStatus() ? GlowHelper.glow(status.getButton()) : status.getButton());
        }

        inventory.setItem(8, itemGui(Material.BARRIER, config.getColored("menus.global.items.close")));
        return inventory;
    }

    public GuiApplicationManager(final Player playerWhoClicked, final Application app, final Quill quill) {
        application = app;
        config = quill.getConfigManager();
        playerWhoClicked.openInventory(getInventory());
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final int slot = event.getSlot();

        switch (slot) {
            case 0:
            case 1:
            case 2:
            case 3:
                final Application.ApplicationStatus oldStatus  = application.getStatus();
                if(slot != oldStatus.getValue()) {
                    if(player.hasPermission("quill.status")) {
                        final Application.ApplicationStatus newStatus = Application.ApplicationStatus.fromInteger(slot);
                        boolean updated = application.setStatus(newStatus);
                        if(updated) {
                            inventory.setItem(event.getSlot(), GlowHelper.glow(newStatus.getButton()));
                            inventory.setItem(oldStatus.getValue(), oldStatus.getButton());
                        }
                    } else {
                        player.sendMessage(config.getColored("messages.errors.permission-denied"));
                        player.closeInventory();
                    }
                }
                break;
            case 8:
                player.closeInventory();
                break;
            default:
                break;
        }
    }

    @Override
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

}