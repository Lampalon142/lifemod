package fr.lampalon.lifemod.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VanishedManager {
    private boolean vanished = false;

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished, Player player) {
        this.vanished = vanished;
        if (vanished) {
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
        } else {
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(player));
        }
    }
}
