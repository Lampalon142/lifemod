package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCmd implements CommandExecutor {
    LifeMod plugin;
    public InvseeCmd(LifeMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
            if (label.equalsIgnoreCase("invsee")) {
                if (plugin.isInvseeActive()) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                        return true;
                    }

                    if (!sender.hasPermission("lifemod.invsee")) {
                        sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                        return true;
                    }

                    if (args.length != 1) {
                        sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("invsee-usage")));
                        return true;
                    }

                    Player targetPlayer = Bukkit.getPlayer(args[0]);
                    if (targetPlayer == null || !targetPlayer.isOnline()) {
                        sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                        return true;
                    }

                    Player player = (Player) sender;

                    player.openInventory(targetPlayer.getInventory());
                    return true;
                } else {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
                }
            }

        return false;
    }
}
