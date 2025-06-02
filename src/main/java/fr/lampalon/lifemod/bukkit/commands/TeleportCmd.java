package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeleportCmd implements CommandExecutor, TabCompleter {
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            debug.log("tp", "Console tried to use /tp");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("lifemod.tp")) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
            debug.log("tp", "Permission denied for /tp by " + player.getName());
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                debug.log("tp", "Target offline: " + args[0]);
                return true;
            }
            if (target == player) {
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("tp.yourself")));
                debug.log("tp", player.getName() + " tried to tp to himself");
                return true;
            }
            player.teleport(target.getLocation());
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("tp.success").replace("%player%", target.getName())));
            debug.log("tp", player.getName() + " teleported to " + target.getName());
        }
        else if (args.length == 2) {
            Player target1 = Bukkit.getPlayer(args[0]);
            Player target2 = Bukkit.getPlayer(args[1]);
            if (target1 == null || target2 == null) {
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                debug.log("tp", "At least one target offline: " + args[0] + ", " + args[1]);
                return true;
            }
            target1.teleport(target2.getLocation());
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("tp.twoplayers")
                    .replace("%player1%", target1.getName())
                    .replace("%player2%", target2.getName())));
            debug.log("tp", player.getName() + " teleported " + target1.getName() + " to " + target2.getName());
        }
        else if (args.length == 3) {
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                Location targetLocation = new Location(player.getWorld(), x, y, z);
                player.teleport(targetLocation);
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("tp.success").replace("%player%", "coordinates")));
                debug.log("tp", player.getName() + " teleported to coordinates " + x + "," + y + "," + z);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("tp.invalidcoordinates")));
                debug.log("tp", player.getName() + " entered invalid coordinates");
            }
        } else {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("tp.usage")));
            debug.log("tp", "Invalid usage by " + player.getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("tp") && (args.length == 1 || args.length == 2)) {
            String input = args[args.length - 1].toLowerCase();
            completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return completions;
    }
}
