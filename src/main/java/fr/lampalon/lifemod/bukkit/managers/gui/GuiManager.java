package fr.lampalon.lifemod.bukkit.managers.gui;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.managers.gui.menus.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiManager {

    private final LifeMod plugin;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public GuiManager(LifeMod plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player, List<Report> reports, int page, int itemsPerPage) {
        new ReportMainMenu(plugin, player, reports, page, itemsPerPage).open();
    }

   public void openReportDetails(Player player, Report report) {
       new ReportDetailMenu(plugin, player, report).open();
   }

    public void openPlayerHistory(Player player, Report report) {
        new PlayerHistoryMenu(plugin, player, report.getTargetUuid()).open();
    }

    public void openPlayerInformations(Player viewer, UUID playerUuid){
        new PlayerInfoMenu(plugin, viewer, playerUuid).open();
    }

    public void openStaffNotesMenu(Player player, Report report){
        new StaffNotesMenu(plugin, player, report).open();
    }

    public void openConfirmationMenu(Player player, String actionType, UUID noteId, Report report){
        new ConfirmationMenu(plugin, player, actionType, noteId, report).open();
    }

    public int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    public void setCurrentPage(Player player, int page) {
        UUID uuid = player.getUniqueId();
        if (playerPages.getOrDefault(uuid, -1) != page) {
            playerPages.put(uuid, page);
        }
    }

    public int getTotalPage(List<Report> reports, int itemsPerPage) {
        int size = (reports != null) ? reports.size() : 0;
        return Math.max(1, (size + itemsPerPage - 1) / itemsPerPage);
    }
}
