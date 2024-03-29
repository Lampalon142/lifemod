package fr.lampalon.lifemod.commands.utils;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCmd implements CommandExecutor {
    private Messages s;
    
    public TeleportCmd(Messages s){
        this.s = s;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tp")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtil.parseColors(s.noconsole));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("lifemod.tp")) {
                player.sendMessage(MessageUtil.parseColors( s.noperm));
                return true;
            }

            if (args.length != 1 && args.length != 3 && args.length != 2) {
                player.sendMessage(MessageUtil.parseColors(s.tpusage));
                return true;
            }

            if (args.length == 1) {

                String targetName = args[0];
                Player target = Bukkit.getPlayer(targetName);

                if (target == null) {
                    player.sendMessage(MessageUtil.parseColors(s.offlineplayer));
                    return true;
                }

                if (target == player){
                    player.sendMessage(MessageUtil.parseColors(s.prefixGeneral + s.yourselftp));
                    return false;
                }

                String target1 = LifeMod.getInstance().getConfig().getString("tpsuccess");
                player.teleport(target);
                player.sendMessage(MessageUtil.parseColors(s.prefixGeneral + target1.replace("%player%", target.getPlayer().getName())));

            } else if (args.length == 2) {
                Player target1 = Bukkit.getPlayer(args[0]);
                Player target2 = Bukkit.getPlayer(args[1]);

                if (target1 == null || target2 == null) {
                    player.sendMessage(MessageUtil.parseColors(s.offlineplayer));
                    return true;
                }

                target1.teleport(target2.getLocation());
                String messages = LifeMod.getInstance().getConfig().getString("tptwoplayers");
                player.sendMessage(MessageUtil.parseColors(messages.replace("%player1%", target1.getName()).replace("%player2%", target2.getName())));
            } else if (args.length == 3) {

                try {
                    double x = Double.parseDouble(args[0]);
                    double y = Double.parseDouble(args[1]);
                    double z = Double.parseDouble(args[2]);

                    Location targetLocation = new Location(player.getWorld(), x, y, z);
                    String target1 = LifeMod.getInstance().getConfig().getString("tpsuccess");
                    player.teleport(targetLocation);
                    player.sendMessage(MessageUtil.parseColors(target1.replace("%player%", "coordinates")));
                } catch (NumberFormatException e) {
                    player.sendMessage(MessageUtil.parseColors(s.invalidcoordinates));
                }
            }

            return true;
        } else if(label.equalsIgnoreCase("tphere")){
            if (!(sender instanceof Player)){
                sender.sendMessage(MessageUtil.parseColors(s.noconsole));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("lifemod.tphere")){
                player.sendMessage(MessageUtil.parseColors(s.noperm));
                return true;
            }

            if (args.length != 1){
                player.sendMessage(MessageUtil.parseColors(s.susage));
                return true;
            }

            Player senderPlayer = (Player) sender;
            Player targetPlayer = Bukkit.getPlayer(args[0]);

            if (targetPlayer == null){
                player.sendMessage(MessageUtil.parseColors(s.offlineplayer));
                return true;
            }

            String target2 = LifeMod.getInstance().getConfig().getString("tpheresuccess");
            targetPlayer.teleport(senderPlayer.getLocation());
            player.sendMessage(MessageUtil.parseColors(target2.replace("%player%", targetPlayer.getPlayer().getName())));
            return true;
        }
        return true;
    }
}
