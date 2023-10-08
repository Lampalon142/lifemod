package fr.lampalon.lifemod.commands.users;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCmd implements CommandExecutor {
    private Messages messages;
    private LifeMod main;

    public FreezeCmd(LifeMod main, Messages messages){
        this.main = main;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Conditions //
        if (label.equalsIgnoreCase("freeze")){
            if (!(sender instanceof Player)){sender.sendMessage(messages.noconsole); return true; }
            Player player = (Player) sender;
            if (player.hasPermission("lifemod.freeze")){
                if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target.equals(player)) {
                        sender.sendMessage(messages.freezeno);
                        return true;
                    }
                    if (main.getFrozenPlayers().containsKey(target.getUniqueId())){
                        String s2 = LifeMod.getInstance().getConfig().getString("unfreeze");
                        String s3 = LifeMod.getInstance().getConfig().getString("unfreezeby");
                        LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
                        target.sendMessage(messages.prefixGeneral + s3.replace("%player%", player.getPlayer().getName()));
                        player.sendMessage(messages.prefixGeneral + s2.replace("%target%", target.getPlayer().getName()));
                    } else {
                        String s4 = main.getConfig().getString("freeze-msg-six");
                        main.getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
                        target.sendMessage(messages.freezeone);
                        target.sendMessage(messages.freezetwo);
                        target.sendMessage(messages.freezethree);
                        target.sendMessage(messages.freezefour);
                        target.sendMessage(messages.freezefive);
                        player.sendMessage(messages.prefixGeneral + s4.replace("%target%", target.getPlayer().getName()));
                    }
                } else {
                    player.sendMessage(messages.freezeusage);
                }
            } else {
                player.sendMessage(messages.noperm);
                return true;
            }
        }
        return false;
    }
}