package fr.lampalon.lifemod.manager.database;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.database.type.SQLiteManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;

public class DatabaseManager {

    private final LifeMod plugin;
    private DatabaseProvider databaseProvider;
    private SQLiteManager sqliteManager;

    public DatabaseManager(LifeMod plugin) {
        this.plugin = plugin;
        this.sqliteManager = new SQLiteManager(plugin);
    }

    public void setupDatabase() {
        FileConfiguration config = plugin.getConfig();
        String type = config.getString("database.type", "sqlite").toLowerCase();

        switch (type) {
            //case "mysql":
                //databaseProvider = new MySQLManager(plugin);
                //break;
            case "sqlite":
            default:
                databaseProvider = new SQLiteManager(plugin);
                break;
        }

        databaseProvider.setupDatabase();
        sqliteManager.setupDatabase();
        plugin.getLogger().info("[LifeMod] " + type.toUpperCase() + " database initialized successfully.");
    }

    public Connection getConnection(){
        return databaseProvider.getConnection();
    }

    public SQLiteManager getSQLiteManager() {
        return sqliteManager;
    }

    public void closeConnection(){
        if (databaseProvider != null){
            databaseProvider.closeConnection();
        }
    }
}
