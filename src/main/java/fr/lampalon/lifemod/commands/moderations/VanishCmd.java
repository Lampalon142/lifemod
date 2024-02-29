package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.VanishedManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCmd implements CommandExecutor {
    private final Messages messages;
    private final VanishedManager playerManager;

    public VanishCmd(Messages messages, VanishedManager playerManager){
        this.messages = messages;
        this.playerManager = playerManager;
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
                    boolean isVanished = playerManager.isVanished();
                    playerManager.setVanished(!isVanished, player);
                    if (!isVanished) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishon));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishoff));
                    }
                } else if (args.length == 1) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (targetPlayer != null) {
                        boolean isVanished = playerManager.isVanished();
                        playerManager.setVanished(!isVanished, targetPlayer);
                        if (!isVanished) {
                            targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishon));
                        } else {
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
