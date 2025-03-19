package fr.lampalon.lifemod.manager.database.type;

import fr.lampalon.lifemod.LifeMod;
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
import java.util.UUID;

public class SQLiteManager implements DatabaseProvider {

    private final LifeMod plugin;
    private Connection connection;

    public SQLiteManager(LifeMod plugin) {
        this.plugin = plugin;
        connect();
    }

    private void connect() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            if (!dbFile.exists()) {
                plugin.getLogger().info("");
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            }

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);

            if (connection != null) {
                plugin.getLogger().info("SQLite connection established successfully.");
            } else {
                plugin.getLogger().severe("ERROR: SQLite connection is NULL!");
            }

        } catch (IOException | SQLException e) {
            plugin.getLogger().severe("Unable to establish SQLite connection!");
            e.printStackTrace();
        }
    }

    @Override
    public void setupDatabase() {
        if (connection == null) {
            plugin.getLogger().severe("Unable to set up database: connection is NULL!");
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

        } catch (SQLException e) {
            plugin.getLogger().severe("[LifeMod] ERROR: Failed to initialize \"setupDatabase()\"!");
            e.printStackTrace();
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
                plugin.getLogger().info("[LifeMod] SQLite connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[LifeMod] Error while closing SQLite connection!");
            e.printStackTrace();
        }
    }

    public void savePlayerInventory(UUID uuid, Inventory inventory) {
        if (connection == null) {
            plugin.getLogger().severe("ERROR: SQLite connection NULL during savePlayerInventory!");
            return;
        }

        String serializedInventory = serializeInventory(inventory);
        if (serializedInventory == null) return;

        String sql = "INSERT OR REPLACE INTO player_inventories (uuid, inventory_data) VALUES (?, ?);";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, serializedInventory);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save inventory for UUID: " + uuid);
            e.printStackTrace();
        }
    }

    public ItemStack[] getPlayerInventory(UUID uuid) {
        if (connection == null) {
            plugin.getLogger().severe("ERROR: SQLite connection NULL during getPlayerInventory!");
            return null;
        }

        String sql = "SELECT inventory_data FROM player_inventories WHERE uuid = ?;";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String inventoryData = resultSet.getString("inventory_data");
                return deserializeInventory(inventoryData);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve inventory for UUID: " + uuid);
            e.printStackTrace();
        }

        return null;
    }

    public void savePlayerCoords(UUID uuid, Location location) {
        if (connection == null) {
            plugin.getLogger().severe("ERROR: SQLite connection NULL during savePlayerCoords!");
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
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save coordinates for UUID: " + uuid);
            e.printStackTrace();
        }
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
            e.printStackTrace();
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
            e.printStackTrace();
            return new ItemStack[0];
        }
    }

    public Location getPlayerCoords(UUID uuid) {
        if (connection == null) {
            plugin.getLogger().severe("ERROR: SQLite connection NULL during getPlayerCoords!");
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
                    plugin.getLogger().warning("World '" + worldName + "' not found! Using default world.");
                    world = Bukkit.getWorlds().get(0);
                }

                return new Location(world, x, y, z);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve coordinates for UUID: " + uuid);
            e.printStackTrace();
        }

        return null;
    }
}