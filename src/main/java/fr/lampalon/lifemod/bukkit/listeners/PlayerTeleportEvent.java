package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.VanishedManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTeleportEvent implements Listener {
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    @EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (VanishedManager.isVanished(player)) {
            VanishedManager.setVanished(true, player);
            debug.log("vanish", player.getName() + " teleport while vanished, vanish reapplied.");
        }
    }
}
