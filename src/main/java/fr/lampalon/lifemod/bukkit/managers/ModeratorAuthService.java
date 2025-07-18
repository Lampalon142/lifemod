package fr.lampalon.lifemod.bukkit.managers;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.database.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ModeratorAuthService {
    private final LifeMod plugin;
    private final DatabaseManager dbManager;

    public ModeratorAuthService(LifeMod plugin) {
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS moderator_auth (" +
                             "uuid VARCHAR(36) PRIMARY KEY," +
                             "name VARCHAR(32)," +
                             "password_hash VARCHAR(256) NOT NULL," +
                             "ip VARCHAR(64)," +
                             "last_update BIGINT" +
                             ")"
             )) {
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[LifeMod] Unable to create moderator_auth table: " + e.getMessage());
        }
    }

    public boolean isRegistered(UUID uuid) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM moderator_auth WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("[LifeMod] isRegistered error: " + e.getMessage());
            return false;
        }
    }

    public void registerModerator(UUID uuid, String name, String password, String ip) {
        String hash = hash(password);
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO moderator_auth (uuid, name, password_hash, ip, last_update) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setString(3, hash);
            ps.setString(4, ip);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[LifeMod] registerModerator error: " + e.getMessage());
        }
    }

    public void resetModeratorPassword(UUID uuid) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM moderator_auth WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[LifeMod] resetModeratorPassword error: " + e.getMessage());
        }
    }

    public String getStoredIp(UUID uuid) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ip FROM moderator_auth WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("ip");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("[LifeMod] getStoredIp error: " + e.getMessage());
        }
        return null;
    }

    public void updateIp(UUID uuid, String ip) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE moderator_auth SET ip = ?, last_update = ? WHERE uuid = ?")) {
            ps.setString(1, ip);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[LifeMod] updateIp error: " + e.getMessage());
        }
    }

    public UUID getUUIDByName(String name) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT uuid, name FROM moderator_auth WHERE LOWER(name) = LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String uuidStr = rs.getString("uuid");
                    String dbName = rs.getString("name");
                    if (!dbName.equals(name)) {
                        updateName(UUID.fromString(uuidStr), name);
                    }
                    return UUID.fromString(uuidStr);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("[LifeMod] getUUIDByName error: " + e.getMessage());
        }
        return null;
    }

    public void updateName(UUID uuid, String newName) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE moderator_auth SET name = ? WHERE uuid = ?")) {
            ps.setString(1, newName);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("[LifeMod] updateName error: " + e.getMessage());
        }
    }

    private String hash(String input) {
        return BCrypt.hashpw(input, BCrypt.gensalt(12));
    }

    public boolean checkPassword(UUID uuid, String password) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT password_hash FROM moderator_auth WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    return BCrypt.checkpw(password, hash);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("[LifeMod] checkPassword error: " + e.getMessage());
        }
        return false;
    }

    public void changePassword(UUID uuid, String newPassword) {
        String hash = hash(newPassword);
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE moderator_auth SET password_hash = ?, last_update = ? WHERE uuid = ?")) {
            ps.setString(1, hash);
            ps.setLong(2, System.currentTimeMillis());
            ps.setString(3, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[LifeMod] changePassword error: " + e.getMessage());
        }
    }
}
