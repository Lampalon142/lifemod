package fr.lampalon.lifemod.commands.utils;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Teleport implements CommandExecutor {
    private Messages s;
    
    public Teleport(Messages s){
        this.s = s;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tp")){
            if (!(sender instanceof Player)) {
                sender.sendMessage(s.noconsole);
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("lifemod.tp")){
                player.sendMessage(s.noperm);
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(s.tpusage);
                return true;
            }

            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                player.sendMessage(s.offlineplayer);
                return true;
            }
            String target1 = LifeMod.getInstance().getConfig().getString("tpsuccess");
            player.teleport(target);
            player.sendMessage(target1.replace("%player%", target.getPlayer().getName()));

            return true;
        } else if(label.equalsIgnoreCase("tphere")){
            if (!(sender instanceof Player)){
                sender.sendMessage(s.noconsole);
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("lifemod.tphere")){
                player.sendMessage(s.noperm);
                return true;
            }

            if (args.length != 1){
                player.sendMessage(s.susage);
                return true;
            }

            Player senderPlayer = (Player) sender;
            Player targetPlayer = Bukkit.getPlayer(args[0]);

            if (targetPlayer == null){
                player.sendMessage(s.offlineplayer);
                return true;
            }

            String target2 = LifeMod.getInstance().getConfig().getString("tpheresuccess");
            targetPlayer.teleport(senderPlayer.getLocation());
            player.sendMessage(target2.replace("%player%", targetPlayer.getPlayer().getName()));
            return true;
        }
        return true;
    }
}
