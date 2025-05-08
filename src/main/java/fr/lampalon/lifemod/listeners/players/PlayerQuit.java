package fr.lampalon.lifemod.listeners.players;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.PlayerManager;
import fr.lampalon.lifemod.manager.VanishedManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuit implements Listener {
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    public PlayerQuit() {
        Bukkit.getOnlinePlayers().stream()
                .filter(PlayerManager::isInModerationMod)
                .forEach(p -> {
                    PlayerManager.getFromPlayer(p).destroy();
                    debug.log("mod", p.getName() + " moderation mode destroyed on plugin reload.");
                });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location location = player.getLocation();

        LifeMod.getInstance().getDatabaseManager().getSQLiteManager().savePlayerCoords(uuid, location);
        LifeMod.getInstance().getDatabaseManager().getSQLiteManager().savePlayerInventory(uuid, player.getInventory());
        VanishedManager.handlePlayerQuit(player);

        debug.log("playerquit", player.getName() + " data saved on quit.");
    }
}
