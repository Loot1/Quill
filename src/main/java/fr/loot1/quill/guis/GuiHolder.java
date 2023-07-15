package fr.loot1.quill.guis;

import java.util.Arrays;
import java.util.List;

import fr.loot1.quill.objects.ArchivedBook;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public abstract class GuiHolder implements InventoryHolder {
    public abstract void onInventoryClick(InventoryClickEvent event);

    public abstract void onInventoryDrag(InventoryDragEvent event);

    public void onInventoryOpen(InventoryOpenEvent event) {}

    public void onInventoryClose(InventoryCloseEvent event) {}

    public ItemStack itemGui(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack itemGuiWithLore(final Material material, final String name, final List<String> lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack headGui(final OfflinePlayer player, final String name, final List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        if (headMeta != null) {
            headMeta.setOwningPlayer(player);
            headMeta.setDisplayName(name);
            headMeta.setLore(lore);
        }
        head.setItemMeta(headMeta);
        return head;
    }

    public List<String> formatLore(final List<String> lore, final ArchivedBook archivedBook) {
        lore.replaceAll(s -> s.replaceAll("%player%", archivedBook.getAuthor().getName()));
        lore.replaceAll(s -> s.replaceAll("%title%", archivedBook.getTitle()));
        lore.replaceAll(s -> s.replaceAll("%date%", archivedBook.getDate()));
        lore.replaceAll(s -> s.replaceAll("%status%", String.valueOf(archivedBook.getStatus())));
        return lore;
    }


}