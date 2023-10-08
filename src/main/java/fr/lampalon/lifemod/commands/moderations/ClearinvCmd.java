package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearinvCmd implements CommandExecutor {
    Messages messages;
    public ClearinvCmd(Messages messages){
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("clearinv")){
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String target = LifeMod.getInstance().getConfig().getString("clearinvmsg");
                if (player.hasPermission("lifemod.clearinv")){
                    if (args.length == 1) {
                        Player targetPlayer = Bukkit.getPlayer(args[0]);

                        if (targetPlayer != null) {
                            targetPlayer.getInventory().clear();
                            player.sendMessage(messages.prefixGeneral + target.replace("%player%", targetPlayer.getPlayer().getName()));
                        } else {
                            player.sendMessage(messages.prefixGeneral  + messages.offlineplayer);
                        }
                    } else {
                        player.sendMessage(messages.prefixGeneral + messages.clearinvusage);
                    }
                } else {
                    player.sendMessage(messages.prefixGeneral + messages.noperm);
                }
            } else {
                sender.sendMessage(messages.noconsole);
            }
        }
        return true;
    }
}
