package fr.lampalon.lifemod.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private final String databasePath;
    private Connection connection;

    public DatabaseManager(String pluginFolderPath) {
        this.databasePath = pluginFolderPath + "/data.db";
    }

    public void setupDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_coords (
                        uuid TEXT PRIMARY KEY,
                        x DOUBLE,
                        y DOUBLE,
                        z DOUBLE,
                        world TEXT
                    );
                """);

                statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_inventories (
                        uuid TEXT PRIMARY KEY,
                        inventory_data TEXT
                    );
                """);
            }

            Bukkit.getLogger().info("[LifeMod] SQLite database initialized successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("[LifeMod] Failed to initialize SQLite database.");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerCoords(UUID uuid, Location location) {
        String sql = "INSERT OR REPLACE INTO player_coords (uuid, x, y, z, world) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, location.getX());
            statement.setDouble(3, location.getY());
            statement.setDouble(4, location.getZ());
            statement.setString(5, location.getWorld().getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getPlayerCoords(UUID uuid) {
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

                if (world != null) {
                    return new Location(world, x, y, z);
                } else {
                    Bukkit.getLogger().warning("[LifeMod] world not found : " + worldName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
