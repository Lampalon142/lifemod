package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VanishedManager {
    private static final Map<UUID, Boolean> vanishedPlayers = new HashMap<>();
    private static final Map<UUID, BukkitRunnable> vanishTasks = new HashMap<>();
    private static final DebugManager debug = LifeMod.getInstance().getDebugManager();

    public static boolean isVanished(Player player) {
        return vanishedPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public static void setVanished(boolean vanished, Player player) {
        UUID playerId = player.getUniqueId();
        vanishedPlayers.put(playerId, vanished);

        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(LifeMod.getInstance(), player));
            player.setCollidable(false);
            player.setCanPickupItems(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !isVanished(player)) {
                        this.cancel();
                        vanishTasks.remove(playerId);
                        debug.log("vanish", "Stopped vanish task for " + player.getName());
                        return;
                    }
                    Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(LifeMod.getInstance(), player));
                }
            };
            task.runTaskTimer(LifeMod.getInstance(), 0L, 20L);
            vanishTasks.put(playerId, task);

            debug.log("vanish", player.getName() + " is now vanished.");
        } else {
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(LifeMod.getInstance(), player));
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.setCollidable(true);
            player.setCanPickupItems(true);

            if (vanishTasks.containsKey(playerId)) {
                vanishTasks.get(playerId).cancel();
                vanishTasks.remove(playerId);
                debug.log("vanish", "Cancelled vanish task for " + player.getName());
            }
            debug.log("vanish", player.getName() + " is no longer vanished.");
        }
    }

    public static void handlePlayerJoin(Player joiningPlayer) {
        vanishedPlayers.forEach((uuid, vanished) -> {
            if (vanished) {
                Player vanishedPlayer = Bukkit.getPlayer(uuid);
                if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                    joiningPlayer.hidePlayer(LifeMod.getInstance(), vanishedPlayer);
                }
            }
        });
        debug.log("vanish", joiningPlayer.getName() + " joined; hidden vanished players.");
    }

    public static void handlePlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        if (vanishTasks.containsKey(playerId)) {
            vanishTasks.get(playerId).cancel();
            vanishTasks.remove(playerId);
            debug.log("vanish", "Cancelled vanish task for quitting player " + player.getName());
        }
        vanishedPlayers.remove(playerId);
        debug.log("vanish", player.getName() + " quit; removed from vanished list.");
    }

    public static Map<UUID, Boolean> getVanishedPlayers() {
        return vanishedPlayers;
    }
}