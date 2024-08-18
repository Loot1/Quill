package fr.loot1.quill.listeners;

import fr.loot1.quill.Quill;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryHolder;

import fr.loot1.quill.guis.GuiHolder;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    public GuiListener(Quill quill) { }

    @EventHandler
    private void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getRawSlot() < 0) return;
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof GuiHolder)) return;
        ClickType click = event.getClick();
        if (event.getSlot() != event.getRawSlot()) {
            if (click.isKeyboardClick() || click.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        final ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        ((GuiHolder) holder).onInventoryClick(event);
    }

    @EventHandler
    private void onInventoryDragEvent(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof GuiHolder) {
            for (int slot : event.getRawSlots()) {
                if (event.getInventorySlots().contains(slot)) {
                    event.setCancelled(true);
                }
            }
            ((GuiHolder) holder).onInventoryDrag(event);
        }
    }

    @EventHandler
    private void onInventoryOpenEvent(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof GuiHolder) {
            ((GuiHolder) holder).onInventoryOpen(event);
        }
    }

    @EventHandler
    private void onInventoryCloseEvent(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof GuiHolder) {
            ((GuiHolder) holder).onInventoryClose(event);
        }
    }
}