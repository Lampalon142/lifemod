package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EcopenCmd implements CommandExecutor {
    Messages messages;

    public EcopenCmd(Messages messages) {
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("ecopen")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (args.length != 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.usageopenec));
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[0]);

                if (!player.hasPermission("lifemod.ecopen")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.noperm));
                } else {
                    player.openInventory(targetPlayer.getEnderChest());
                }

                if (targetPlayer == null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.offlineplayer));
                    return true;
                }

                if (targetPlayer == player){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.yourselfenderchest));
                    return true;
                }

            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noconsole));
            }
            return true;
        }
        return false;
    }
}
