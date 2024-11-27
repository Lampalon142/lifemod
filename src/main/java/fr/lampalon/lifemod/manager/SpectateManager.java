package fr.lampalon.lifemod.manager;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class SpectateManager {

    private final HashMap<UUID, Location> originalLocations = new HashMap<>();

    public void startSpectate(Player spectator, Player target) {
        originalLocations.put(spectator.getUniqueId(), spectator.getLocation());
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.teleport(target.getLocation());
    }

    public void endSpectate(Player spectator, boolean returnToOriginal) {
        if (returnToOriginal) {
            Location originalLocation = originalLocations.get(spectator.getUniqueId());
            if (originalLocation != null) {
                spectator.teleport(originalLocation);
            }
        }
        originalLocations.remove(spectator.getUniqueId());
        spectator.setGameMode(GameMode.SURVIVAL);
    }

    public boolean isSpectating(Player player) {
        return originalLocations.containsKey(player.getUniqueId());
    }
}