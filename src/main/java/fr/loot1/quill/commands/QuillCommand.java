package fr.loot1.quill.commands;

import fr.loot1.quill.Quill;
import fr.loot1.quill.guis.GuiApplications;
import fr.loot1.quill.guis.GuiHolder;
import fr.loot1.quill.guis.GuiPlayerApplications;
import fr.loot1.quill.objects.ApplicationList;
import fr.loot1.quill.managers.ConfigManager;
import fr.loot1.quill.managers.DatabaseManager;
import fr.loot1.quill.managers.PlayerCacheManager;
import fr.loot1.quill.utils.DiscordWebhook;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Commands(@org.bukkit.plugin.java.annotation.command.Command( name = "quill", desc = "Manage the Quill plugin" ))
@Permission(name = "quill.*", desc = "Allows all permissions of Quill plugin", defaultValue = PermissionDefault.OP, children = {
        @ChildPermission(name ="quill.apply"),
        @ChildPermission(name ="quill.look"),
        @ChildPermission(name ="quill.notify"),
        @ChildPermission(name ="quill.reload"),
        @ChildPermission(name ="quill.status"),
        @ChildPermission(name ="quill.self"),
})
@Permission(name = "quill.apply", desc = "Allows to apply", defaultValue = PermissionDefault.OP)
@Permission(name = "quill.look", desc = "Allows to look applications", defaultValue = PermissionDefault.OP)
@Permission(name = "quill.notify", desc = "Allows to have a notification if some applications are waiting", defaultValue = PermissionDefault.OP)
@Permission(name = "quill.reload", desc = "Allows to reload plugin configuration file", defaultValue = PermissionDefault.OP)
@Permission(name = "quill.status", desc = "Allows to change the status of an application", defaultValue = PermissionDefault.OP)
@Permission(name = "quill.self", desc = "Allows to view own submitted applications", defaultValue = PermissionDefault.OP)

public class QuillCommand implements CommandExecutor, TabCompleter {

    private final Quill main;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final PlayerCacheManager playerCacheManager;

    public QuillCommand(Quill quill) {
        this.main = quill;
        this.databaseManager = quill.getDatabaseManager();
        this.configManager = quill.getConfigManager();
        this.playerCacheManager = quill.getPlayerCacheManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String msg, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "apply":
                    if (!(sender instanceof Player p)) {
                        sender.sendMessage(configManager.getColored("messages.errors.console-sender"));
                        break;
                    }
                    if (!p.hasPermission("quill.apply")) {
                        p.sendMessage(configManager.getColored("messages.errors.permission-denied"));
                        break;
                    }
                    ItemStack book = p.getInventory().getItemInMainHand();
                    if (book.getType() != Material.WRITTEN_BOOK) {
                        p.sendMessage(configManager.getColored("messages.errors.item-not-book"));
                        break;
                    }
                    BookMeta bookMeta = (BookMeta) book.getItemMeta();
                    if (bookMeta == null) {
                        p.sendMessage(configManager.getColored("messages.errors.item-not-book"));
                        break;
                    }

                    final String bookTitle   = bookMeta.getTitle() != null ? bookMeta.getTitle() : "";
                    final String bookAuthor  = bookMeta.getAuthor() != null ? bookMeta.getAuthor() : p.getName();
                    final int    pageCount   = bookMeta.getPages().size();
                    final String encoded     = databaseManager.encode(bookMeta.getPages());
                    final UUID   playerUuid  = p.getUniqueId();
                    final String playerName  = p.getName();

                    main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
                        boolean added = databaseManager.addApplication(playerUuid, bookTitle, encoded);

                        if (added) {
                            String webhookUrl = configManager.get("discord.webhook-url");
                            if (webhookUrl != null && !webhookUrl.isEmpty()) {
                                try {
                                    DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
                                    webhook.setContent("**New Application Submitted**\n"
                                            + "**Author:** " + playerName + "\n"
                                            + "**Title:** " + bookTitle + "\n"
                                            + "**Pages:** " + pageCount);
                                    webhook.execute();
                                } catch (Exception e) {
                                    main.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
                                }
                            }
                        }

                        main.getServer().getScheduler().runTask(main, () -> {
                            if (!p.isOnline()) return;
                            if (!added) {
                                p.sendMessage(configManager.getColored("messages.errors.apply-failed"));
                                return;
                            }
                            p.getInventory().remove(book);
                            p.sendMessage(configManager.getColored("messages.apply-done")
                                    .replace("%title%", bookTitle)
                                    .replace("%author%", bookAuthor));
                            main.getServer().getOnlinePlayers().stream()
                                    .filter(pl -> pl.hasPermission("quill.notify"))
                                    .forEach(pl -> pl.sendMessage(
                                            configManager.getColored("messages.notification-new-apply")
                                                    .replace("%author%", bookAuthor)));
                        });
                    });
                    break;

                case "look":
                    if (!(sender instanceof Player p2)) {
                        sender.sendMessage(configManager.getColored("messages.errors.console-sender"));
                        break;
                    }
                    if (!p2.hasPermission("quill.look")) {
                        p2.sendMessage(configManager.getColored("messages.errors.permission-denied"));
                        break;
                    }
                    if (args.length > 1) {
                        OfflinePlayer target = main.getServer().getOfflinePlayer(args[1]);
                        if (!target.hasPlayedBefore()) {
                            p2.sendMessage(configManager.getColored("messages.errors.player-not-found")
                                    .replace("%player%", args[1]));
                            break;
                        }
                        final UUID targetUuid = target.getUniqueId();
                        main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
                            ApplicationList applications = databaseManager.getPlayerApplications(
                                    targetUuid, GuiHolder.APPLICATIONS_PER_PAGE, 0);
                            main.getServer().getScheduler().runTask(main, () -> {
                                if (!p2.isOnline()) return;
                                if (applications == null || applications.getCount() == 0) {
                                    p2.sendMessage(configManager.getColored("messages.errors.no-application"));
                                } else {
                                    new GuiPlayerApplications(p2, target, applications, main);
                                }
                            });
                        });
                    } else {
                        p2.sendMessage(configManager.getColored("messages.usages.look"));
                    }
                    break;

                case "reload":
                    if (!sender.hasPermission("quill.reload")) {
                        sender.sendMessage(configManager.getColored("messages.errors.permission-denied"));
                        break;
                    }
                    configManager.reload();
                    sender.sendMessage(configManager.getColored("messages.configuration-reload"));
                    break;

                case "self":
                    if (!(sender instanceof Player pSelf)) {
                        sender.sendMessage(configManager.getColored("messages.errors.console-sender"));
                        break;
                    }
                    if (!pSelf.hasPermission("quill.self")) {
                        pSelf.sendMessage(configManager.getColored("messages.errors.permission-denied"));
                        break;
                    }
                    final UUID selfUuid = pSelf.getUniqueId();
                    main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
                        ApplicationList selfList = databaseManager.getPlayerApplications(
                                selfUuid, GuiHolder.APPLICATIONS_PER_PAGE, 0);
                        main.getServer().getScheduler().runTask(main, () -> {
                            if (!pSelf.isOnline()) return;
                            if (selfList == null || selfList.getCount() == 0) {
                                pSelf.sendMessage(configManager.getColored("messages.errors.no-application-self"));
                            } else {
                                new GuiPlayerApplications(pSelf, pSelf, selfList, main);
                            }
                        });
                    });
                    break;

                default:
                    sender.sendMessage(configManager.getColored("messages.usages.main"));
                    break;
            }
        } else {
            if (!(sender instanceof Player p3)) {
                sender.sendMessage(configManager.getColored("messages.errors.console-sender"));
                return true;
            }
            if (!p3.hasPermission("quill.look")) {
                p3.sendMessage(configManager.getColored("messages.errors.permission-denied"));
                return true;
            }
            main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
                ApplicationList applicationList = databaseManager.getApplications(GuiHolder.APPLICATIONS_PER_PAGE, 0);
                main.getServer().getScheduler().runTask(main, () -> {
                    if (!p3.isOnline()) return;
                    if (applicationList == null || applicationList.getCount() == 0) {
                        p3.sendMessage(configManager.getColored("messages.errors.no-application"));
                    } else {
                        new GuiApplications(p3, applicationList, main);
                    }
                });
            });
        }
        return true;
    }

    private static final List<String> SUBCOMMANDS = Arrays.asList("apply", "look", "reload", "self");
    private static final List<String> BLANK = Collections.emptyList();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length <= 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("look")) {
            return StringUtil.copyPartialMatches(args[1], playerCacheManager.getCachedPlayerNames(), new ArrayList<>());
        }
        return BLANK;
    }

}