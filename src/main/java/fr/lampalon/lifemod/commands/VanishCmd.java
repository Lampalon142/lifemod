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

public class VanishCmd implements CommandExecutor {
    private final VanishedManager playerManager;

    public VanishCmd(VanishedManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isVanishActive()) {
            if (command.getName().equalsIgnoreCase("vanish")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                    return true;
                }

                Player player = (Player) sender;

                if (player.hasPermission("lifemod.vanish")) {
                    if (args.length == 0) {
                        boolean isVanished = VanishedManager.isVanished(player);
                        VanishedManager.setVanished(!isVanished, player);
                        if (!isVanished) {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishon")));
                        } else {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishoff")));
                        }
                    } else if (args.length == 1) {
                        Player targetPlayer = Bukkit.getPlayer(args[0]);

                        if (targetPlayer != null) {
                            boolean isVanished = VanishedManager.isVanished(targetPlayer);
                            VanishedManager.setVanished(!isVanished, targetPlayer);
                            if (!isVanished) {
                                targetPlayer.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishon")));
                            } else {
                                targetPlayer.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishoff")));
                            }
                        } else {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                        }
                    } else {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishusage")));
                    }
                } else {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                }

                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.commanddisable));
        }
        return false;
    }
}
