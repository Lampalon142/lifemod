package fr.lampalon.lifemod.bukkit.managers.gui.menus;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import fr.lampalon.lifemod.bukkit.utils.PaginationHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ReportMainMenu {
    private final LifeMod plugin;
    private final Player player;
    private final List<Report> reports;
    private final int page;
    private final int itemsPerPage;

    public ReportMainMenu(LifeMod plugin, Player player, List<Report> reports, int page, int itemsPerPage) {
        this.plugin = plugin;
        this.player = player;
        this.reports = reports;
        this.page = page;
        this.itemsPerPage = itemsPerPage;
    }

    public void open() {
        int rows = 6;
        String titleTemplate = plugin.getLangConfig().getString("report.gui.title", "&d&lReport &7(&e%page%&7/&e%totalpage%&7)");
        String title = MessageUtil.formatMessage(
                titleTemplate
                        .replace("%page%", String.valueOf(page + 1))
                        .replace("%totalpage%", String.valueOf(getTotalPages()))
        );
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        PaginationHelper<Report> pagination = new PaginationHelper<>(reports, itemsPerPage);
        List<Report> pageReports = pagination.getPage(page);

        int slot = 0;
        for (Report report : pageReports) {
            inv.setItem(slot++, buildReportItem(report));
        }

        String previousPageName = plugin.getLangConfig().getString("report.gui.navigation.previous-page", "&aPrevious Page");
        String nextPageName = plugin.getLangConfig().getString("report.gui.navigation.next-page", "&aNext Page");

        if (page > 0) {
            inv.setItem(rows * 9 - 8, buildNavItem(previousPageName, Material.ARROW));
        }
        if (page < getTotalPages() - 1) {
            inv.setItem(rows * 9 - 2, buildNavItem(nextPageName, Material.ARROW));
        }

        plugin.getGuiManager().setCurrentPage(player, page);

        player.openInventory(inv);
    }

    private ItemStack buildReportItem(Report report) {
        String targetName = getNameFromUuid(report.getTargetUuid());
        String targetStatus = getStatusString(report.getTargetUuid());
        String reporterName = getNameFromUuid(report.getReporterUuid());
        String reporterStatus = getStatusString(report.getReporterUuid());

        List<String> loreTemplate = plugin.getLangConfig().getStringList("report.gui.lore");
        List<String> lore = loreTemplate.stream()
                .map(line -> MessageUtil.formatMessage(line
                        .replace("%target%", targetName)
                        .replace("%target_status%", targetStatus)
                        .replace("%reporter%", reporterName)
                        .replace("%reporter_status%", reporterStatus)
                        .replace("%reason%", report.getReason())
                        .replace("%status%", report.getStatus().getDisplayName())
                        .replace("%server%", report.getServerName())
                        .replace("%date%", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(report.getCreatedAt())))
                        .replace("%uuid%", report.getUuid().toString())
                ))
                .toList();

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        String displayName = plugin.getLangConfig()
                .getString("report.gui.name", "&eReport: %uuid%")
                .replace("%uuid%", report.getUuid().toString());
        meta.setDisplayName(MessageUtil.formatMessage(displayName));
        meta.setLore(lore);

        NamespacedKey key = new NamespacedKey(plugin, "report_uuid");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, report.getUuid().toString());
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack buildNavItem(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.formatMessage(name));
        item.setItemMeta(meta);
        return item;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) reports.size() / itemsPerPage));
    }

    private String getNameFromUuid(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer != null && offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString();
    }

    private String getStatusString(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        String key = (player != null && player.isOnline()) ? "report.status.online" : "report.status.offline";
        return MessageUtil.formatMessage(plugin.getLangConfig().getString(key, (player != null && player.isOnline()) ? "&aonline" : "&coffline"));
    }
}