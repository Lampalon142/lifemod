package fr.lampalon.lifemod;

import fr.lampalon.lifemod.commands.*;
import fr.lampalon.lifemod.listeners.moderation.FreezeGui;
import fr.lampalon.lifemod.listeners.moderation.ModCancels;
import fr.lampalon.lifemod.listeners.moderation.ModItemsInteract;
import fr.lampalon.lifemod.listeners.players.PlayerJoin;
import fr.lampalon.lifemod.listeners.players.PlayerQuit;
import fr.lampalon.lifemod.listeners.utils.PluginDisable;
import fr.lampalon.lifemod.listeners.utils.Staffchatevent;
import fr.lampalon.lifemod.manager.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

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

public class LifeMod extends JavaPlugin {
    private FreezeManager freezeManager;
    private static LifeMod instance;
    private UpdateChecker updateChecker;
    private ChatManager chatManager;
    private VanishedManager playerManager;
    private File ConfigFile;
    private FileConfiguration ConfigConfig;
    private ArrayList<UUID> moderators;
    private HashMap<UUID, PlayerManager> players;
    private HashMap<UUID, Location> frozenPlayers;
    public CommandMap commandMap;
    public String webHookUrl = getConfig().getString("discord.webhookurl");
    private boolean isFreezeActive;
    private boolean isModActive;
    private boolean isBroadcastActive;
    private boolean isGamemodeActive;
    private boolean isFlyActive;
    private boolean isEcopenActive;
    private boolean isVanishActive;
    private boolean isClearinvActive;
    private boolean isStafflistActive;
    private boolean isStaffchatActive;
    private boolean isChatclearActive;
    private boolean isHealActive;
    private boolean isTeleportActive;
    private boolean isGodmodeActive;
    private boolean isInvseeActive;
    private boolean isFeedActive;
    private boolean isWeatherActive;
    private boolean isLifemodActive;
    private boolean isSpeedActive;
    private File LangFile;
    private FileConfiguration LangConfig;


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
        Bukkit.getConsoleSender().sendMessage("§cLifeMod developed by Lampalon with §4<3 §cwas been successfully initialised");
        utils();


    }
    private void utils(){
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
        pm.registerEvents(new Staffchatevent(this), (Plugin)this);
        pm.registerEvents(new PluginDisable(), this);
        pm.registerEvents(new PlayerQuit(), this);
        pm.registerEvents(new FreezeGui(this), this);
        updateChecker = new UpdateChecker(this, 112381);
        pm.registerEvents(new PlayerJoin(this, updateChecker), this);
    }
    private void registerCommands() {
        playerManager = new VanishedManager();
    }
    private void CommandHandler(){
        isFreezeActive = getConfig().getBoolean("commands-enabled.freeze", true);
        isModActive = getConfig().getBoolean("commands-enabled.mod", true);
        isBroadcastActive = getConfig().getBoolean("commands-enabled.broadcast", true);
        isGamemodeActive = getConfig().getBoolean("commands-enabled.gamemode", true);
        isFlyActive = getConfig().getBoolean("commands-enabled.fly", true);
        isEcopenActive = getConfig().getBoolean("commands-enabled.ecopen", true);
        isVanishActive = getConfig().getBoolean("commands-enabled.vanish", true);
        isClearinvActive = getConfig().getBoolean("commands-enabled.clearinv", true);
        isStafflistActive = getConfig().getBoolean("commands-enabled.stafflist", true);
        isStaffchatActive = getConfig().getBoolean("commands-enabled.staffchat", true);
        isChatclearActive = getConfig().getBoolean("commands-enabled.chatclear", true);
        isHealActive = getConfig().getBoolean("commands-enabled.heal", true);
        isTeleportActive = getConfig().getBoolean("commands-enabled.teleport", true);
        isGodmodeActive = getConfig().getBoolean("commands-enabled.godmode", true);
        isInvseeActive = getConfig().getBoolean("commands-enabled.invsee", true);
        isFeedActive = getConfig().getBoolean("commands-enabled.feed", true);
        isWeatherActive = getConfig().getBoolean("commands-enabled.weather", true);
        isLifemodActive = getConfig().getBoolean("commands-enabled.lifemod", true);
        isSpeedActive = getConfig().getBoolean("commands-enabled.speed", true);

        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isFreezeActive) {
            getCommand("freeze").setExecutor(new FreezeCmd(this));
        } else {
            unregisterCommand("freeze");
        }

        if(isModActive){
            getCommand("mod").setExecutor((CommandExecutor) new ModCommand());
            getCommand("staff").setExecutor((CommandExecutor)new ModCommand());
        } else {
            unregisterCommand("mod");
            unregisterCommand("staff");
        }

        if (isBroadcastActive){
            getCommand("broadcast").setExecutor((CommandExecutor) new BroadcastCmd());
            getCommand("bc").setExecutor((CommandExecutor) new BroadcastCmd());
            getCommand("broadcast").setTabCompleter((TabCompleter) new BroadcastCmd());
            getCommand("bc").setTabCompleter((TabCompleter) new BroadcastCmd());
        } else {
            unregisterCommand("broadcast");
            unregisterCommand("bc");
        }

        if (isGamemodeActive){
            getCommand("gm").setExecutor((CommandExecutor) new GmCmd());
            getCommand("gm").setTabCompleter((TabCompleter) new GmCmd());
            getCommand("gamemode").setTabCompleter((TabCompleter) new GmCmd());
        } else {
            unregisterCommand("gm");
        }

        if (isFlyActive){
            getCommand("fly").setExecutor((CommandExecutor) new FlyCmd());
            getCommand("fly").setTabCompleter((TabCompleter) new FlyCmd());
        } else {
            unregisterCommand("fly");
        }

        if (isEcopenActive){
            getCommand("ecopen").setExecutor((CommandExecutor) new EcopenCmd());
            getCommand("ecopen").setTabCompleter((TabCompleter) new EcopenCmd());
        } else {
            unregisterCommand("ecopen");
        }

        if (isVanishActive){
            getCommand("vanish").setExecutor((CommandExecutor) new VanishCmd(playerManager));
        } else {
            unregisterCommand("vanish");
        }

        if (isClearinvActive){
            getCommand("clearinv").setExecutor((CommandExecutor) new ClearinvCmd());
            getCommand("clearinv").setTabCompleter((TabCompleter) new ClearinvCmd());
        } else {
            unregisterCommand("clearinv");
        }

        if(isStafflistActive){
            getCommand("stafflist").setExecutor((CommandExecutor) new StafflistCmd());
        } else {
            unregisterCommand("stafflist");
        }

        if(isStaffchatActive){
            getCommand("staffchat").setExecutor((CommandExecutor) new StaffchatCmd());
            getCommand("staffchat").setTabCompleter((TabCompleter) new StaffchatCmd() );
        } else {
            unregisterCommand("staffchat");
        }

        if(isChatclearActive){
            getCommand("chatclear").setExecutor((CommandExecutor) new ChatclearCmd());
        } else {
            unregisterCommand("chatclear");
        }

        if(isHealActive){
            getCommand("heal").setExecutor((CommandExecutor) new HealCmd());
            getCommand("heal").setTabCompleter((TabCompleter) new HealCmd());
        } else {
            unregisterCommand("heal");
        }

        if(isTeleportActive){
            getCommand("tp").setExecutor((CommandExecutor) new TeleportCmd());
            getCommand("tphere").setExecutor((CommandExecutor) new TeleportCmd());
            getCommand("tp").setTabCompleter((TabCompleter) new TeleportCmd());
            getCommand("tphere").setTabCompleter((TabCompleter) new TeleportCmd());
        } else {
            unregisterCommand("tp");
            unregisterCommand("tphere");
        }

        if(isGodmodeActive){
            getCommand("god").setExecutor((CommandExecutor) new GodModCmd());
            getCommand("god").setTabCompleter((TabCompleter) new GodModCmd());
        } else {
            unregisterCommand("god");
        }

        if(isInvseeActive){
            getCommand("invsee").setExecutor((CommandExecutor) new InvseeCmd(this));
            getCommand("invsee").setTabCompleter((TabCompleter) new InvseeCmd(this));
        } else {
            unregisterCommand("invsee");
        }

        if(isFeedActive){
            getCommand("feed").setExecutor((CommandExecutor) new FeedCmd());
            getCommand("feed").setTabCompleter((TabCompleter) new FeedCmd());
        } else {
            unregisterCommand("feed");
        }

        if(isWeatherActive){
            getCommand("weather").setExecutor((CommandExecutor) new WeatherCmd(this));
            getCommand("weather").setTabCompleter((TabCompleter) new WeatherCmd(this));
        } else {
            unregisterCommand("weather");
        }

        if(isLifemodActive){
            getCommand("lifemod").setExecutor((CommandExecutor)new CommandHandler(this));
            getCommand("lifemod").setTabCompleter((TabCompleter) new CommandHandler(this));
        } else {
            unregisterCommand("lifemod");
        }

        if(isSpeedActive){
            getCommand("speed").setExecutor((CommandExecutor)new SpeedCommand());
            getCommand("speed").setTabCompleter((TabCompleter) new SpeedCommand());
        } else {
            unregisterCommand("speed");
        }
    }
    public boolean isFreezeActive() {
        return isFreezeActive;
    }

    public boolean isFlyActive() {
        return isFlyActive;
    }

    public boolean isBroadcastActive() {
        return isBroadcastActive;
    }

    public boolean isGamemodeActive() {
        return isGamemodeActive;
    }

    public boolean isModActive() {
        return isModActive;
    }

    public boolean isEcopenActive() {
        return isEcopenActive;
    }

    public boolean isVanishActive() {
        return isVanishActive;
    }

    public boolean isStafflistActive() {
        return isStafflistActive;
    }

    public boolean isClearinvActive() {
        return isClearinvActive;
    }

    public boolean isStaffchatActive() {
        return isStaffchatActive;
    }

    public boolean isChatclearActive() {
        return isChatclearActive;
    }

    public boolean isHealActive() {
        return isHealActive;
    }

    public boolean isTeleportActive() {
        return isTeleportActive;
    }

    public boolean isInvseeActive() {
        return isInvseeActive;
    }

    public boolean isGodmodeActive() {
        return isGodmodeActive;
    }

    public boolean isSpeedActive() {
        return isSpeedActive;
    }

    public boolean isLifemodActive() {
        return isLifemodActive;
    }

    public boolean isWeatherActive() {
        return isWeatherActive;
    }

    public boolean isFeedActive() {
        return isFeedActive;
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
    public static LifeMod getInstance() {
        return instance;
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
    public FreezeManager getFreezeManager(){
        return freezeManager;
    }
    public void reloadPluginConfig() {
        reloadConfig();
        LangConfig();
    }
}