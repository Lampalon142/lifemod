package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import fr.lampalon.lifemod.bukkit.utils.ActionBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class FollowCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;
    private final Map<UUID, UUID> following = new HashMap<>();
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();
    private final Map<UUID, Deque<Long>> cpsMap = new HashMap<>();

    public FollowCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("follow")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.follow")) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("commands", "Permission denied for /follow by " + player.getName());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("follow.usage")));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
            return true;
        }

        if (targetPlayer == player) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("follow.yourself")));
            return true;
        }

        if (tasks.containsKey(player.getUniqueId())) {
            tasks.get(player.getUniqueId()).cancel();
        }

        following.put(player.getUniqueId(), targetPlayer.getUniqueId());
        player.sendMessage(MessageUtil.formatMessage(
                plugin.getLangConfig().getString("follow.success")
                        .replace("%target%", targetPlayer.getName())
        ));
        debug.log("follow", player.getName() + " suit " + targetPlayer.getName());

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !targetPlayer.isOnline()) {
                    stopFollowing(player);
                    this.cancel();
                    return;
                }
                if (!following.containsKey(player.getUniqueId())) {
                    this.cancel();
                    return;
                }
                double distance = player.getLocation().distance(targetPlayer.getLocation());
                int cps = getCPS(targetPlayer);

                String msg = MessageUtil.formatMessage(
                        plugin.getLangConfig().getString("follow.actionbar")
                                .replace("%target%", targetPlayer.getName())
                                .replace("%distance%", String.format("%.1f", distance))
                                .replace("%cps%", String.valueOf(cps))
                );

                ActionBarUtil.sendActionBar(player, msg);
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
        tasks.put(player.getUniqueId(), task);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("follow")) return null;
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return null;
    }

    private int getCPS(Player player) {
        Deque<Long> deque = cpsMap.get(player.getUniqueId());
        if (deque == null) return 0;
        long now = System.currentTimeMillis();
        while (!deque.isEmpty() && now - deque.peekFirst() > 1000) {
            deque.pollFirst();
        }
        return deque.size();
    }

    public void stopFollowing(Player follower) {
        following.remove(follower.getUniqueId());
        if (tasks.containsKey(follower.getUniqueId())) {
            tasks.get(follower.getUniqueId()).cancel();
            tasks.remove(follower.getUniqueId());
        }

        cpsMap.remove(follower.getUniqueId());
    }
}
