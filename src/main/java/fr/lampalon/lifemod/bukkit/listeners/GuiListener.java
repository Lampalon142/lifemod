package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.gui.menus.ReportMainMenu;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class GuiListener implements Listener {
    private final LifeMod plugin;

    public GuiListener(LifeMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        String menuTitleTemplate = plugin.getLangConfig().getString("report.gui.title", "&d&lReport &7(&e%page%&7/&e%totalpage%&7)");
        String menuTitlePrefix = MessageUtil.formatMessage(menuTitleTemplate.split("%page%")[0]);
        if (!title.startsWith(menuTitlePrefix)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;
        if (!clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();
        int itemsPerPage = plugin.getLangConfig().getInt("report.gui.items-per-page", 45);
        List<Report> reports = plugin.getDatabaseManager().getDatabaseProvider().getAllReports();
        int page = plugin.getGuiManager().getCurrentPage(player);
        int totalPages = plugin.getGuiManager().getTotalPage(reports, itemsPerPage);

        String nextPageName = MessageUtil.formatMessage(plugin.getLangConfig().getString("report.gui.navigation.next-page", "&aNext Page"));
        String previousPageName = MessageUtil.formatMessage(plugin.getLangConfig().getString("report.gui.navigation.previous-page", "&cPrevious Page"));

        if (displayName.equalsIgnoreCase(nextPageName) && page < totalPages - 1) {
            plugin.getGuiManager().openMainMenu(player, reports, page + 1, itemsPerPage);
            return;
        }
        if (displayName.equalsIgnoreCase(previousPageName) && page > 0) {
            plugin.getGuiManager().openMainMenu(player, reports, page - 1, itemsPerPage);
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, "report_uuid");
        String uuidStr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (uuidStr == null) return;
        UUID reportUuid;
        try {
            reportUuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            String msg = plugin.getLangConfig().getString("report.messages.invalid-uuid", "&cInvalid report UUID.");
            player.sendMessage(MessageUtil.formatMessage(msg));
            return;
        }

        ClickType click = event.getClick();
        if (click == null) {
            plugin.getLogger().warning("InventoryClickEvent: ClickType is null! (item: " + clicked.getType() + ", player: " + player.getName() + ")");
            return;
        }

        Report report = plugin.getDatabaseManager().getDatabaseProvider().getReportByUuid(reportUuid);

        switch (click) {
            case LEFT:
                if (report == null) {
                    String msg1 = plugin.getLangConfig().getString("report.messages.not-found", "&cReport not found.");
                    player.sendMessage(MessageUtil.formatMessage(msg1));
                    return;
                }
                if (report.getAssignedTo() != null) {
                    String msg2 = plugin.getLangConfig().getString("report.messages.assigned.already", "&cThis report is already assigned.");
                    player.sendMessage(MessageUtil.formatMessage(msg2));
                    return;
                }
                report.setAssignedTo(player.getUniqueId());
                plugin.getDatabaseManager().getDatabaseProvider().updateReport(report);
                String msg = plugin.getLangConfig().getString("report.messages.assigned.success", "&aYou have been assigned to this report!");
                player.sendMessage(MessageUtil.formatMessage(msg));
                break;
            case RIGHT:
                if (report == null) {
                    String msg3 = plugin.getLangConfig().getString("report.messages.not-found", "&cReport not found.");
                    player.sendMessage(MessageUtil.formatMessage(msg3));
                    return;
                }
                plugin.getGuiManager().openReportDetails(player, report);
            break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                if (report == null) {
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.suppress-already", "&cThis report is already deleted.")
                    ));
                    return;
                }
                plugin.getDatabaseManager().getDatabaseProvider().deleteReport(report.getUuid());
                player.sendMessage(MessageUtil.formatMessage(
                        plugin.getLangConfig().getString("report.suppress", "&aReport deleted successfully.")
                ));
                List<Report> updatedReports = plugin.getDatabaseManager().getDatabaseProvider().getAllReports();
                plugin.getGuiManager().openMainMenu(player, updatedReports, page, itemsPerPage);
                break;
            default:
                break;
        }
    }
}
