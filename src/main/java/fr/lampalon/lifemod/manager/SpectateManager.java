package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class SpectateManager {
    private final HashMap<UUID, Location> originalLocations = new HashMap<>();
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    public void startSpectate(Player spectator, Player target) {
        originalLocations.put(spectator.getUniqueId(), spectator.getLocation());
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.teleport(target.getLocation());
        debug.log("spectate", spectator.getName() + " started spectating " + target.getName());
    }

    public void endSpectate(Player spectator, boolean returnToOriginal) {
        if (returnToOriginal) {
            Location originalLocation = originalLocations.get(spectator.getUniqueId());
            if (originalLocation != null) {
                spectator.teleport(originalLocation);
                debug.log("spectate", spectator.getName() + " returned to original location after spectating.");
            } else {
                debug.log("spectate", "No original location found for " + spectator.getName());
            }
        }
        originalLocations.remove(spectator.getUniqueId());
        spectator.setGameMode(GameMode.SURVIVAL);
        debug.log("spectate", spectator.getName() + " ended spectate mode.");
    }

    public boolean isSpectating(Player player) {
        return originalLocations.containsKey(player.getUniqueId());
    }
}