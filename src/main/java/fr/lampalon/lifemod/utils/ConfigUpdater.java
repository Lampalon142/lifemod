package fr.lampalon.lifemod.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public class ConfigUpdater {
    private final JavaPlugin plugin;
    private final String VERSION_KEY = "version";

    public ConfigUpdater(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void updateConfigs() {
        updateFile("config.yml");
        updateFile("lang.yml");
    }

    private void updateFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            logSection("§aCreated missing §f" + fileName + "§a from plugin resources.");
            return;
        }

        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(file);

        FileConfiguration defaultConfig;
        try (InputStreamReader reader = new InputStreamReader(plugin.getResource(fileName))) {
            defaultConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            logSection("§cFailed to load default §f" + fileName + "§c: " + e.getMessage());
            return;
        }

        boolean changed = false;
        int added = 0;

        Set<String> keys = defaultConfig.getKeys(true);
        for (String key : keys) {
            if (!userConfig.contains(key)) {
                userConfig.set(key, defaultConfig.get(key));
                added++;
                changed = true;
            }
        }

        String defaultVersion = defaultConfig.getString(VERSION_KEY);
        if (defaultVersion != null && !defaultVersion.equals(userConfig.getString(VERSION_KEY))) {
            userConfig.set(VERSION_KEY, defaultVersion);
            changed = true;
        }

        if (changed) {
            try {
                userConfig.save(file);
                logSection("§6LifeMod §8| §f" + fileName + " §aupdated! §7(§a+" + added + " new keys§7, §eversion: " + defaultVersion + "§7)");
            } catch (IOException e) {
                logSection("§cFailed to save updated §f" + fileName + "§c: " + e.getMessage());
            }
        } else {
            logSection("§6LifeMod §8| §f" + fileName + " §7is up-to-date (§eversion: " + defaultVersion + "§7)");
        }
    }

    private void logSection(String message) {
        Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
        Bukkit.getConsoleSender().sendMessage(message);
        Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
    }
}
