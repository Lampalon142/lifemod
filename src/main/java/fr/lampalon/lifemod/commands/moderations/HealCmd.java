package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCmd implements CommandExecutor {
    Messages messages;

    public HealCmd(Messages messages){
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("heal")){
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
                return true;
            }

            Player player = (Player) sender;

            String s = LifeMod.getInstance().getConfig().getString("healmodmsg");

            if (!player.hasPermission("lifemod.heal")) {
                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
                return true;
            }

            if (args.length == 0) {
                double maxHealth = player.getMaxHealth();
                player.setHealth(maxHealth);
                player.setFoodLevel(player.getFoodLevel());
                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.healmsgplayer));
            } else if (args.length == 1) {
                Player target = player.getServer().getPlayer(args[0]);
                if (target != null) {
                    double maxHealth = player.getMaxHealth();
                    target.setHealth(maxHealth);
                    target.setFoodLevel(player.getFoodLevel());
                    player.sendMessage(MessageUtil.parseColors(s.replace("%player%", player.getPlayer().getName())));
                } else {
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.offlineplayer));
                }
            } else {
                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.healusage));
            }
        }
        return true;
    }
}
