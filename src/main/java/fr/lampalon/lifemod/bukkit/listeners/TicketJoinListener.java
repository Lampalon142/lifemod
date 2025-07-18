package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.models.ReportStatus;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import fr.lampalon.lifemod.bukkit.utils.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class TicketJoinListener implements Listener {
    private final LifeMod plugin;
    private final UpdateChecker updateChecker;
    private final DebugManager debug;

    public TicketJoinListener(LifeMod plugin, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;
        this.debug = plugin.getDebugManager();
    }

    @EventHandler
    public void onTicketJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("lifemod.report.join")) return;

        List<Report> reports = plugin.getDatabaseManager().getDatabaseProvider().getAllReports();
        long openCount = reports.stream().filter(r -> r.getStatus() == ReportStatus.OPEN).count();
        long pendingCount = reports.stream().filter(r -> r.getStatus() == ReportStatus.PENDING).count();

        String msg = plugin.getLangConfig().getString(
                "report.join-message",
                "&aThere are currently &e%open%&a open reports and &e%pending%&a pending reports."
        );
        msg = msg.replace("%open%", String.valueOf(openCount))
                .replace("%pending%", String.valueOf(pendingCount))
                .replace("%total%", String.valueOf(reports.size()));

        player.sendMessage(MessageUtil.formatMessage(msg));
    }
}
