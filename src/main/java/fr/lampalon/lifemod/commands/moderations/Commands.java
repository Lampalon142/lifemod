package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.PlayerManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noconsole));
            return false;
        }

        Player player = (Player)sender;

        if (label.equalsIgnoreCase("mod") || label.equalsIgnoreCase("staff")) {
            if (!player.hasPermission("lifemod.mod")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.noperm));
                return false;
            }

            if (PlayerManager.isInModerationMod(player)) {
                PlayerManager.getFromPlayer(player).destroy();
            } else {
                (new PlayerManager(player)).init();
            }
        }
        return false;
    }
}