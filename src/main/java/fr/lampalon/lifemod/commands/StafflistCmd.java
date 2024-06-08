package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.VanishedManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StafflistCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isStafflistActive()) {
            if (label.equalsIgnoreCase("stafflist")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    StringBuilder modList = new StringBuilder(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("onlinemod")));

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("lifemod.stafflist") && !VanishedManager.isVanished(onlinePlayer)) {
                            modList.append(onlinePlayer.getName()).append(", ");
                        }
                    }

                    if (modList.length() > LifeMod.getInstance().getConfigConfig().getString("onlinemod").length()) {
                        modList.delete(modList.length() - 2, modList.length());
                    } else {
                        modList.append(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("nomodonline")));
                    }

                    player.sendMessage(modList.toString());
                    return true;
                } else {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                    return true;
                }
            }
        } else {
            sender.sendMessage(LifeMod.getInstance().getDisabledCommand());
        }
        return false;
    }
}
