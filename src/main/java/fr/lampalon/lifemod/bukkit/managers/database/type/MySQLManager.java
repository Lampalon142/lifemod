package fr.lampalon.lifemod.bukkit.managers.database.type;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.database.DatabaseProvider;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.models.ReportStatus;
import fr.lampalon.lifemod.bukkit.models.StaffNote;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class MySQLManager implements DatabaseProvider {

    private final LifeMod plugin;
    private HikariDataSource dataSource;

    public MySQLManager(LifeMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setupDatabase() {
        FileConfiguration config = this.plugin.getConfig();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.name");
        String user = config.getString("database.user");
        String password = config.getString("database.password");
        int poolsize = config.getInt("database.poolsize", 10);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(poolsize);
        hikariConfig.setPoolName("LifeMod-MySQL");
        this.dataSource = new HikariDataSource(hikariConfig);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reports (" +
                            "uuid VARCHAR(36) PRIMARY KEY, reporter_uuid VARCHAR(36), target_uuid VARCHAR(36), reason TEXT, " +
                            "server_name VARCHAR(64), status VARCHAR(32), assigned_to VARCHAR(36), created_at BIGINT, " +
                            "updated_at BIGINT, closed_at BIGINT, close_reason TEXT," +
                            "location_world VARCHAR(64), location_x DOUBLE, location_y DOUBLE, location_z DOUBLE, " +
                            "location_yaw FLOAT, location_pitch FLOAT, last_updated_by VARCHAR(36)" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS report_staff_notes (" +
                            "note_id VARCHAR(36) PRIMARY KEY, report_id VARCHAR(36) NOT NULL, author VARCHAR(36) NOT NULL, " +
                            "created_at BIGINT NOT NULL, updated_at BIGINT NOT NULL, content TEXT NOT NULL, " +
                            "FOREIGN KEY (report_id) REFERENCES reports(uuid)" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_inventories (" +
                            "uuid VARCHAR(36) PRIMARY KEY, inventory_data LONGBLOB NOT NULL, saved_at BIGINT" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_coords (" +
                            "uuid VARCHAR(36) PRIMARY KEY, world VARCHAR(64), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, saved_at BIGINT" +
                            ");"
            );

            Map<String, String> columns = Map.of(
                    "location_world", "VARCHAR(64)",
                    "location_x", "DOUBLE",
                    "location_y", "DOUBLE",
                    "location_z", "DOUBLE",
                    "location_yaw", "FLOAT",
                    "location_pitch", "FLOAT",
                    "last_updated_by", "VARCHAR(36)"
            );
            Set<String> existingColumns = new HashSet<>();
            try (ResultSet rs = conn.getMetaData().getColumns(database, null, "reports", null)) {
                while (rs.next()) existingColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            for (Map.Entry<String, String> entry : columns.entrySet()) {
                if (!existingColumns.contains(entry.getKey().toLowerCase())) {
                    stmt.executeUpdate("ALTER TABLE reports ADD COLUMN " + entry.getKey() + " " + entry.getValue() + ";");
                    plugin.getLogger().info("[LifeMod] Added missing column to reports: " + entry.getKey() + " (" + entry.getValue() + ")");
                }
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error creating/upgrading tables: " + e.getMessage());
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.dataSource == null) {
            plugin.getLogger().severe("[LifeMod] MySQL dataSource is null! Called from: " + Arrays.toString(Thread.currentThread().getStackTrace()));
            throw new SQLException("MySQL dataSource is null!");
        }
        return this.dataSource.getConnection();
    }

    @Override
    public void closeConnection() {
        if (this.dataSource != null) this.dataSource.close();
    }

    @Override
    public Report getReportByUuid(UUID uuid) {
        String sql = "SELECT * FROM reports WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToReport(rs);
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error fetching report: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void saveReport(Report report) {
        String sql = "INSERT INTO reports (uuid, reporter_uuid, target_uuid, reason, server_name, status, assigned_to, created_at, updated_at, closed_at, close_reason, location_world, location_x, location_y, location_z, location_yaw, location_pitch, last_updated_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE reporter_uuid=VALUES(reporter_uuid), target_uuid=VALUES(target_uuid), reason=VALUES(reason), server_name=VALUES(server_name), status=VALUES(status), assigned_to=VALUES(assigned_to), created_at=VALUES(created_at), updated_at=VALUES(updated_at), closed_at=VALUES(closed_at), close_reason=VALUES(close_reason), location_world=VALUES(location_world), location_x=VALUES(location_x), location_y=VALUES(location_y), location_z=VALUES(location_z), location_yaw=VALUES(location_yaw), location_pitch=VALUES(location_pitch), last_updated_by=VALUES(last_updated_by)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, report.getUuid().toString());
            stmt.setString(2, report.getReporterUuid().toString());
            stmt.setString(3, report.getTargetUuid().toString());
            stmt.setString(4, report.getReason());
            stmt.setString(5, report.getServerName());
            stmt.setString(6, report.getStatus().name());
            stmt.setString(7, (report.getAssignedTo() != null) ? report.getAssignedTo().toString() : null);
            stmt.setLong(8, report.getCreatedAt());
            stmt.setLong(9, report.getUpdatedAt());
            stmt.setLong(10, report.getClosedAt());
            stmt.setString(11, report.getCloseReason());
            if (report.getLocation() != null && report.getLocation().getWorld() != null) {
                stmt.setString(12, report.getLocation().getWorld().getName());
                stmt.setDouble(13, report.getLocation().getX());
                stmt.setDouble(14, report.getLocation().getY());
                stmt.setDouble(15, report.getLocation().getZ());
                stmt.setFloat(16, report.getLocation().getYaw());
                stmt.setFloat(17, report.getLocation().getPitch());
            } else {
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.DOUBLE);
                stmt.setNull(14, Types.DOUBLE);
                stmt.setNull(15, Types.DOUBLE);
                stmt.setNull(16, Types.FLOAT);
                stmt.setNull(17, Types.FLOAT);
            }
            stmt.setString(18, (report.getLastUpdatedBy() != null) ? report.getLastUpdatedBy().toString() : null);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error saving report: " + e.getMessage());
        }
    }

    @Override
    public void updateReport(Report report) {
        saveReport(report);
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) reports.add(mapResultSetToReport(rs));
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error fetching reports: " + e.getMessage());
        }
        return reports;
    }

    @Override
    public List<Report> getReportsByTarget(UUID targetUuid) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE target_uuid = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, targetUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error fetching reports by target: " + e.getMessage());
        }
        return reports;
    }

    public void deleteReport(UUID uuid) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement psNotes = conn.prepareStatement("DELETE FROM report_staff_notes WHERE report_id = ?")) {
                psNotes.setString(1, uuid.toString());
                psNotes.executeUpdate();
            }
            try (PreparedStatement psReport = conn.prepareStatement("DELETE FROM reports WHERE uuid = ?")) {
                psReport.setString(1, uuid.toString());
                psReport.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "MySQL deleteReport error: " + e.getMessage());
        }
    }

    @Override
    public List<StaffNote> getStaffNotesForReport(UUID reportId) {
        List<StaffNote> notes = new ArrayList<>();
        String sql = "SELECT * FROM report_staff_notes WHERE report_id = ? ORDER BY created_at ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffNote note = new StaffNote(
                            UUID.fromString(rs.getString("note_id")),
                            UUID.fromString(rs.getString("author")),
                            rs.getLong("created_at"),
                            rs.getLong("updated_at"),
                            rs.getString("content")
                    );
                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error loading staff notes: " + e.getMessage());
        }
        return notes;
    }

    @Override
    public void addStaffNote(UUID reportId, StaffNote note) {
        String sql = "INSERT INTO report_staff_notes (note_id, report_id, author, created_at, updated_at, content) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note.getNoteId().toString());
            ps.setString(2, reportId.toString());
            ps.setString(3, note.getAuthor().toString());
            ps.setLong(4, note.getCreatedAt());
            ps.setLong(5, note.getUpdatedAt());
            ps.setString(6, note.getContent());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error adding staff note: " + e.getMessage());
        }
    }

    @Override
    public void deleteStaffNote(UUID noteId) {
        String sql = "DELETE FROM report_staff_notes WHERE note_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, noteId.toString());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                plugin.getDebugManager().log("database", "No staff note found with note_id: " + noteId);
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error deleting staff note: " + e.getMessage());
        }
    }

    @Override
    public void updateStaffNote(StaffNote note) {
        String sql = "UPDATE report_staff_notes SET updated_at = ?, content = ? WHERE note_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, note.getUpdatedAt());
            ps.setString(2, note.getContent());
            ps.setString(3, note.getNoteId().toString());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                plugin.getDebugManager().log("database", "No staff note found to update with note_id: " + note.getNoteId());
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error updating staff note: " + e.getMessage());
        }
    }

    @Override
    public void savePlayerInventory(UUID uuid, Inventory inventory) {
        String sql = "REPLACE INTO player_inventories (uuid, inventory_data, saved_at) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setBytes(2, serializeInventory(inventory));
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            plugin.getLogger().warning("[LifeMod] MySQL savePlayerInventory error: " + e.getMessage());
        }
    }

    @Override
    public ItemStack[] getPlayerInventory(UUID uuid) {
        String sql = "SELECT inventory_data FROM player_inventories WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return deserializeInventory(rs.getBytes("inventory_data"));
                }
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("[LifeMod] MySQL getPlayerInventory error: " + e.getMessage());
        }
        return new ItemStack[0];
    }

    private byte[] serializeInventory(Inventory inventory) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeInt(inventory.getSize());
            for (ItemStack item : inventory.getContents()) {
                oos.writeObject(item);
            }
            return baos.toByteArray();
        }
    }

    private ItemStack[] deserializeInventory(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            int size = ois.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) ois.readObject();
            }
            return items;
        }
    }

    @Override
    public void savePlayerCoords(UUID uuid, Location location) {
        String sql = "INSERT OR REPLACE INTO player_coords (uuid, x, y, z, world) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setString(5, location.getWorld().getName());
            statement.executeUpdate();
            plugin.getDebugManager().log("database", "Saved coords for UUID: " + uuid);
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Failed to save coordinates for UUID: " + uuid + " (" + e.getMessage() + ")");
        }
    }

    @Override
    public Location getPlayerCoords(UUID uuid) {
        String sql = "SELECT x, y, z, world FROM player_coords WHERE uuid = ?;";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    String worldName = resultSet.getString("world");
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        plugin.getDebugManager().log("database", "World '" + worldName + "' not found! Using default world.");
                        world = Bukkit.getWorlds().get(0);
                    }
                    plugin.getDebugManager().log("database", "Loaded coords for UUID: " + uuid);
                    return new Location(world, x, y, z);
                }
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Failed to retrieve coordinates for UUID: " + uuid + " (" + e.getMessage() + ")");
        }
        return null;
    }

    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        UUID reporterUuid = UUID.fromString(rs.getString("reporter_uuid"));
        UUID targetUuid = UUID.fromString(rs.getString("target_uuid"));
        String reason = rs.getString("reason");
        String serverName = rs.getString("server_name");
        Location location = null;
        String worldName = rs.getString("location_world");
        if (worldName != null && !worldName.isEmpty()) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = rs.getDouble("location_x");
                double y = rs.getDouble("location_y");
                double z = rs.getDouble("location_z");
                float yaw = rs.getFloat("location_yaw");
                float pitch = rs.getFloat("location_pitch");
                location = new Location(world, x, y, z, yaw, pitch);
            }
        }
        ReportStatus status = ReportStatus.valueOf(rs.getString("status").toUpperCase());
        UUID assignedTo = (rs.getString("assigned_to") != null) ? UUID.fromString(rs.getString("assigned_to")) : null;
        long createdAt = rs.getLong("created_at");
        long updatedAt = rs.getLong("updated_at");
        long closedAt = rs.getLong("closed_at");
        String closeReason = rs.getString("close_reason");
        UUID lastUpdatedBy = null;
        String lastUpdatedByStr = rs.getString("last_updated_by");
        if (lastUpdatedByStr != null && !lastUpdatedByStr.isEmpty())
            lastUpdatedBy = UUID.fromString(lastUpdatedByStr);

        Report report = new Report(uuid, reporterUuid, targetUuid, reason, serverName, location, status, assignedTo, createdAt, updatedAt, lastUpdatedBy, closedAt, closeReason);
        report.getStaffNotes().addAll(getStaffNotesForReport(report.getUuid()));
        return report;
    }
}