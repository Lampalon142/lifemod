package fr.lampalon.lifemod.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VanishedManager {
    private static final Map<UUID, Boolean> vanishedPlayers = new HashMap<>();

    public static boolean isVanished(Player player) {
        return vanishedPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public static void setVanished(boolean vanished, Player player) {
        vanishedPlayers.put(player.getUniqueId(), vanished);
        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
        } else {
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(player));
        }
    }
}
