package fr.lampalon.lifemod.listeners.players;

import fr.lampalon.lifemod.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class PlayerQuit implements Listener {
    public PlayerQuit(){
        Bukkit.getOnlinePlayers().stream().filter(PlayerManager::isInModerationMod).forEach(p -> {
            if (PlayerManager.isInModerationMod(p)) {
                PlayerManager.getFromPlayer(p).destroy();
            }
        });
    }
}
