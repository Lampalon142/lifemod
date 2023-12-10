package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VanishCmd implements CommandExecutor {
    Messages messages;

    public VanishCmd(Messages messages){
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vanish")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noconsole));
                return true;
            }

            Player player = (Player) sender;

            if (player.hasPermission("lifemod.vanish")){
                if (args.length == 0){
                    if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishon));
                    } else {
                        player.removePotionEffect(PotionEffectType.INVISIBILITY);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishoff));
                    }
                } else if (args.length == 1) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (targetPlayer != null) {
                        if (!targetPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishon));
                        } else {
                            targetPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
                            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishoff));
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral  + messages.offlineplayer));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishusage));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.noperm));
            }

            return true;
        }
        return false;
    }
}
