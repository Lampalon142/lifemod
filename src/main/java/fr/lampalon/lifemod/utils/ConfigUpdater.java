package fr.lampalon.lifemod.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigUpdater {
    private final JavaPlugin plugin;
    private final String CONFIG_VERSION_KEY = "version";
    private final String LANG_VERSION_KEY = "version";

    public ConfigUpdater(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateConfig() {
        updateFile("config.yml", CONFIG_VERSION_KEY);
        updateFile("lang.yml", LANG_VERSION_KEY);
    }

    private void updateFile(String fileName, String versionKey) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String currentVersion = plugin.getDescription().getVersion();
        String fileVersion = config.getString(versionKey, "0");

        if (!config.contains(versionKey)) {
            config.set(versionKey, currentVersion);
            try {
                config.save(file);
                plugin.getLogger().info("Added missing version updater to config.yml and lang.yml");
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to add missing version key in " + fileName);
            }
        }

        if (!currentVersion.equals(fileVersion)) {
            backupOldFile(fileName, fileVersion);
            generateNewFile(fileName);
        }
    }

    private void backupOldFile(String fileName, String oldVersion) {
        File file = new File(plugin.getDataFolder(), fileName);
        File backupFile = new File(plugin.getDataFolder(), "backup-" + oldVersion + "-" + fileName);
        try {
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create backup for " + fileName + ": " + e.getMessage());
        }
    }

    private void generateNewFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (file.delete()) {
            plugin.saveResource(fileName, false);
            plugin.getLogger().info("Generated a fresh " + fileName + " with new settings.");
        } else {
            plugin.getLogger().warning("Failed to delete old " + fileName + ". Check permissions.");
        }
    }
}
