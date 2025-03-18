package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.database.DatabaseManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public class OtpCommand implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final FileConfiguration langConfig;

    public OtpCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.langConfig = LifeMod.getInstance().getLangConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.parseColors(langConfig.getString("general.onlyplayer", "&cThis command must be executed by a player.")));
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.otp")) {
            player.sendMessage(MessageUtil.parseColors(langConfig.getString("general.nopermission", "&cYou don't have permission to use this command.")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageUtil.parseColors(langConfig.getString("otp.usage", "&cUsage: /otp <player>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(MessageUtil.parseColors(langConfig.getString("otp.player-not-found", "&cPlayer %target% doesn't exist or has never connected.").replace("%target%", args[0])));
            return true;
        }

        Location location = databaseManager.getSQLiteManager().getPlayerCoords(target.getUniqueId());
        if (location == null) {
            player.sendMessage(MessageUtil.parseColors(langConfig.getString("otp.no-position", "&cNo saved position for %target%.").replace("%target%", args[0])));
            return true;
        }

        player.teleport(location);
        player.sendMessage(MessageUtil.parseColors(langConfig.getString("otp.teleported", "&aYou have been teleported to %target%'s last position.").replace("%target%", args[0])));
        return true;
    }
}