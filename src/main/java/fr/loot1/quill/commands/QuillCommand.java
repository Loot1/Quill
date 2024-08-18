package fr.loot1.quill.commands;

import fr.loot1.quill.Quill;
import fr.loot1.quill.guis.GuiApplications;
import fr.loot1.quill.guis.GuiPlayerApplications;
import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.utils.Config;
import fr.loot1.quill.utils.Database;
import fr.loot1.quill.utils.PlayerCacheManager;
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

public class QuillCommand implements CommandExecutor, TabCompleter {

    private final Quill main;
    private final Database database;
    private final Config config;
    private final PlayerCacheManager playerCacheManager;

    public QuillCommand(Quill quill) {
        this.main = quill;
        this.database = quill.getDatabase();
        this.config = quill.getConfigManager();
        this.playerCacheManager = quill.getPlayerCacheManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String msg, String[] args) {
        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "apply":
                    if(sender instanceof Player p) {
                        if(sender.hasPermission("quill.apply")) {
                            ItemStack book = p.getInventory().getItemInMainHand();
                            if(book.getType() == Material.WRITTEN_BOOK) {
                                BookMeta bookMeta = (BookMeta) book.getItemMeta();
                                database.addApplication(p.getUniqueId(), bookMeta.getTitle(), database.encode(bookMeta.getPages()));
                                p.getInventory().remove(book);
                                sender.sendMessage(config.getColored("messages.apply-done").replace("%title%", bookMeta.getTitle()).replace("%author%", bookMeta.getAuthor()));
                                main.getServer().getOnlinePlayers().stream()
                                        .filter(playerToNotify -> playerToNotify.hasPermission("quill.notify"))
                                        .forEach(playerToNotify -> playerToNotify.sendMessage(config.getColored("messages.notification-new-apply").replace("%author%", bookMeta.getAuthor())));
                            } else {
                                sender.sendMessage(config.getColored("messages.errors.item-not-book"));
                            }
                        } else {
                            sender.sendMessage(config.getColored("messages.errors.permission-denied"));
                        }
                    } else {
                        sender.sendMessage(config.getColored("messages.errors.console-sender"));
                    }
                    break;
                case "look":
                    if(sender instanceof Player) {
                        if(sender.hasPermission("quill.look")) {
                            if(args.length > 1) {
                                OfflinePlayer player;
                                player = main.getServer().getOfflinePlayer(args[1]);
                                if(player.hasPlayedBefore()) {
                                    ApplicationList applications = database.getPlayerApplications(player.getUniqueId(), GuiPlayerApplications.getApplicationsPerPage(), 0);
                                    if(applications.getCount() == 0) {
                                        sender.sendMessage(config.getColored("messages.errors.no-application"));
                                    } else {
                                        new GuiPlayerApplications((Player) sender, player, applications, main);
                                    }
                                } else {
                                    sender.sendMessage(config.getColored("messages.errors.player-not-found").replace("%player%", args[1]));
                                }
                            } else {
                                sender.sendMessage(config.getColored("messages.usages.look"));
                            }
                        } else {
                            sender.sendMessage(config.getColored("messages.errors.permission-denied"));
                        }
                    } else {
                        sender.sendMessage(config.getColored("messages.errors.console-sender"));
                    }
                    break;
                case "reload":
                    if(sender.hasPermission("quill.reload")) {
                        config.reload();
                        sender.sendMessage(config.getColored("messages.configuration-reload"));
                    } else {
                        sender.sendMessage(config.getColored("messages.errors.permission-denied"));
                    }
                    break;
                default:
                    sender.sendMessage(config.getColored("messages.usages.main"));
                    break;
            }
        } else {
            if(sender instanceof Player) {
                if(sender.hasPermission("quill.look")) {
                    ApplicationList applicationList = database.getApplications(GuiApplications.getApplicationsPerPage(), 0);
                    if(applicationList.getCount() == 0) {
                        sender.sendMessage(config.getColored("messages.errors.no-application"));
                    } else {
                        new GuiApplications((Player) sender, applicationList, main);
                    }
                } else {
                    sender.sendMessage(config.getColored("messages.errors.permission-denied"));
                }
            } else {
                sender.sendMessage(config.getColored("messages.errors.console-sender"));
            }
        }
        return true;
    }

    private static final List<String> SUBCOMMANDS = Arrays.asList("apply", "look", "reload");
    private static final List<String> BLANK = Collections.emptyList();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length <= 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        } else if(args.length == 2 && args[0].equalsIgnoreCase("look")) {
            return StringUtil.copyPartialMatches(args[1], playerCacheManager.getCachedPlayerNames(), new ArrayList<>());
        }
        return BLANK;
    }

}
