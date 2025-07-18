package fr.lampalon.lifemod.bukkit.managers.gui.menus;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerHistoryMenu {
    private final LifeMod plugin;
    private final Player player;
    private final UUID targetUuid;

    public PlayerHistoryMenu(LifeMod plugin, Player player, UUID targetUuid) {
        this.plugin = plugin;
        this.player = player;
        this.targetUuid = targetUuid;
    }

    public void open() {
        List<Report> reports = plugin.getDatabaseManager().getDatabaseProvider().getReportsByTarget(targetUuid);
        String playerName = Bukkit.getOfflinePlayer(targetUuid).getName();
        String title = MessageUtil.formatMessage(
                plugin.getLangConfig().getString("report.detail.history-player.title", "&6History: %player%")
        ).replace("%player%", playerName != null ? playerName : "Unknown");

        int size = Math.max(27, ((reports.size() - 1) / 9 + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, title);

        if (reports.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.setDisplayName(MessageUtil.formatMessage(
                    plugin.getLangConfig().getString("report.detail.history-player.empty", "&cNo report history for this player.")
            ));
            empty.setItemMeta(meta);
            inv.setItem(13, empty);
        } else {
            for (int i = 0; i < reports.size(); i++) {
                Report r = reports.get(i);
                ItemStack item = buildReportItem(r);
                inv.setItem(i, item);
            }
        }

        player.openInventory(inv);
    }

    private ItemStack buildReportItem(Report report) {
        String name = MessageUtil.formatMessage(
                plugin.getLangConfig().getString("report.detail.history-player.item.name", "&eReport: %reason%")
        ).replace("%reason%", report.getReason());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getLangConfig().getStringList("report.detail.history-player.item.lore")) {
            lore.add(MessageUtil.formatMessage(line)
                    .replace("%date%", sdf.format(new java.util.Date(report.getCreatedAt())))
                    .replace("%status%", report.getStatus().getDisplayName())
                    .replace("%server%", report.getServerName())
            );
        }

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
