package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Stafflist implements CommandExecutor {
    Messages messages;
    public Stafflist(Messages messages){
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("stafflist")){
            if (sender instanceof Player) {
                Player player = (Player) sender;

                StringBuilder modList = new StringBuilder(messages.onlinemod);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("lifemod.stafflist")) {
                        modList.append(onlinePlayer.getName()).append(", ");
                    }
                }

                if (modList.length() > messages.onlinemod.length()) {
                    modList.delete(modList.length() - 2, modList.length());
                } else {
                    modList.append(messages.nomodonline);
                }

                player.sendMessage(modList.toString());
                return true;
            } else {
                sender.sendMessage(messages.noconsole);
                return true;
            }
        }
        return false;
    }
}
