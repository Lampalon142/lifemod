package fr.lampalon.lifemod.listeners;

import fr.lampalon.lifemod.manager.HistoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HistoryListener implements Listener {
    private final HistoryManager historyManager;

    public HistoryListener(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        historyManager.logConnection(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        historyManager.logDisconnection(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        historyManager.logAction(event.getEntity(), "Died");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String blockName = event.getClickedBlock().getType().toString();
        String action = "Interacted with " + blockName;
        historyManager.logAction(player, action);
    }
}
