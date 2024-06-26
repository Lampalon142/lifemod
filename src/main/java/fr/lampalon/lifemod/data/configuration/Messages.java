package fr.lampalon.lifemod.data.configuration;

import fr.lampalon.lifemod.LifeMod;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Messages {
    private final FileConfiguration config = LifeMod.getInstance().getConfig();

    public String prefixGeneral = config.getString("prefix");
    public String notifychatmod = config.getString("chatManager.notification");
    public String flyenable = config.getString("fly-enable");
    public String flydisable = config.getString("fly-disable");
    public String modenable = config.getString("mod-enable");
    public String moddisable = config.getString("mod-disable");

    public String noperm = config.getString("nopermission");

    public String noconsole = config.getString("onlyplayer");
    public String commanddisable = config.getString("command-disable");
    public String gminvalid = config.getString("gm-invalid");
    public String bc = config.getString("bc");
    public String bcusage = config.getString("bc-usage");
    public String freezeusage = config.getString("freeze-usage");
    public String freezeno = config.getString("freeze-yourself");
    public String nothingtp = config.getString("nothingtp");
    public String vanishon = config.getString("vanishon");
    public String vanishoff = config.getString("vanishoff");
    // Moderation's-Tools //
    public String nameinvsee = config.getString("Name-invSee");
    public String namefreeze = config.getString("Name-freeze");
    public String nametprandom = config.getString("Name-tpRandom");
    public String namevanish = config.getString("Name-vanish");
    public String namekill = config.getString("Name-kill");
    public String namekbtester = config.getString("Name-Kbtester");
    public String descinvsee = config.getString("Description-invSee");
    public String descfreeze = config.getString("Description-freeze");
    public String desctprandom = config.getString("Description-TPRandom");
    public String descvanish = config.getString("Description-Vanish");
    public String desckill = config.getString("Description-Kill");
    public String desckbtester = config.getString("Description-Kbtester");
    // other //
    public String offlineplayer = config.getString("offlineplayer");
    public String usageopenec = config.getString("usageopenec");
    // vanish //
    public String vanishusage = config.getString("vanishusage");
    // clearinv //
    public String clearinvusage = config.getString("clearinvusage");
    // mod //
    public String onlinemod = config.getString("onlinemod");
    public String nomodonline = config.getString("nomodonline");
    // staffchat //
    public String prefixmsg = config.getString("prefixmsg");
    public String staffsuccesmsg = config.getString("staffsuccesmsg");
    public String staffusage = config.getString("staffusage");
    // clearchat //
    public String chatclear = config.getString("chatclear");
    // Heal //
    public String healmsgplayer = config.getString("healmsgplayer");
    public String healusage = config.getString("healusage");
    public String tpusage = config.getString("tpusage");
    public String susage = config.getString("tphereusage");
    // weather //
    public String weatherusage = config.getString("weatherusage");
    public String weathersun = config.getString("weathersun");
    public String weatherrain = config.getString("weatherrain");
    public String weatherstorm = config.getString("weatherstorm");
    // God //
    public String godactivate = config.getString("godactivate");
    public String goddesactivate = config.getString("godesactivate");
    // Invsee //
    public String invseeusage = config.getString("invsee-usage");
    // Feed //
    public String yourselffeed = config.getString("yourself-feed");
    public String playerfeed = config.getString("player-feed");
    public String feedusage = config.getString("feed-usage");
    // enderchest //
    public String yourselfenderchest = config.getString("yourselfenderchest");
    public String invalidcoordinates = config.getString("invalidcoordinates");
    public String yourselftp = config.getString("yourselftp");
    // tp //
    public String gmown = config.getString("gm-own");
    public String gmother = config.getString("gm-other");
    // speed //
    public String providespeed = config.getString("speed.provide");
    public String speedsuccess = config.getString("speed.success");
}