package fr.loot1.quill.commands;

import fr.loot1.quill.Quill;
import fr.loot1.quill.guis.GuiApplications;
import fr.loot1.quill.guis.GuiPlayerApplications;
import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class QuillCommand implements CommandExecutor, TabCompleter {

    final static Quill main = Quill.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String msg, String[] args) {
        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "apply":
                    if(sender instanceof Player) {
                        Player p = (Player) sender;
                        if(sender.hasPermission("quill.apply")) {
                            ItemStack book = p.getInventory().getItemInMainHand();
                            if(book.getType() == Material.WRITTEN_BOOK) {
                                BookMeta bookMeta = (BookMeta) book.getItemMeta();
                                Database.addArchivedBook(p.getUniqueId(), bookMeta.getTitle(), Database.encode(bookMeta.getPages()));
                                p.getInventory().remove(book);
                                sender.sendMessage(Config.getColored("messages.apply-done").replace("%title%", bookMeta.getTitle()).replace("%author%", bookMeta.getAuthor()));
                            } else {
                                sender.sendMessage(Config.getColored("messages.errors.item-not-book"));
                            }
                        } else {
                            sender.sendMessage(Config.getColored("messages.errors.permission-denied"));
                        }
                    } else {
                        sender.sendMessage(Config.getColored("messages.errors.console-sender"));
                    }
                    break;
                case "look":
                    if(sender instanceof Player) {
                        if(sender.hasPermission("quill.look")) {
                            if(args.length > 1) {
                                OfflinePlayer player;
                                player = main.getServer().getOfflinePlayer(args[1]);
                                if(player.hasPlayedBefore()) {
                                    ApplicationList archivedBooks = Database.getPlayerArchivedBooks(player.getUniqueId(), GuiPlayerApplications.getArchivedBooksPerPage(), 0);
                                    if(archivedBooks.getCount() == 0) {
                                        sender.sendMessage(Config.getColored("messages.errors.no-player-book"));
                                    } else {
                                        new GuiPlayerApplications((Player) sender, player, archivedBooks);
                                    }
                                } else {
                                    sender.sendMessage(Config.getColored("messages.errors.unknown-player"));
                                }
                            } else {
                                sender.sendMessage(Config.getColored("messages.usages.look"));
                            }
                        } else {
                            sender.sendMessage(Config.getColored("messages.errors.permission-denied"));
                        }
                    } else {
                        sender.sendMessage(Config.getColored("messages.errors.console-sender"));
                    }
                    break;
                case "reload":
                    if(sender.hasPermission("quill.reload")) {
                        Config.reload();
                        sender.sendMessage(Config.getColored("messages.configuration-reload"));
                    } else {
                        sender.sendMessage(Config.getColored("messages.errors.permission-denied"));
                    }
                    break;
                default:
                    sender.sendMessage(Config.getColored("messages.usages.main"));
                    break;
            }
        } else {
            if(sender instanceof Player) {
                if(sender.hasPermission("quill.look")) {
                    ApplicationList applicationList = Database.getArchivedBooks(GuiApplications.getArchivedBooksPerPage(), 0);
                    if(applicationList.getCount() == 0) {
                        sender.sendMessage(Config.getColored("messages.errors.no-book"));
                    } else {
                        new GuiApplications((Player) sender, applicationList);
                    }
                } else {
                    sender.sendMessage(Config.getColored("messages.errors.permission-denied"));
                }
            } else {
                sender.sendMessage(Config.getColored("messages.errors.console-sender"));
            }
        }
        return true;
    }

    private static final List<String> SUBCOMMANDS = Arrays.asList("archive", "look", "reload");
    private static final List<String> BLANK = Collections.emptyList();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length <= 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        } else if(args.length == 2 && args[0].equalsIgnoreCase("look")) {
            List<String> offlinePlayers = Arrays
                    .stream(main.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], offlinePlayers, new ArrayList<>());
        }
        return BLANK;
    }

}
