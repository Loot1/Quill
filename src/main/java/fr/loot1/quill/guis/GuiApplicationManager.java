package fr.loot1.quill.guis;

import fr.loot1.quill.Quill;
import fr.loot1.quill.objects.Application;
import fr.loot1.quill.managers.ConfigManager;
import fr.loot1.quill.utils.GlowHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiApplicationManager extends GuiHolder {

    protected Inventory inventory;
    private final Application application;
    private final ConfigManager configManager;

    @Nullable
    private final GuiHolder previousGui;

    public GuiApplicationManager(final Player playerWhoClicked, final Application app, final Quill quill, @Nullable final GuiHolder previousGui) {
        application = app;
        configManager = quill.getConfigManager();
        this.previousGui = previousGui;
        playerWhoClicked.openInventory(getInventory());
    }

    @Override
    public @NotNull Inventory getInventory() {
        inventory = Bukkit.createInventory(this, 9,
                configManager.getColored("menus.application.title")
                        .replace("%title%", application.getTitle()));

        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            inventory.setItem(status.getValue(),
                    status == application.getStatus()
                            ? GlowHelper.glow(status.getButton(configManager))
                            : status.getButton(configManager));
        }

        if (previousGui != null) {
            inventory.setItem(7, itemGui(Material.ARROW,
                    configManager.getColored("menus.global.items.back")));
        }

        inventory.setItem(8, itemGui(Material.BARRIER,
                configManager.getColored("menus.global.items.close")));

        return inventory;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final int slot = event.getSlot();

        switch (slot) {
            case 0:
            case 1:
            case 2:
            case 3: {
                final Application.ApplicationStatus oldStatus = application.getStatus();
                if (slot != oldStatus.getValue()) {
                    if (player.hasPermission("quill.status")) {
                        final Application.ApplicationStatus newStatus =
                                Application.ApplicationStatus.fromInteger(slot);
                        if (application.setStatus(newStatus)) {
                            inventory.setItem(slot, GlowHelper.glow(newStatus.getButton(configManager)));
                            inventory.setItem(oldStatus.getValue(), oldStatus.getButton(configManager));
                        }
                    } else {
                        player.sendMessage(configManager.getColored("messages.errors.permission-denied"));
                        player.closeInventory();
                    }
                }
                break;
            }
            case 7:
                if (previousGui != null) {
                    player.openInventory(previousGui.getInventory());
                } else {
                    player.closeInventory();
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