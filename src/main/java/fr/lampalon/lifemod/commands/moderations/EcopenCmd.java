package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.usageopenec));
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[0]);

                if (!player.hasPermission("lifemod.ecopen")) {
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
                } else {
                    player.openInventory(targetPlayer.getEnderChest());
                }

                if (targetPlayer == null) {
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.offlineplayer));
                    return true;
                }

                if (targetPlayer == player){
                    player.sendMessage(MessageUtil.parseColors(messages.yourselfenderchest));
                    return true;
                }

            } else {
                sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
            }
            return true;
        }
        return false;
    }
}
