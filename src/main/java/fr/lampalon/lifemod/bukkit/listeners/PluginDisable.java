package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginDisable implements Listener {
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginDisable(PluginDisableEvent event) {
        Bukkit.getOnlinePlayers().stream()
                .filter(PlayerManager::isInModerationMod)
                .forEach(p -> {
                    PlayerManager.getFromPlayer(p).destroy();
                    debug.log("mod", p.getName() + " moderation mode destroyed on plugin disable.");
                });
    }
}
