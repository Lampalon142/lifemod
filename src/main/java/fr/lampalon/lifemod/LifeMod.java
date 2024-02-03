package fr.lampalon.lifemod;

import fr.lampalon.lifemod.commands.moderations.*;
import fr.lampalon.lifemod.commands.users.FeedCmd;
import fr.lampalon.lifemod.commands.utils.TeleportCmd;
import fr.lampalon.lifemod.commands.utils.WeatherCmd;
import fr.lampalon.lifemod.commands.users.GodModCmd;
import fr.lampalon.lifemod.commands.users.FreezeCmd;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.data.configuration.Options;
import fr.lampalon.lifemod.listeners.moderation.ModCancels;
import fr.lampalon.lifemod.listeners.moderation.ModItemsInteract;
import fr.lampalon.lifemod.listeners.players.PlayerQuit;
import fr.lampalon.lifemod.listeners.utils.PluginDisable;
import fr.lampalon.lifemod.listeners.utils.Staffchatevent;
import fr.lampalon.lifemod.manager.PlayerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import fr.lampalon.lifemod.utils.Update;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LifeMod extends JavaPlugin {
    private static LifeMod instance;
    public Options options;
    public Messages messages;
    private ArrayList<UUID> moderators; private HashMap<UUID, PlayerManager> players; private HashMap<UUID, Location> frozenPlayers;
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

    Boolean invsee = getConfig().getBoolean("commands-enabled.invsee");
    Boolean gamemode = getConfig().getBoolean("commands-enabled.gamemode");
    Boolean vanish = getConfig().getBoolean("commands-enabled.vanish");
    Boolean feed = getConfig().getBoolean("commands-enabled.feed");
    Boolean mod = getConfig().getBoolean("commands-enabled.mod");
    Boolean freeze = getConfig().getBoolean("commands-enabled.freeze");
    Boolean fly = getConfig().getBoolean("commands-enabled.fly");
    Boolean broadcast = getConfig().getBoolean("commands-enabled.broadcast");
    Boolean chatclear = getConfig().getBoolean("commands-enabled.chatclear");
    Boolean clearinv = getConfig().getBoolean("commands-enabled.clearinv");
    Boolean ecopen = getConfig().getBoolean("commands-enabled.ecopen");
    Boolean staffmsg = getConfig().getBoolean("commands-enabled.staffchat");
    Boolean stafflist = getConfig().getBoolean("commands-enabled.stafflist");
    Boolean godmode = getConfig().getBoolean("commands-enabled.godmode");
    Boolean teleport = getConfig().getBoolean("commands-enabled.teleport");
    Boolean weather = getConfig().getBoolean("commands-enabled.weather");
    Boolean heal = getConfig().getBoolean("commands-enabled.heal");

    public void onEnable() {
        setup();
        utils();
    }
    private void setup() {
        instance = this;
        this.frozenPlayers = new HashMap<>();
        this.players = new HashMap<>();
        this.moderators = new ArrayList<>();
        registerEvents();
        registerCommands();
        saveDefaultConfig();
        Update();
        this.options = new Options();
        this.messages = new Messages();
        Bukkit.getConsoleSender().sendMessage("§8=================================");
        Bukkit.getConsoleSender().sendMessage("§8");
        Bukkit.getConsoleSender().sendMessage("§7Plugin initialization in progress... please wait!");
        Bukkit.getConsoleSender().sendMessage("§8");
        Bukkit.getConsoleSender().sendMessage("§7Events initialization in progress...." + " §a§lsuccess!");
        Bukkit.getConsoleSender().sendMessage("§8");
        Bukkit.getConsoleSender().sendMessage("§7Commands initialization in progress..." + " §a§lsuccess!");
        Bukkit.getConsoleSender().sendMessage("§8");
        Bukkit.getConsoleSender().sendMessage("§7CheckUpdates initialization in progress..." + " §a§lsuccess!");
        Bukkit.getConsoleSender().sendMessage("§8");
        Bukkit.getConsoleSender().sendMessage("§4§lLifemod developed by Lampalon_");
        Bukkit.getConsoleSender().sendMessage("§8");
        Bukkit.getConsoleSender().sendMessage("§a§lPlugin initialized.");
        Bukkit.getConsoleSender().sendMessage("§8=================================");
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
         this.messages = new Messages();
         PluginManager pm = Bukkit.getPluginManager();
         pm.registerEvents((Listener)new ModCancels(), (Plugin)this);
         pm.registerEvents((Listener)new ModItemsInteract(), (Plugin)this);
         pm.registerEvents((Listener)new Staffchatevent(this, this.messages), (Plugin)this);
         pm.registerEvents((Listener)new PluginDisable(), (Plugin)this);
         pm.registerEvents((Listener)new PlayerQuit(), (Plugin)this);
    }
    private void registerCommands() {
        this.messages = new Messages();
        if (mod) {
            getCommand("mod").setExecutor((CommandExecutor) new Commands());
            getCommand("staff").setExecutor((CommandExecutor)new Commands());
        }

        if (broadcast) {
            getCommand("broadcast").setExecutor((CommandExecutor) new BroadcastCmd());
            getCommand("bc").setExecutor((CommandExecutor) new BroadcastCmd());
        }

        if (gamemode) {
            getCommand("gm").setExecutor((CommandExecutor) new GmCmd(this.messages));
        }

        if (fly) {
            getCommand("fly").setExecutor((CommandExecutor) new FlyCmd());
        }

        if (ecopen) {
            getCommand("ecopen").setExecutor((CommandExecutor) new EcopenCmd(this.messages));
        }

        if (vanish) {
            getCommand("vanish").setExecutor((CommandExecutor) new VanishCmd(this.messages));
        }

        if (clearinv) {
            getCommand("clearinv").setExecutor((CommandExecutor) new ClearinvCmd(this.messages));
        }

        if (stafflist) {
            getCommand("stafflist").setExecutor((CommandExecutor) new StafflistCmd(this.messages));
        }

        if (staffmsg) {
            getCommand("staffchat").setExecutor((CommandExecutor) new StaffchatCmd(this.messages));
        }

        if (chatclear) {
            getCommand("chatclear").setExecutor((CommandExecutor) new ChatclearCmd(this.messages));
        }

        if (heal) {
            getCommand("heal").setExecutor((CommandExecutor) new HealCmd(this.messages));
        }

        if (teleport) {
            getCommand("tp").setExecutor((CommandExecutor) new TeleportCmd(this.messages));
            getCommand("tphere").setExecutor((CommandExecutor) new TeleportCmd(this.messages));
        }

        if (godmode) {
            getCommand("god").setExecutor((CommandExecutor) new GodModCmd(this.messages));
        }

        if (freeze) {
            getCommand("freeze").setExecutor((CommandExecutor) new FreezeCmd(this, this.messages));
        }

        if (invsee) {
            getCommand("invsee").setExecutor((CommandExecutor) new InvseeCmd(this, this.messages));
        }

        if (feed) {
            getCommand("feed").setExecutor((CommandExecutor) new FeedCmd(this.messages));
        }

        if (weather) {
            getCommand("weather").setExecutor((CommandExecutor) new WeatherCmd(this, this.messages));
        }
    }

    private void Update(){
        new Update(this, 112381).getLatestVersion(version -> {
            if(this.getDescription().getVersion().equalsIgnoreCase(version)){
                this.getLogger().info("Plugin use the latest update thanks.");
            } else {
                this.getLogger().warning("Plugin required an update ! (https://www.spigotmc.org/resources/lifemod-moderation-plugin.112381/)");
            }
        });
    }
    public void onDisable() {
        Bukkit.getOnlinePlayers().stream().filter(PlayerManager::isInModerationMod).forEach(p -> {
            if (PlayerManager.isInModerationMod(p)) {
                PlayerManager.getFromPlayer(p).destroy();
            }
        });
        this.options = null;
        this.messages = null;
    }
 }