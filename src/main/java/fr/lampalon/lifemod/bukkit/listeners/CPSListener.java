package fr.lampalon.lifemod.bukkit.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

public class CPSListener implements Listener {
    private final Map<UUID, Deque<Long>> cpsMap;

    public CPSListener(Map<UUID, Deque<Long>> cpsMap) {
        this.cpsMap = cpsMap;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        addClick(event.getPlayer());
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            addClick((Player) event.getDamager());
        }
    }

    private void addClick(Player player) {
        UUID uuid = player.getUniqueId();
        Deque<Long> deque = cpsMap.computeIfAbsent(uuid, k -> new ArrayDeque<>());
        deque.addLast(System.currentTimeMillis());
    }
}