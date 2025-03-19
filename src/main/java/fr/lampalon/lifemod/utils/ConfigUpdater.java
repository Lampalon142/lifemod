package fr.lampalon.lifemod.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ConfigUpdater {
    public static void updateConfig(JavaPlugin plugin, File file, FileConfiguration config, FileConfiguration defaultConfig){
        boolean updated = false;

        Set<String> defaultKeys = defaultConfig.getKeys(true);
        for (String key : defaultKeys){
            if (!config.contains(key)){
                Object value = defaultConfig.get(key);
                config.set(key, value);
                updated = true;
                plugin.getLogger().info("Adding missing keys for " + key);
            }
        }

        if (updated) {
            try {
                config.save(file);
                plugin.getLogger().info("Config file updated successfully");
            } catch (IOException e) {
                plugin.getLogger().severe("ERROR during saving configuration : " + e.getMessage());
            }
        } else {
            plugin.getLogger().info("Nothing update required for config file.");
        }
    }
}
