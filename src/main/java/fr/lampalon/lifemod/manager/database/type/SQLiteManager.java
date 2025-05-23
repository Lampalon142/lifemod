package fr.lampalon.lifemod.manager.database.type;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.database.DatabaseProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

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
            File dbFile = new File(plugin.getDataFolder(), Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("database.sqlite.file")));
            if (!dbFile.exists()) {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
                debug.log("database", "Created new SQLite database file.");
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

        try (Statement statement = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS player_coords ("
                    + "uuid TEXT PRIMARY KEY, "
                    + "x DOUBLE, "
                    + "y DOUBLE, "
                    + "z DOUBLE, "
                    + "world TEXT"
                    + ");";
            statement.executeUpdate(sql);

            String sql2 = "CREATE TABLE IF NOT EXISTS player_inventories ("
                    + "uuid TEXT PRIMARY KEY, "
                    + "inventory_data TEXT"
                    + ");";
            statement.executeUpdate(sql2);

            debug.log("database", "SQLite tables initialized.");
        } catch (SQLException e) {
            debug.log("database", "ERROR: Failed to initialize setupDatabase()!");
            debug.userError(null, "SQLite table creation error", e);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                debug.log("database", "SQLite connection closed.");
            }
        } catch (SQLException e) {
            debug.log("database", "Error while closing SQLite connection!");
            debug.userError(null, "SQLite close connection error", e);
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
}