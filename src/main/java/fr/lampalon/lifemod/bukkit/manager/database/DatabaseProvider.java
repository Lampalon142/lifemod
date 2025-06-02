package fr.lampalon.lifemod.bukkit.manager.database;

import java.sql.Connection;

public interface DatabaseProvider {
    void setupDatabase();
    Connection getConnection();
    void closeConnection();
}
