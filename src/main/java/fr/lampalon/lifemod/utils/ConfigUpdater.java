package fr.lampalon.lifemod.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

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

        backupFile(file, fileName, userConfig.getString(VERSION_KEY, "unknown"));

        boolean changed = false;
        int added = 0;
        List<String> addedKeys = new ArrayList<>();

        Set<String> defaultKeys = defaultConfig.getKeys(true);
        for (String key : defaultKeys) {
            if (!userConfig.contains(key)) {
                Object defaultVal = defaultConfig.get(key);
                userConfig.set(key, defaultVal);
                added++;
                addedKeys.add(key);
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
                moveVersionKeyToTop(file);
                logSection("§6LifeMod §8| §f" + fileName + " §aupgraded! §7(§a+" + added + " new keys§7, §eversion: " + defaultVersion + "§7)\n§7Added keys: §f" + String.join(", ", addedKeys));
            } catch (IOException e) {
                logSection("§cFailed to save updated §f" + fileName + "§c: " + e.getMessage());
            }
        } else {
            logSection("§6LifeMod §8| §f" + fileName + " §7is up-to-date (§eversion: " + defaultVersion + "§7)");
        }
    }

    private void moveVersionKeyToTop(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String versionLine = null;
            Iterator<String> it = lines.iterator();
            while (it.hasNext()) {
                String line = it.next();
                if (line.trim().startsWith("version:")) {
                    versionLine = line;
                    it.remove();
                    break;
                }
            }
            if (versionLine != null) {
                lines.add(0, versionLine);
                Files.write(file.toPath(), lines);
            }
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§c[LifeMod] Failed to move version key to top: " + e.getMessage());
        }
    }

    private void backupFile(File file, String fileName, String oldVersion) {
        try {
            File backupDir = new File(plugin.getDataFolder(), "backups");
            if (!backupDir.exists()) backupDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String backupName = fileName.replace(".yml", "") + "-v" + oldVersion + "-" + timestamp + ".yml.bak";
            File backupFile = new File(backupDir, backupName);

            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Bukkit.getConsoleSender().sendMessage("§7[§6LifeMod§7] §eBackup created: §fbackups/" + backupName);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§c[LifeMod] Failed to backup " + fileName + ": " + e.getMessage());
        }
    }

    private void logSection(String message) {
        Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
        Bukkit.getConsoleSender().sendMessage(message);
        Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
    }
}
