package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.gui.menus.ConfirmationMenu;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.models.ReportStatus;
import fr.lampalon.lifemod.bukkit.models.StaffNote;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import fr.lampalon.lifemod.bukkit.managers.gui.menus.ReportMainMenu;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class GuiDetailListener implements Listener {
    private final LifeMod plugin;

    public GuiDetailListener(LifeMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();

        String detailTitleTemplate = plugin.getLangConfig().getString("report.detail.title", "&d&lReport Details &7- &e%uuid%");
        String detailTitlePrefix = MessageUtil.formatMessage(detailTitleTemplate.split("%uuid%")[0]);

        String staffNotesTitle = MessageUtil.formatMessage(plugin.getLangConfig().getString("report.detail.notes.menu.title", "&bStaff Notes"));
        String confirmTitle = MessageUtil.formatMessage(plugin.getLangConfig().getString("report.detail.notes.menu.remove.confirm_title", "&cConfirm Delete"));
        if (!title.startsWith(detailTitlePrefix) && !title.equals(staffNotesTitle) && !title.equals(confirmTitle)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        NamespacedKey reportKey = new NamespacedKey(plugin, "report_uuid");
        NamespacedKey actionKey = new NamespacedKey(plugin, "action_type");

        String actionType = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        String clickedItemPlayerUuidStr = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "player_uuid"), PersistentDataType.STRING);

        String reportUuidStr = meta.getPersistentDataContainer().get(reportKey, PersistentDataType.STRING);
        if (reportUuidStr == null && actionType != null && !actionType.startsWith("playerinfo_")) {
            return;
        }

        Report report = null;
        if (reportUuidStr != null) {
            UUID reportUuid;
            try {
                reportUuid = UUID.fromString(reportUuidStr);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid report UUID.");
                return;
            }
            report = plugin.getDatabaseManager().getDatabaseProvider().getReportByUuid(reportUuid);
            if (report == null) {
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.messages.not-found", "&cReport not found.")));
                return;
            }
        }

        if (actionType != null) {
            actionType = actionType.trim().toLowerCase();
        }
        List<Report> reports = plugin.getDatabaseManager().getDatabaseProvider().getAllReports();
        int page = plugin.getGuiManager().getCurrentPage(player);
        int itemsPerPage = plugin.getLangConfig().getInt("report.gui.items-per-page", 45);
        NamespacedKey noteKey = new NamespacedKey(plugin, "note_id");
        String noteIdStr = meta.getPersistentDataContainer().get(noteKey, PersistentDataType.STRING);

        switch (actionType) {
            case "back":
                new ReportMainMenu(plugin, player, reports, page, itemsPerPage).open();
                break;
            case "assign":
                if (report.getAssignedTo() != null) {
                    player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.messages.assigned.already", "&cThis report is already assigned.")));
                    return;
                }
                report.setAssignedTo(player.getUniqueId());
                plugin.getDatabaseManager().getDatabaseProvider().updateReport(report);
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.messages.assigned.success", "&aYou have been assigned to this report!")));
                new fr.lampalon.lifemod.bukkit.managers.gui.menus.ReportDetailMenu(plugin, player, report).open();
                break;

            case "close":
                if (report.getStatus() == ReportStatus.CLOSED) {
                    player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.closed-already", "&cThis report is already closed.")));
                    return;
                }
                report.setStatus(ReportStatus.CLOSED);
                report.setClosedAt(System.currentTimeMillis());
                report.setUpdatedAt(System.currentTimeMillis());
                report.setCloseReason("Closed by " + player.getName());
                plugin.getDatabaseManager().getDatabaseProvider().updateReport(report);
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.closed", "&aReport closed.")));
                new ReportMainMenu(plugin, player, reports, page, itemsPerPage).open();
                break;

            case "suppress":
                Report current = plugin.getDatabaseManager().getDatabaseProvider().getReportByUuid(report.getUuid());
                if (current == null) {
                    player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.suppress-already", "&cThis report is already deleted.")));
                    return;
                }

                plugin.getDatabaseManager().getDatabaseProvider().deleteReport(report.getUuid());
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.suppress", "&aReport deleted successfully.")));

                new ReportMainMenu(plugin, player, reports, page, itemsPerPage).open();
                break;

            case "teleport":
                if (report.getLocation() == null || report.getLocation().getWorld() == null) {
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.detail.unknown.location", "&cLocation unknown or invalid.")
                    ));
                    return;
                }

                player.teleport(report.getLocation());
                player.sendMessage(MessageUtil.formatMessage(
                        plugin.getLangConfig().getString("report.detail.teleport-success", "&aTeleported to report location!")
                ));
                break;

            case "history":
                plugin.getGuiManager().openPlayerHistory(player, report);
                break;

            case "playerinfo_reporter":
            case "playerinfo_target":
            case "playerinfo_staff":
                if (clickedItemPlayerUuidStr == null) {
                    player.sendMessage("§cPlayer UUID not found on this item.");
                    return;
                }
                try {
                    UUID playerUuid = UUID.fromString(clickedItemPlayerUuidStr);
                    plugin.getGuiManager().openPlayerInformations(player, playerUuid);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cInvalid player UUID on item.");
                }
                break;
            case "notes":
                plugin.getGuiManager().openStaffNotesMenu(player, report);
                break;
            case "add_note":
                player.closeInventory();
                player.sendMessage(MessageUtil.formatMessage(
                        plugin.getLangConfig().getString("report.detail.notes.menu.chat.prompt", "&eType your staff note in chat. Type &ccancel &eto abort.")
                ));
                plugin.getNoteInputManager().startNoteInput(player, report);
                break;
            case "staff_note":

                if (noteIdStr == null) {
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.detail.notes.menu.errors.not_found", "&cNote not found.")
                    ));
                    return;
                }

                try {
                    UUID noteId = UUID.fromString(noteIdStr);
                    StaffNote note = report.getStaffNotes().stream()
                            .filter(n -> n.getNoteId().equals(noteId))
                            .findFirst()
                            .orElse(null);

                    if (note == null) {
                        player.sendMessage(MessageUtil.formatMessage(
                                plugin.getLangConfig().getString("report.detail.notes.menu.errors.not_found", "&cNote not found.")
                        ));
                        return;
                    }

                    ClickType click = event.getClick();

                    if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
                        player.closeInventory();
                        player.sendMessage(MessageUtil.formatMessage(
                                plugin.getLangConfig().getString("report.detail.notes.menu.edit.prompt", "&eType the new content for this note in chat. Type &ccancel &eto abort.")
                        ));
                        plugin.getNoteInputManager().startNoteEdit(player, report, note);
                    } else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
                        new ConfirmationMenu(plugin, player, "confirm_delete_note", noteId, report).open();
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cInvalid note ID format.");
                }
                break;
            case "confirm_delete_note":

                if (noteIdStr == null || reportUuidStr == null) {
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.detail.notes.menu.errors.data_deletion", "§cMissing data for deletion.")
                    ));
                    return;
                }
                try {
                    UUID noteId = UUID.fromString(noteIdStr);

                    if (report == null) {
                        player.sendMessage(MessageUtil.formatMessage(
                                plugin.getLangConfig().getString("report.detail.notes.menu.errors.report_not_found", "&cReport not found.")
                        ));
                        return;
                    }
                    StaffNote note = report.getStaffNotes().stream()
                            .filter(n -> n.getNoteId().equals(noteId))
                            .findFirst().orElse(null);
                    if (note == null) {
                        player.sendMessage(MessageUtil.formatMessage(
                                plugin.getLangConfig().getString("report.detail.notes.menu.errors.not_found", "&cNote not found.")
                        ));
                        return;
                    }
                    report.getStaffNotes().remove(note);
                    plugin.getDatabaseManager().getDatabaseProvider().deleteStaffNote(noteId);
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.detail.notes.menu.remove.deleted", "&aNote deleted.")
                    ));
                    plugin.getGuiManager().openStaffNotesMenu(player, report);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.detail.notes.menu.errors.not_found", "&cNote not found.")
                    ));
                }
                break;
            case "cancel_delete_note":
                if (reportUuidStr == null) {
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.detail.notes.menu.errors.report_not_found", "&cReport not found.")
                    ));
                    return;
                }

                if (report == null) {
                    player.sendMessage(MessageUtil.formatMessage(
                            plugin.getLangConfig().getString("report.detail.notes.menu.errors.report_not_found", "&cReport not found.")
                    ));
                    return;
                }
                player.sendMessage(MessageUtil.formatMessage(
                        plugin.getLangConfig().getString("report.detail.notes.menu.remove.cancelled", "&eNote deletion cancelled.")
                ));
                plugin.getGuiManager().openStaffNotesMenu(player, report);
                break;
            default:
                break;
//            case "evidence", "context":
//                String targetUuidStr = meta.getPersistentDataContainer().get(
//                        new NamespacedKey(plugin, "target_uuid"),
//                        PersistentDataType.STRING
//                );
//                if (targetUuidStr == null) {
//                    player.sendMessage("§cImpossible d’ouvrir le replay : UUID cible manquant.");
//                    return;
//                }
//                UUID targetUuid = UUID.fromString(targetUuidStr);
//                OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
//                String targetName = target.getName();
//                if (targetName == null) {
//                    player.sendMessage("§cNothing Report found.");
//                    return;
//                }
//                player.closeInventory();
//            }
        }
    }
}