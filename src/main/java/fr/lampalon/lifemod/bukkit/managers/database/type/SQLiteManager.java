package fr.lampalon.lifemod.bukkit.managers.database.type;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
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

import java.io.*;
import java.sql.*;
import java.util.*;

public class SQLiteManager implements DatabaseProvider {

    private final LifeMod plugin;
    private final DebugManager debug;
    private Connection connection;

    public SQLiteManager(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
        connect();
    }

    private void connect() {
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                debug.log("database", "SQLite JDBC driver not found!");
                return;
            }
            FileConfiguration config = plugin.getConfig();
            String dbFileName = config.getString("database.sqlite.file", "database.db");
            File dbFile = new File(plugin.getDataFolder(), dbFileName);

            if (!dbFile.getParentFile().exists()) {
                if (dbFile.getParentFile().mkdirs()) {
                    debug.log("database", "Created SQLite database folder.");
                }
            }

            if (!dbFile.exists()) {
                if (dbFile.createNewFile()) {
                    debug.log("database", "Created new SQLite database file.");
                }
            }

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            if (connection == null) {
                debug.log("database", "ERROR: SQLite connection is NULL!");
            } else {
                debug.log("database", "SQLite connection established.");
            }
        } catch (IOException | SQLException e) {
            debug.log("database", "Unable to establish SQLite connection!");
            debug.userError(null, "SQLite connection error", e);
        }
    }

    @Override
    public void setupDatabase() {
        if (connection == null) {
            debug.log("database", "Unable to set up database: connection is NULL!");
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reports (" +
                            "uuid TEXT PRIMARY KEY, " +
                            "reporter_uuid TEXT, " +
                            "target_uuid TEXT, " +
                            "reason TEXT, " +
                            "server_name TEXT, " +
                            "status TEXT, " +
                            "assigned_to TEXT, " +
                            "created_at INTEGER, " +
                            "updated_at INTEGER, " +
                            "closed_at INTEGER, " +
                            "close_reason TEXT, " +
                            "location_world TEXT, " +
                            "location_x REAL, " +
                            "location_y REAL, " +
                            "location_z REAL, " +
                            "location_yaw REAL, " +
                            "location_pitch REAL, " +
                            "last_updated_by TEXT" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS report_staff_notes (" +
                            "note_id TEXT PRIMARY KEY, " +
                            "report_id TEXT NOT NULL, " +
                            "author TEXT NOT NULL, " +
                            "created_at INTEGER NOT NULL, " +
                            "updated_at INTEGER NOT NULL, " +
                            "content TEXT NOT NULL, " +
                            "FOREIGN KEY (report_id) REFERENCES reports(uuid) ON DELETE CASCADE" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_inventories (" +
                            "uuid TEXT PRIMARY KEY, " +
                            "inventory_data TEXT NOT NULL, " +
                            "saved_at INTEGER" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_coords (" +
                            "uuid TEXT PRIMARY KEY, " +
                            "world TEXT, " +
                            "x REAL, " +
                            "y REAL, " +
                            "z REAL, " +
                            "yaw REAL, " +
                            "pitch REAL, " +
                            "saved_at INTEGER" +
                            ");"
            );

            debug.log("database", "SQLite tables initialized.");
        } catch (SQLException e) {
            debug.log("database", "ERROR: Failed to initialize setupDatabase()!");
            debug.userError(null, "SQLite table creation error", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        FileConfiguration config = plugin.getConfig();
        String dbFileName = config.getString("database.sqlite.file", "database.db");
        File dbFile = new File(plugin.getDataFolder(), dbFileName);
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        return DriverManager.getConnection(url);
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                debug.log("database", "SQLite connection closed.");
            }
        } catch (SQLException e) {
            debug.userError(null, "Error closing SQLite connection", e);
        }
    }

    public void savePlayerInventory(UUID uuid, Inventory inventory) {
        if (connection == null) {
            debug.log("database", "ERROR: SQLite connection NULL during savePlayerInventory!");
            return;
        }

        String serializedInventory = serializeInventory(inventory);
        if (serializedInventory == null) return;

        String sql = "INSERT OR REPLACE INTO player_inventories (uuid, inventory_data) VALUES (?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serializedInventory);
            statement.executeUpdate();
            debug.log("database", "Saved inventory for UUID: " + uuid);
        } catch (SQLException e) {
            debug.log("database", "Failed to save inventory for UUID: " + uuid);
            debug.userError(null, "SQLite savePlayerInventory error", e);
        }
    }

    public ItemStack[] getPlayerInventory(UUID uuid) {
        if (connection == null) {
            debug.log("database", "ERROR: SQLite connection NULL during getPlayerInventory!");
            return null;
        }

        String sql = "SELECT inventory_data FROM player_inventories WHERE uuid = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String inventoryData = resultSet.getString("inventory_data");
                debug.log("database", "Loaded inventory for UUID: " + uuid);
                return deserializeInventory(inventoryData);
            }

        } catch (SQLException e) {
            debug.log("database", "Failed to retrieve inventory for UUID: " + uuid);
            debug.userError(null, "SQLite getPlayerInventory error", e);
        }

        return null;
    }

    public void savePlayerCoords(UUID uuid, Location location) {
        if (connection == null) {
            debug.log("database", "ERROR: SQLite connection NULL during savePlayerCoords!");
            return;
        }

        String sql = "INSERT OR REPLACE INTO player_coords (uuid, x, y, z, world) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setString(5, location.getWorld().getName());
            statement.executeUpdate();
            debug.log("database", "Saved coords for UUID: " + uuid);
        } catch (SQLException e) {
            debug.log("database", "Failed to save coordinates for UUID: " + uuid);
            debug.userError(null, "SQLite savePlayerCoords error", e);
        }
    }

    public Location getPlayerCoords(UUID uuid) {
        if (connection == null) {
            debug.log("database", "ERROR: SQLite connection NULL during getPlayerCoords!");
            return null;
        }

        String sql = "SELECT x, y, z, world FROM player_coords WHERE uuid = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                double z = resultSet.getDouble("z");
                String worldName = resultSet.getString("world");

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    debug.log("database", "World '" + worldName + "' not found! Using default world.");
                    world = Bukkit.getWorlds().get(0);
                }

                debug.log("database", "Loaded coords for UUID: " + uuid);
                return new Location(world, x, y, z);
            }

        } catch (SQLException e) {
            debug.log("database", "Failed to retrieve coordinates for UUID: " + uuid);
            debug.userError(null, "SQLite getPlayerCoords error", e);
        }

        return null;
    }

    private String serializeInventory(Inventory inventory) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(inventory.getContents().length);
            for (ItemStack item : inventory.getContents()) {
                dataOutput.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            debug.log("database", "Failed to serialize inventory!");
            debug.userError(null, "SQLite serializeInventory error", e);
            return null;
        }
    }

    private ItemStack[] deserializeInventory(String base64) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            int size = dataInput.readInt();
            ItemStack[] inventoryContents = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                inventoryContents[i] = (ItemStack) dataInput.readObject();
            }
            return inventoryContents;

        } catch (IOException | ClassNotFoundException e) {
            debug.log("database", "Failed to deserialize inventory!");
            debug.userError(null, "SQLite deserializeInventory error", e);
            return new ItemStack[0];
        }
    }

    @Override
    public Report getReportByUuid(UUID uuid) {
        Report report = null;
        String sql = "SELECT * FROM reports WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    report = mapResultSetToReport(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error fetching report by UUID: " + uuid + " | " + e.getMessage());
        }
        return report;
    }

    @Override
    public void saveReport(Report report) {
        String sql = "INSERT OR REPLACE INTO reports (" +
                "uuid, reporter_uuid, target_uuid, reason, server_name, status, assigned_to, created_at, updated_at, closed_at, close_reason, " +
                "location_world, location_x, location_y, location_z, location_yaw, location_pitch, last_updated_by" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, report.getUuid().toString());
            stmt.setString(2, report.getReporterUuid().toString());
            stmt.setString(3, report.getTargetUuid().toString());
            stmt.setString(4, report.getReason());
            stmt.setString(5, report.getServerName());
            stmt.setString(6, report.getStatus().name());
            stmt.setString(7, report.getAssignedTo() != null ? report.getAssignedTo().toString() : null);
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
                stmt.setNull(12, java.sql.Types.VARCHAR);
                stmt.setNull(13, java.sql.Types.DOUBLE);
                stmt.setNull(14, java.sql.Types.DOUBLE);
                stmt.setNull(15, java.sql.Types.DOUBLE);
                stmt.setNull(16, java.sql.Types.FLOAT);
                stmt.setNull(17, java.sql.Types.FLOAT);
            }

            stmt.setString(18, report.getLastUpdatedBy() != null ? report.getLastUpdatedBy().toString() : null);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error saving report: " + e.getMessage());
        }
    }

    @Override
    public void updateReport(Report report) {
        String sql = "UPDATE reports SET " +
                "reporter_uuid = ?, " +
                "target_uuid = ?, " +
                "reason = ?, " +
                "server_name = ?, " +
                "status = ?, " +
                "assigned_to = ?, " +
                "created_at = ?, " +
                "updated_at = ?, " +
                "closed_at = ?, " +
                "close_reason = ?, " +
                "location_world = ?, " +
                "location_x = ?, " +
                "location_y = ?, " +
                "location_z = ?, " +
                "location_yaw = ?, " +
                "location_pitch = ?, " +
                "last_updated_by = ? " +
                "WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, report.getReporterUuid().toString());
            ps.setString(2, report.getTargetUuid().toString());
            ps.setString(3, report.getReason());
            ps.setString(4, report.getServerName());
            ps.setString(5, report.getStatus().name());
            ps.setString(6, report.getAssignedTo() != null ? report.getAssignedTo().toString() : null);
            ps.setLong(7, report.getCreatedAt());
            ps.setLong(8, report.getUpdatedAt());
            ps.setLong(9, report.getClosedAt());
            ps.setString(10, report.getCloseReason());

            if (report.getLocation() != null && report.getLocation().getWorld() != null) {
                ps.setString(11, report.getLocation().getWorld().getName());
                ps.setDouble(12, report.getLocation().getX());
                ps.setDouble(13, report.getLocation().getY());
                ps.setDouble(14, report.getLocation().getZ());
                ps.setFloat(15, report.getLocation().getYaw());
                ps.setFloat(16, report.getLocation().getPitch());
            } else {
                ps.setNull(11, java.sql.Types.VARCHAR);
                ps.setNull(12, java.sql.Types.DOUBLE);
                ps.setNull(13, java.sql.Types.DOUBLE);
                ps.setNull(14, java.sql.Types.DOUBLE);
                ps.setNull(15, java.sql.Types.FLOAT);
                ps.setNull(16, java.sql.Types.FLOAT);
            }

            ps.setString(17, report.getLastUpdatedBy() != null ? report.getLastUpdatedBy().toString() : null);
            ps.setString(18, report.getUuid().toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error updating report: " + e.getMessage());
        }
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Report report = mapResultSetToReport(rs);
                reports.add(report);
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error fetching reports: " + e.getMessage());
        }
        return reports;
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
                float yaw = 0f, pitch = 0f;
                try { yaw = rs.getFloat("location_yaw"); } catch (SQLException ignored) {}
                try { pitch = rs.getFloat("location_pitch"); } catch (SQLException ignored) {}
                location = new Location(world, x, y, z, yaw, pitch);
            }
        }

        ReportStatus status = ReportStatus.valueOf(rs.getString("status").toUpperCase());

        String assignedToStr = rs.getString("assigned_to");
        UUID assignedTo = (assignedToStr != null && !assignedToStr.isEmpty()) ? UUID.fromString(assignedToStr) : null;

        long createdAt = rs.getLong("created_at");
        long updatedAt = rs.getLong("updated_at");
        long closedAt = rs.getLong("closed_at");
        String closeReason = rs.getString("close_reason");

        String lastUpdatedByStr = rs.getString("last_updated_by");
        UUID lastUpdatedBy = (lastUpdatedByStr != null && !lastUpdatedByStr.isEmpty()) ? UUID.fromString(lastUpdatedByStr) : null;

        Report report = new Report(
                uuid,
                reporterUuid,
                targetUuid,
                reason,
                serverName,
                location,
                status,
                assignedTo,
                createdAt,
                updatedAt,
                lastUpdatedBy,
                closedAt,
                closeReason
        );

        try {
            List<StaffNote> notes = getStaffNotesForReport(uuid);
            if (notes != null) {
                report.getStaffNotes().addAll(notes);
            }
        } catch (Exception e) {
            plugin.getDebugManager().log("database", "Error loading staff notes for report " + uuid + ": " + e.getMessage());
        }

        return report;
    }

    @Override
    public List<Report> getReportsByTarget(UUID targetUuid) {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE target_uuid = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, targetUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error fetching reports by target: " + e.getMessage());
        }
        return reports;
    }

    @Override
    public void deleteReport(UUID uuid) {
        String sql = "DELETE FROM reports WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error deleting report: " + e.getMessage());
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
            ps.executeUpdate();
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
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getDebugManager().log("database", "Error updating staff note: " + e.getMessage());
        }
    }
}