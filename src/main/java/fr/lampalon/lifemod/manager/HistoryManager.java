package fr.lampalon.lifemod.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryManager {
    private File file;
    private FileConfiguration config;

    public HistoryManager() {
        file = new File("plugins/LifeMod/data.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void logConnection(Player player) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());

        ConfigurationSection connectionSection = config.createSection("connections." + player.getName() + "." + date);
        connectionSection.set("status", "connected");

        saveConfig();
    }

    public void logDisconnection(Player player) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());

        ConfigurationSection disconnectionSection = config.createSection("connections." + player.getName() + "." + date);
        disconnectionSection.set("status", "disconnected");

        saveConfig();
    }

    public void logAction(Player player, String action) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());

        ConfigurationSection actionSection = config.createSection("actions." + player.getName() + "." + date);
        actionSection.set("action", action);

        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
