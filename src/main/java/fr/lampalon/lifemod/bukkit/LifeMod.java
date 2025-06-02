package fr.lampalon.lifemod.bukkit;

import fr.lampalon.lifemod.bukkit.commands.*;
import fr.lampalon.lifemod.bukkit.listeners.*;
import fr.lampalon.lifemod.bukkit.manager.*;
import fr.lampalon.lifemod.bukkit.manager.database.DatabaseManager;
import fr.lampalon.lifemod.bukkit.utils.ConfigUpdater;
import fr.lampalon.lifemod.bukkit.utils.UpdateChecker;
import fr.lampalon.lifemod.bukkit.manager.PlayerManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

public class LifeMod extends JavaPlugin {
    private static LifeMod instance;
    private CommandMap commandMap;
    private SpectateManager spectateManager;
    private FreezeManager freezeManager;
    private DatabaseManager databaseManager;
    private UpdateChecker updateChecker;
    private DebugManager debugManager;
    private ChatManager chatManager;
    private VanishedManager playerManager;
    private FileConfiguration configConfig;
    private FileConfiguration langConfig;
    private Set<UUID> moderators = new HashSet<>();
    private Map<UUID, PlayerManager> players = new HashMap<>();
    private Map<UUID, Location> frozenPlayers = new HashMap<>();
    public String webHookUrl = getConfig().getString("discord.webhookurl");

    public static LifeMod getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;
        saveDefaultConfig();
        new ConfigUpdater(this).updateConfigs();
        loadConfigurations();
        this.spectateManager = new SpectateManager();
        this.debugManager = new DebugManager(this);
        this.databaseManager = new DatabaseManager(this);
        initializeManagers();
        registerEvents();
        registerCommands();
        setupMetrics();
        String dbStatus = "§cFailed";
        try {
            if (databaseManager != null && databaseManager.getConnection() != null && !databaseManager.getConnection().isClosed()) {
                dbStatus = "§aConnected";
            }
        } catch (Exception e) {
            dbStatus = "§cError";
        }
        long elapsed = System.currentTimeMillis() - start;
        Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
        Bukkit.getConsoleSender().sendMessage("§6LifeMod §7v" + getDescription().getVersion() + " §8| §fby Lampalon");
        Bukkit.getConsoleSender().sendMessage("§7Java: §e" + System.getProperty("java.version") + " §8| §7Server: §e" + Bukkit.getVersion());
        Bukkit.getConsoleSender().sendMessage("§7Database: " + dbStatus);
        Bukkit.getConsoleSender().sendMessage("§7Startup time: §b" + elapsed + "ms");
        Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
    }

    private void loadConfigurations() {
        configConfig = loadConfig("config.yml");
        langConfig = loadConfig("lang.yml");
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            getDataFolder().mkdirs();
            saveResource(fileName, false);
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(getResource(fileName))
        );

        return config;
    }

    private void initializeManagers() {
        freezeManager = new FreezeManager();
        playerManager = new VanishedManager();
        chatManager = new ChatManager(this);
        databaseManager = new DatabaseManager(this);
        databaseManager.setupDatabase();
    }

    private void setupMetrics() {
        int pluginId = 19817;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SingleLineChart("players", () -> Bukkit.getOnlinePlayers().size()));
    }

    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        updateChecker = new UpdateChecker(this, 112381);
        pm.registerEvents(new ModCancels(), this);
        pm.registerEvents(new ModItemsInteract(), this);
        pm.registerEvents(new Staffchatevent(this), this);
        pm.registerEvents(new PluginDisable(), this);
        pm.registerEvents(new PlayerQuit(), this);
        pm.registerEvents(new FreezeGui(this), this);
        pm.registerEvents(new PlayerTeleportEvent(), this);
        if (getLangConfig().getBoolean("general.update.enabled")) {
            pm.registerEvents(new PlayerJoin(this, updateChecker), this);
        }
    }

    private void registerCommands() {
        commandMap = getCommandMap();
        registerCommand("freeze", new FreezeCmd(this));
        registerCommand("mod", new ModCmd(this));
        registerCommand("staff", new ModCmd(this));
        registerCommand("broadcast", new BroadcastCmd(this));
        registerCommand("bc", new BroadcastCmd(this));
        registerCommand("gamemode", new GmCmd(this));
        registerCommand("gm", new GmCmd(this));
        registerCommand("fly", new FlyCmd(this));
        registerCommand("ecopen", new EcopenCmd(this));
        registerCommand("vanish", new VanishCmd(playerManager));
        registerCommand("clearinv", new ClearinvCmd(this));
        registerCommand("stafflist", new StafflistCmd());
        registerCommand("staffchat", new StaffchatCmd());
        registerCommand("chatclear", new ChatclearCmd(this));
        registerCommand("heal", new HealCmd(this));
        registerCommand("tp", new TeleportCmd());
        registerCommand("tphere", new TeleportCmd());
        registerCommand("god", new GodModCmd(this));
        registerCommand("invsee", new InvseeCmd(this));
        registerCommand("feed", new FeedCmd(this));
        registerCommand("weather", new WeatherCmd(this));
        registerCommand("lifemod", new LifemodCmd(this));
        registerCommand("speed", new SpeedCmd());
        registerCommand("spectate", new SpectateCmd(this));
        registerCommand("otp", new OtpCmd(databaseManager));
        registerCommand("oinvsee", new OInvseeCmd(databaseManager));
        registerCommand("settime", new TimeCmd(this));
        registerCommand("difficulty", new DifficultyCmd(this));
        registerCommand("hearts", new HeartsCmd(this));
    }

    private CommandMap getCommandMap() {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(getServer());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        if (getConfig().getBoolean("commands-enabled." + commandName, true)) {
            getCommand(commandName).setExecutor(executor);
            if (executor instanceof TabCompleter) {
                getCommand(commandName).setTabCompleter((TabCompleter) executor);
            }
        } else {
            unregisterCommand(commandName);
        }
    }

    private void unregisterCommand(String commandName) {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(getServer());

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            if (knownCommands.containsKey(commandName)) {
                knownCommands.remove(commandName).unregister(commandMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().stream()
                .filter(PlayerManager::isInModerationMod)
                .forEach(p -> PlayerManager.getFromPlayer(p).destroy());
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }

    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    public FileConfiguration getConfigConfig() { return configConfig; }

    public SpectateManager getSpectateManager() { return spectateManager; }

    public DebugManager getDebugManager() { return debugManager; }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public Set<UUID> getModerators() {
        return moderators;
    }

    public VanishedManager getPlayerManager() {
        return playerManager;
    }

    public Map<UUID, PlayerManager> getPlayers() {
        return players;
    }

    public Map<UUID, Location> getFrozenPlayers() {
        return frozenPlayers;
    }

    public boolean isFreeze(Player player) {
        return frozenPlayers.containsKey(player.getUniqueId());
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public void reloadPluginConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        this.configConfig = YamlConfiguration.loadConfiguration(configFile);
    }
    public void reloadLangConfig() {
        File langFile = new File(getDataFolder(), "lang.yml");
        this.langConfig = YamlConfiguration.loadConfiguration(langFile);
    }
}