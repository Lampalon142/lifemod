package fr.lampalon.lifemod.listeners.players;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.PlayerManager;
import fr.lampalon.lifemod.manager.VanishedManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class PlayerQuit implements Listener {
    public PlayerQuit(){
        Bukkit.getOnlinePlayers().stream().filter(PlayerManager::isInModerationMod).forEach(p -> {
            if (PlayerManager.isInModerationMod(p)) {
                PlayerManager.getFromPlayer(p).destroy();
            }
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
    }
}
