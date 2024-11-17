package fr.lampalon.lifemod;

import fr.lampalon.lifemod.commands.*;
import fr.lampalon.lifemod.listeners.moderation.FreezeGui;
import fr.lampalon.lifemod.listeners.moderation.ModCancels;
import fr.lampalon.lifemod.listeners.moderation.ModItemsInteract;
import fr.lampalon.lifemod.listeners.players.PlayerJoin;
import fr.lampalon.lifemod.listeners.players.PlayerQuit;
import fr.lampalon.lifemod.listeners.utils.PluginDisable;
import fr.lampalon.lifemod.listeners.utils.Staffchatevent;
import fr.lampalon.lifemod.manager.ChatManager;
import fr.lampalon.lifemod.manager.FreezeManager;
import fr.lampalon.lifemod.manager.PlayerManager;
import fr.lampalon.lifemod.manager.VanishedManager;
import fr.lampalon.lifemod.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class LifeMod extends JavaPlugin {
    private static LifeMod instance;
    public CommandMap commandMap;
    public String webHookUrl = getConfig().getString("discord.webhookurl");
    private FreezeManager freezeManager;
    private UpdateChecker updateChecker;
    private ChatManager chatManager;
    private VanishedManager playerManager;
    private File ConfigFile;
    private FileConfiguration ConfigConfig;
    private ArrayList<UUID> moderators;
    private HashMap<UUID, PlayerManager> players;
    private HashMap<UUID, Location> frozenPlayers;
    private File LangFile;
    private FileConfiguration LangConfig;

    public static LifeMod getInstance() {
        return instance;
    }

    public void onEnable() {
        CommandHandler();
        ConfigConfig();
        LangConfig();
        instance = this;
        freezeManager = new FreezeManager();
        this.frozenPlayers = new HashMap<>();
        this.players = new HashMap<>();
        this.moderators = new ArrayList<>();
        registerEvents();
        registerCommands();
        saveDefaultConfig();
        chatManager = new ChatManager(this);
        Bukkit.getConsoleSender().sendMessage("§aLifeMod developed by Lampalon with §4<3 §awas been successfully initialised");
        utils();
    }

    private void utils() {
        int pluginId = 19817;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SingleLineChart("players", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return Bukkit.getOnlinePlayers().size();
            }
        }));
    }

    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ModCancels(), this);
        pm.registerEvents(new ModItemsInteract(), this);
        pm.registerEvents(new Staffchatevent(this), this);
        pm.registerEvents(new PluginDisable(), this);
        pm.registerEvents(new PlayerQuit(), this);
        pm.registerEvents(new FreezeGui(this), this);
        updateChecker = new UpdateChecker(this, 112381);
        pm.registerEvents(new PlayerJoin(this, updateChecker), this);
    }

    private void registerCommands() {
        playerManager = new VanishedManager();
    }

    private void CommandHandler() {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getConfig().getBoolean("commands-enabled.freeze", true)) {
            getCommand("freeze").setExecutor(new FreezeCmd(this));
        } else {
            unregisterCommand("freeze");
        }

        if (getConfig().getBoolean("commands-enabled.mod", true)) {
            getCommand("mod").setExecutor(new ModCommand());
            getCommand("staff").setExecutor(new ModCommand());
        } else {
            unregisterCommand("mod");
            unregisterCommand("staff");
        }

        if (getConfig().getBoolean("commands-enabled.broadcast", true)) {
            getCommand("broadcast").setExecutor(new BroadcastCmd());
            getCommand("bc").setExecutor(new BroadcastCmd());
            getCommand("broadcast").setTabCompleter(new BroadcastCmd());
            getCommand("bc").setTabCompleter(new BroadcastCmd());
        } else {
            unregisterCommand("broadcast");
            unregisterCommand("bc");
        }

        if (getConfig().getBoolean("commands-enabled.gamemode", true)) {
            getCommand("gm").setExecutor(new GmCmd());
            getCommand("gm").setTabCompleter(new GmCmd());
            getCommand("gamemode").setTabCompleter(new GmCmd());
        } else {
            unregisterCommand("gm");
        }

        if (getConfig().getBoolean("commands-enabled.fly", true)) {
            getCommand("fly").setExecutor(new FlyCmd());
            getCommand("fly").setTabCompleter(new FlyCmd());
        } else {
            unregisterCommand("fly");
        }

        if (getConfig().getBoolean("commands-enabled.ecopen", true)) {
            getCommand("ecopen").setExecutor(new EcopenCmd());
            getCommand("ecopen").setTabCompleter(new EcopenCmd());
        } else {
            unregisterCommand("ecopen");
        }

        if (getConfig().getBoolean("commands-enabled.vanish", true)) {
            getCommand("vanish").setExecutor(new VanishCmd(playerManager));
        } else {
            unregisterCommand("vanish");
        }

        if (getConfig().getBoolean("commands-enabled.clearinv", true)) {
            getCommand("clearinv").setExecutor(new ClearinvCmd());
            getCommand("clearinv").setTabCompleter(new ClearinvCmd());
        } else {
            unregisterCommand("clearinv");
        }

        if (getConfig().getBoolean("commands-enabled.stafflist", true)) {
            getCommand("stafflist").setExecutor(new StafflistCmd());
        } else {
            unregisterCommand("stafflist");
        }

        if (getConfig().getBoolean("commands-enabled.staffchat", true)) {
            getCommand("staffchat").setExecutor(new StaffchatCmd());
            getCommand("staffchat").setTabCompleter(new StaffchatCmd());
        } else {
            unregisterCommand("staffchat");
        }

        if (getConfig().getBoolean("commands-enabled.chatclear", true)) {
            getCommand("chatclear").setExecutor(new ChatclearCmd());
        } else {
            unregisterCommand("chatclear");
        }

        if (getConfig().getBoolean("commands-enabled.heal", true)) {
            getCommand("heal").setExecutor(new HealCmd());
            getCommand("heal").setTabCompleter(new HealCmd());
        } else {
            unregisterCommand("heal");
        }

        if (getConfig().getBoolean("commands-enabled.teleport", true)) {
            getCommand("tp").setExecutor(new TeleportCmd());
            getCommand("tphere").setExecutor(new TeleportCmd());
            getCommand("tp").setTabCompleter(new TeleportCmd());
            getCommand("tphere").setTabCompleter(new TeleportCmd());
        } else {
            unregisterCommand("tp");
            unregisterCommand("tphere");
        }

        if (getConfig().getBoolean("commands-enabled.godmode", true)) {
            getCommand("god").setExecutor(new GodModCmd());
            getCommand("god").setTabCompleter(new GodModCmd());
        } else {
            unregisterCommand("god");
        }

        if (getConfig().getBoolean("commands-enabled.invsee", true)) {
            getCommand("invsee").setExecutor(new InvseeCmd(this));
            getCommand("invsee").setTabCompleter(new InvseeCmd(this));
        } else {
            unregisterCommand("invsee");
        }

        if (getConfig().getBoolean("commands-enabled.feed", true)) {
            getCommand("feed").setExecutor(new FeedCmd());
            getCommand("feed").setTabCompleter(new FeedCmd());
        } else {
            unregisterCommand("feed");
        }

        if (getConfig().getBoolean("commands-enabled.weather", true)) {
            getCommand("weather").setExecutor(new WeatherCmd(this));
            getCommand("weather").setTabCompleter(new WeatherCmd(this));
        } else {
            unregisterCommand("weather");
        }

        if (getConfig().getBoolean("commands-enabled.lifemod", true)) {
            getCommand("lifemod").setExecutor(new CommandHandler(this));
            getCommand("lifemod").setTabCompleter(new CommandHandler(this));
        } else {
            unregisterCommand("lifemod");
        }

        if (getConfig().getBoolean("commands-enabled.speed", true)) {
            getCommand("speed").setExecutor(new SpeedCommand());
            getCommand("speed").setTabCompleter(new SpeedCommand());
        } else {
            unregisterCommand("speed");
        }
    }

    public boolean isFreezeActive() {
        return getConfig().getBoolean("commands-enabled.freeze");
    }

    public boolean isFlyActive() {
        return getConfig().getBoolean("commands-enabled.fly");
    }

    public boolean isBroadcastActive() {
        return getConfig().getBoolean("commands-enabled.broadcast");
    }

    public boolean isGamemodeActive() {
        return getConfig().getBoolean("commands-enabled.gamemode");
    }

    public boolean isModActive() {
        return getConfig().getBoolean("commands-enabled.mod");
    }

    public boolean isEcopenActive() {
        return getConfig().getBoolean("commands-enabled.ecopen");
    }

    public boolean isVanishActive() {
        return getConfig().getBoolean("commands-enabled.vanish");
    }

    public boolean isStafflistActive() {
        return getConfig().getBoolean("commands-enabled.stafflist");
    }

    public boolean isClearinvActive() {
        return getConfig().getBoolean("commands-enabled.clearinv");
    }

    public boolean isStaffchatActive() {
        return getConfig().getBoolean("commands-enabled.staffchat");
    }

    public boolean isChatclearActive() {
        return getConfig().getBoolean("commands-enabled.chatclear");
    }

    public boolean isHealActive() {
        return getConfig().getBoolean("commands-enabled.heal");
    }

    public boolean isTeleportActive() {
        return getConfig().getBoolean("commands-enabled.teleport");
    }

    public boolean isInvseeActive() {
        return getConfig().getBoolean("commands-enabled.invsee");
    }

    public boolean isGodmodeActive() {
        return getConfig().getBoolean("commands-enabled.godmode");
    }

    public boolean isSpeedActive() {
        return getConfig().getBoolean("commands-enabled.speed");
    }

    public boolean isLifemodActive() {
        return getConfig().getBoolean("commands-enabled.lifemod");
    }

    public boolean isWeatherActive() {
        return getConfig().getBoolean("commands-enabled.weather");
    }

    public boolean isFeedActive() {
        return getConfig().getBoolean("commands-enabled.feed");
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

    public void onDisable() {
        Bukkit.getOnlinePlayers().stream().filter(PlayerManager::isInModerationMod).forEach(p -> {
            if (PlayerManager.isInModerationMod(p)) {
                PlayerManager.getFromPlayer(p).destroy();
            }
        });
    }

    public void ConfigConfig() {
        ConfigFile = new File(getDataFolder(), "config.yml");
        if (!ConfigFile.exists()) {
            ConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        ConfigConfig = new YamlConfiguration();
        try {
            ConfigConfig.load(ConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void LangConfig() {
        LangFile = new File(getDataFolder(), "lang.yml");
        if (!LangFile.exists()) {
            LangFile.getParentFile().mkdirs();
            saveResource("lang.yml", false);
        }

        LangConfig = new YamlConfiguration();
        try {
            LangConfig.load(LangFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getLangConfig() {
        return this.LangConfig;
    }

    public FileConfiguration getConfigConfig() {
        return this.ConfigConfig;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public ArrayList<UUID> getModerators() {
        return this.moderators;
    }

    public HashMap<UUID, PlayerManager> getPlayers() {
        return this.players;
    }

    public HashMap<UUID, Location> getFrozenPlayers() {
        return this.frozenPlayers;
    }

    public boolean isFreeze(Player player) {
        return getFrozenPlayers().containsKey(player.getUniqueId());
    }

    public FreezeManager getFreezeManager() {
        return freezeManager;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        LangConfig();
    }
}