package fr.lampalon.lifemod.bukkit.manager.database;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.manager.database.type.SQLiteManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;

public class DatabaseManager {

    private final LifeMod plugin;
    private final DebugManager debug;
    private DatabaseProvider databaseProvider;
    private SQLiteManager sqliteManager;

    public DatabaseManager(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
        this.sqliteManager = new SQLiteManager(plugin);
    }

    public void setupDatabase() {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "sqlite").toLowerCase();

        try {
            switch (type) {
                case "sqlite":
                    databaseProvider = new SQLiteManager(plugin);
                    break;
                default:
                    debug.log("database", "Unknown database type: " + type);
                    break;
            }

            if (databaseProvider != null) {
                databaseProvider.setupDatabase();
                debug.log("database", type + " database initialized.");
            }
            if (sqliteManager != null) {
                sqliteManager.setupDatabase();
            }
            plugin.getLogger().info( type.toUpperCase() + " database initialized successfully.");
        } catch (Exception e) {
            debug.userError(null, "Error during database initialization.", e);
        }
    }

    public Connection getConnection() {
        return databaseProvider.getConnection();
    }

    public SQLiteManager getSQLiteManager() {
        return sqliteManager;
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