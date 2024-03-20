package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BroadcastCmd implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = LifeMod.getInstance().messages;
        if (cmd.getName().equalsIgnoreCase("broadcast")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
                return true;
            }
            Player player = (Player) sender;

            if (!player.hasPermission("lifemod.bc")) {
                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.bcusage));
                return true;
            }
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage(MessageUtil.parseColors(messages.bc) + message.toString());
            }

            return true;
        }
        return false;
    }
}