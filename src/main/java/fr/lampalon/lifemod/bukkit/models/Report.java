package fr.lampalon.lifemod.bukkit.models;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Report {
    private final UUID uuid;
    private final UUID reporterUuid;
    private final UUID targetUuid;
    private final String reason;
    private final String serverName;
    private final List<StaffNote> staffNotes = new ArrayList<>();
    private Location location;
    private ReportStatus status;
    private UUID assignedTo;
    private final long createdAt;
    private long updatedAt;
    private UUID lastUpdatedBy;
    private long closedAt;
    private String closeReason;

    public Report(UUID uuid, UUID reporterUuid, UUID targetUuid, String reason, String serverName, Location location,
                  ReportStatus status, UUID assignedTo, long createdAt, long updatedAt, UUID lastUpdatedBy, long closedAt, String closeReason) {
        this.uuid = uuid;
        this.reporterUuid = reporterUuid;
        this.targetUuid = targetUuid;
        this.reason = reason;
        this.serverName = serverName;
        this.location = location;
        this.status = status;
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastUpdatedBy = lastUpdatedBy;
        this.closedAt = closedAt;
        this.closeReason = closeReason;
    }

    public UUID getUuid() { return uuid; }
    public UUID getReporterUuid() { return reporterUuid; }
    public UUID getTargetUuid() { return targetUuid; }
    public String getReason() { return reason; }
    public String getServerName() { return serverName; }
    public Location getLocation() { return location; }
    public ReportStatus getStatus() { return status; }
    public UUID getAssignedTo() { return assignedTo; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public UUID getLastUpdatedBy() { return lastUpdatedBy; }
    public long getClosedAt() { return closedAt; }
    public String getCloseReason() { return closeReason; }
    public List<StaffNote> getStaffNotes() {
        return staffNotes;
    }
    public void addStaffNote(StaffNote note) {
        staffNotes.add(note);
    }
    public void removeStaffNote(StaffNote note) {
        staffNotes.remove(note);
    }

    public void setStatus(ReportStatus status) { this.status = status; }
    public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public void setLastUpdatedBy(UUID lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
    public void setClosedAt(long closedAt) { this.closedAt = closedAt; }
    public void setCloseReason(String closeReason) { this.closeReason = closeReason; }
}
