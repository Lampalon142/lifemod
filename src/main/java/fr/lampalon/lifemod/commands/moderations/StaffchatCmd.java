package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffchatCmd implements CommandExecutor {
    Messages messages;

    public StaffchatCmd(Messages messages){
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noconsole));
            return true;
        }

        Player player = (Player) sender;
        String playermsg = LifeMod.getInstance().getConfig().getString("staffmsg");

        if (cmd.getName().equalsIgnoreCase("staffchat")) {
            if (player.hasPermission("lifemod.staffchat")){
                if (args.length == 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.staffusage));
                    return true;
                }
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(arg).append(" ");
                }
                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission("lifemod.staffchat")) {
                        staff.sendMessage( playermsg.replace("%player%", player.getPlayer().getName()) + ": " + message.toString());
                    }
                }

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.staffsuccesmsg));
                return true;
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.noperm));
                return true;
            }
        }
        return false;
    }
}
