package fr.lampalon.lifemod.bukkit.managers.database;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.database.type.MySQLManager;
import fr.lampalon.lifemod.bukkit.managers.database.type.SQLiteManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private final LifeMod plugin;
    private final DebugManager debug;
    private DatabaseProvider databaseProvider;

    public DatabaseManager(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    public void setupDatabase() {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "sqlite").toLowerCase();

        try {
            switch (type) {
                case "mysql":
                    databaseProvider = new MySQLManager(plugin);
                    break;
                case "sqlite":
                default:
                    databaseProvider = new SQLiteManager(plugin);
                    break;
            }

            databaseProvider.setupDatabase();
            debug.log("database", type + " database initialized.");
            plugin.getLogger().info(type.toUpperCase() + " database initialized successfully.");
        } catch (Exception e) {
            debug.userError(null, "Error during database initialization.", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public Connection getConnection() throws SQLException {
        if (databaseProvider == null) {
            throw new SQLException("Database provider not initialized!");
        }
        return databaseProvider.getConnection();
    }

    public DatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    public void closeConnection() {
        try {
            if (databaseProvider != null) {
                databaseProvider.closeConnection();
                debug.log("database", "Database connection closed.");
            }
        } catch (Exception e) {
            debug.userError(null, "Error while closing database connection.", e);
        }
    }
}
