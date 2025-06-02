package fr.lampalon.lifemod.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bukkit.command.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class LifeMod extends Plugin {
    public static LifeMod instance;
    private Configuration config;
    private Configuration lang;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            getLogger().info("Data folder created: " + dataFolder.getAbsolutePath());
        }

        copyResourceIfNotExists("bungee/config.yml", new File(dataFolder, "config.yml"));
        copyResourceIfNotExists("bungee/lang.yml",   new File(dataFolder, "lang.yml"));

        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(dataFolder, "config.yml"));
            this.lang   = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(dataFolder, "lang.yml"));
            getLogger().info("config.yml and lang.yml loaded from " + dataFolder.getAbsolutePath());
        } catch (IOException e) {
            getLogger().severe("Error loading config.yml and lang.yml!");
            e.printStackTrace();
        }
        registerCommands();

        long elapsed = System.currentTimeMillis() - start;
        getLogger().info("§8§m----------------------------------------");
        getLogger().info("§6LifeMod §7v" + getDescription().getVersion() + " §8| §fby Lampalon");
        getLogger().info("§7Java: §e" + System.getProperty("java.version") + " §8| §7BungeeCord: §e" + ProxyServer.getInstance().getVersion());
        getLogger().info("§7Startup Time: §b" + elapsed + "ms");
        getLogger().info("§8§m----------------------------------------");
    }

    @Override
    public void onDisable() {
        PluginManager pm = ProxyServer.getInstance().getPluginManager();
        pm.unregisterCommands(this);
        getLogger().info("LifeMod disabled.");
    }

    private void registerCommands() {
        PluginManager pm = ProxyServer.getInstance().getPluginManager();
    }

    private CommandMap getCommandMap() {
        try {
            Field commandMapField = getProxy().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get( getProxy());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        if (getConfigYml().getBoolean("commands-enabled." + commandName, true)) {
            getCommand(commandName).setExecutor(executor);
            if (executor instanceof TabCompleter) {
                getCommand(commandName).setTabCompleter((TabCompleter) executor);
            }
        } else {
            unregisterCommand(commandName);
        }
    }

    private PluginCommand getCommand(String commandName) {}

    private void unregisterCommand(String commandName) {
        try {
            Field commandMapField = getProxy().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(getProxy());

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, org.bukkit.command.Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            if (knownCommands.containsKey(commandName)) {
                knownCommands.remove(commandName).unregister(commandMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyResourceIfNotExists(String resourceName, File destinationFile) {
        if (destinationFile.exists()) {
            return;
        }

        try (InputStream in = getResourceAsStream(resourceName)) {
            if (in == null) {
                getLogger().severe("Resource stream for '" + resourceName + "' not found in the JAR!");
                return;
            }
            Files.copy(in, destinationFile.toPath());
            getLogger().info("Copied resource '" + resourceName + "' to " + destinationFile.getAbsolutePath());
        } catch (IOException e) {
            getLogger().severe("Error copying '" + resourceName + "'!");
            e.printStackTrace();
        }
    }

    public Configuration getConfigYml() {
        return this.config;
    }

    public Configuration getLangYml() {
        return this.lang;
    }

    public static LifeMod getInstance() {
        return instance;
    }
}
