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

    /**
     * Check if a player is vanished.
     *
     * @param player The player to check.
     * @return True if vanished, false otherwise.
     */
    public static boolean isVanished(Player player) {
        return vanishedPlayers.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Set a player's vanish state.
     *
     * @param vanished True to vanish the player, false to unvanish.
     * @param player   The player to set vanish state for.
     */
    public static void setVanished(boolean vanished, Player player) {
        UUID playerId = player.getUniqueId();
        vanishedPlayers.put(playerId, vanished);

        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(LifeMod.getInstance(), player));

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !isVanished(player)) {
                        this.cancel();
                        vanishTasks.remove(playerId);
                        return;
                    }
                    Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(LifeMod.getInstance(), player));
                }
            };
            task.runTaskTimer(LifeMod.getInstance(), 0L, 20L); // Runs every second
            vanishTasks.put(playerId, task);

        } else {
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(LifeMod.getInstance(), player));

            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            if (vanishTasks.containsKey(playerId)) {
                vanishTasks.get(playerId).cancel();
                vanishTasks.remove(playerId);
            }
        }
    }

    /**
     * Handle vanish consistency for new players joining the server.
     *
     * @param joiningPlayer The player who joined.
     */
    public static void handlePlayerJoin(Player joiningPlayer) {
        vanishedPlayers.forEach((uuid, vanished) -> {
            if (vanished) {
                Player vanishedPlayer = Bukkit.getPlayer(uuid);
                if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                    joiningPlayer.hidePlayer(LifeMod.getInstance(), vanishedPlayer);
                }
            }
        });
    }

    /**
     * Handle cleanup for vanished players when they leave.
     *
     * @param player The player who quit.
     */
    public static void handlePlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        if (vanishTasks.containsKey(playerId)) {
            vanishTasks.get(playerId).cancel();
            vanishTasks.remove(playerId);
        }
        vanishedPlayers.remove(playerId);
    }

    /**
     * Get a map of vanished players.
     *
     * @return The map of vanished players.
     */
    public static Map<UUID, Boolean> getVanishedPlayers() {
        return vanishedPlayers;
    }
}
