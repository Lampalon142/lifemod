package fr.lampalon.lifemod.listeners.players;

import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.VanishedManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTeleportEvent implements Listener {
    private final DebugManager debug = fr.lampalon.lifemod.LifeMod.getInstance().getDebugManager();

    @EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (VanishedManager.isVanished(player)) {
            VanishedManager.setVanished(true, player);
            debug.log("vanish", player.getName() + " teleport while vanished, vanish reapplied.");
        }
    }
}
