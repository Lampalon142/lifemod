package fr.lampalon.lifemod;

import fr.lampalon.lifemod.commands.moderations.*;
import fr.lampalon.lifemod.commands.users.FeedCmd;
import fr.lampalon.lifemod.commands.utils.TeleportCmd;
import fr.lampalon.lifemod.commands.utils.WeatherCmd;
import fr.lampalon.lifemod.listeners.*;
import fr.lampalon.lifemod.commands.users.GodModCmd;
import fr.lampalon.lifemod.commands.users.FreezeCmd;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.data.configuration.Options;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LifeMod extends JavaPlugin {
    private static LifeMod instance;
    // private Reports reports;
    // private BasicDataSource connectionPool;
    // private MySQL mysql;
    public Options options;

    public Messages messages;
    private ArrayList<UUID> moderators; private HashMap<UUID, PlayerManager> players; private HashMap<UUID, Location> frozenPlayers;
    public static LifeMod getInstance() {
        return instance;
    }

    // public Reports getReports() {
        // return this.reports;
    // }
    public ArrayList<UUID> getModerators() {
                return this.moderators;
    }

    public HashMap<UUID, PlayerManager> getPlayers() {
    return this.players;
    }
    // public HashMap<Player, Player> getWhoReport() {
        // return this.whoReport;
    //}
    public HashMap<UUID, Location> getFrozenPlayers() {
        return this.frozenPlayers;
    }
    public boolean isFreeze(Player player) {
        return getFrozenPlayers().containsKey(player.getUniqueId());
    }

    //public MySQL getMySQL() {
        //return this.mysql;
    //}
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
        //this.reports = new Reports();
        //this.whoReport = new HashMap<>();
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
        Bukkit.getConsoleSender().sendMessage("§eBStats collect some information's for Lampalon_ thanks for your trust.");
    }

    private void registerEvents() {
         this.messages = new Messages();
         PluginManager pm = Bukkit.getPluginManager();
         pm.registerEvents((Listener)new ModCancels(), (Plugin)this);
         pm.registerEvents((Listener)new ModItemsInteract(), (Plugin)this);
         pm.registerEvents((Listener)new Staffchatevent(this, this.messages), (Plugin)this);
         pm.registerEvents((Listener)new PluginDisable(), (Plugin)this);
         pm.registerEvents((Listener)new InventoryClick(), (Plugin)this);
    }

    private void registerCommands() {
        this.messages = new Messages();
        getCommand("mod").setExecutor((CommandExecutor)new Commands());
        getCommand("broadcast").setExecutor((CommandExecutor)new BroadcastCmd());
        getCommand("bc").setExecutor((CommandExecutor)new BroadcastCmd());
        getCommand("gm").setExecutor((CommandExecutor)new GmCmd());
        getCommand("fly").setExecutor((CommandExecutor)new FlyCmd());
        getCommand("ecopen").setExecutor((CommandExecutor)new EcopenCmd(this.messages));
        getCommand("vanish").setExecutor((CommandExecutor)new VanishCmd(this.messages));
        getCommand("clearinv").setExecutor((CommandExecutor)new ClearinvCmd(this.messages));
        getCommand("stafflist").setExecutor((CommandExecutor)new StafflistCmd(this.messages));
        getCommand("staffchat").setExecutor((CommandExecutor)new StaffchatCmd(this.messages));
        getCommand("chatclear").setExecutor((CommandExecutor)new ChatclearCmd(this.messages));
        getCommand("heal").setExecutor((CommandExecutor)new HealCmd(this.messages));
        getCommand("tp").setExecutor((CommandExecutor)new TeleportCmd(this.messages));
        getCommand("tphere").setExecutor((CommandExecutor)new TeleportCmd(this.messages));
        getCommand("weather").setExecutor((CommandExecutor)new WeatherCmd(this, this.messages));
        getCommand("god").setExecutor((CommandExecutor)new GodModCmd(this.messages));
        getCommand("freeze").setExecutor((CommandExecutor)new FreezeCmd(this, this.messages));
        getCommand("invsee").setExecutor((CommandExecutor)new InvseeCmd(this, this.messages));
        getCommand("feed").setExecutor((CommandExecutor)new FeedCmd(this.messages));
    }

    public void Update(){
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