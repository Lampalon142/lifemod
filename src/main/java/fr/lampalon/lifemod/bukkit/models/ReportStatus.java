package fr.lampalon.lifemod.bukkit.models;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;

public enum ReportStatus {
    OPEN(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("report.report-status.open"))),
    PENDING(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("report.report-status.pending"))),
    IN_PROGRESS(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("report.report-status.in_progress"))),
    CLOSED(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("report.report-status.closed"))),
    REJECTED(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("report.report-status.rejeted"))),
    ARCHIVED(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("report.report-status.archived")));

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
