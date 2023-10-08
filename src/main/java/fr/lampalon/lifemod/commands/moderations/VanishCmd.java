package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
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
                sender.sendMessage(messages.noconsole);
                return true;
            }

            Player player = (Player) sender;



            if (player.hasPermission("lifemod.vanish")) {
                if (args.length == 1) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (targetPlayer != null) {
                        if (!targetPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                            targetPlayer.sendMessage(messages.prefixGeneral + messages.vanishon);
                        } else {
                            targetPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
                            targetPlayer.sendMessage(messages.prefixGeneral + messages.vanishoff);
                        }
                    } else {
                        player.sendMessage(messages.prefixGeneral  + messages.offlineplayer);
                    }
                } else {
                    player.sendMessage(messages.prefixGeneral + messages.vanishusage);
                }

            } else {
                player.sendMessage(messages.prefixGeneral + messages.noperm);
            }

            return true;
        }
        return false;
    }
}
