package fr.lampalon.lifemod.data.configuration;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.JavaUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Options {
    private static FileConfiguration config = LifeMod.getInstance().getConfig();
    private InputStream stream = LifeMod.getInstance().getResource("config.yml");
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(this.stream, StandardCharsets.UTF_8));
    public List<String> spamprotectmaxmessage = JavaUtils.stringToList(config.getString("spam-protection.max-messages", ""));
    private int configVersion = this.configuration.getInt("version");
    // Permissions //
    public String permissionWildcard = config.getString("permissions.wild-card");
    public String permissionMember = config.getString("permissions.member");
    // Database //
    public String storageType = config.getString("storage.type", "flatfile");
    public String mySqlHost = config.getString("storage.mysql.host", "localhost");
    public String mySqlUser = config.getString("storage.mysql.user", "root");
    public String database = config.getString("storage.mysql.database", "lifemod");
    public String mySqlPassword = config.getString("storage.mysql.password", "root");
    public int mySqlPort = config.getInt("storage.mysql.port", Integer.parseInt("3306"));

    //
    public Options(){
        if (101 < this.configVersion){
            File dataFolder = LifeMod.getInstance().getDataFolder();
            File configFile = new File(dataFolder, "config.yml");
            YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(configFile);
            String backup = "backup-#101.yml";
            configFile.renameTo(new File(dataFolder, backup));
            LifeMod.getInstance().getConfig().options().copyDefaults(true);
            config.set("config-version", Integer.valueOf(101));
            config.options().header(" LifeMod | Made and Directed by Lampalon_ with love\n Your configuration file has been autmatically updated to lastest version. (101)\n Note : All data has been deleted but you have your config on 'backup-#101.yml' !");
            config.options().copyHeader();
            config.set("config-version", Integer.valueOf(this.configVersion));
            LifeMod.getInstance().saveConfig();
        }
    }
    public YamlConfiguration getConfiguration(){
        return YamlConfiguration.loadConfiguration(new File(LifeMod.getInstance().getDataFolder(), "config.yml"));
    }
    public Options reloadConfig(){
        return new Options();
    }
    private void updateConfig(){
        File dataFolder = LifeMod.getInstance().getDataFolder();
        File configFile = new File(dataFolder, "config.yml");
        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(configFile);
        String backup = "backup-#101.yml";
        String currentKey = "";
        configFile.renameTo(new File(dataFolder, backup));
        LifeMod.getInstance().saveDefaultConfig();
        config = LifeMod.getInstance().getConfig();
        for (String key : oldConfig.getConfigurationSection("").getKeys(true)){
            if (key.equalsIgnoreCase("config-version")){
                config.set(key, Integer.valueOf(101));
                continue;
            }
            config.set(key, oldConfig.get(key));
        }
        for (String key : config.getConfigurationSection("").getKeys(true)){
            if (!key.contains("."))
                currentKey = key;
            if (!oldConfig.contains(key))
                config.set(currentKey + key, config.get(key));
            config.options().header(" LifeMod | Made and Directed by Lampalon_ with love\n Your configuration file has been autmatically updated to lastest version. (101)\n Note : All data has been deleted but you have your config on 'backup-#101.yml' !");
            config.options().copyHeader(true);
            LifeMod.getInstance().saveConfig();
            Bukkit.getConsoleSender().sendMessage("Your config has been updated to #101!");
        }
    }
}
