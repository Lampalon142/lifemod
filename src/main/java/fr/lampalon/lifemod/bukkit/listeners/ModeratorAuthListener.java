package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ModeratorAuthListener implements Listener {

    private boolean needsAuth(Player player) {
        return player.hasPermission("lifemod.moderator")
                && !LifeMod.getInstance().getModeratorSessionManager().isAuthenticated(player.getUniqueId());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage().toLowerCase();
        if (needsAuth(player)
                && !(msg.startsWith("/modlogin") || msg.startsWith("/modregister"))) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.login-required")));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (needsAuth(player)) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.blocked-action")));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (needsAuth(player)) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.blocked-action")));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (needsAuth(player)) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.blocked-action")));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (needsAuth(player)) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.blocked-chat")));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (needsAuth(player)) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.blocked-action")));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (needsAuth(player)) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.blocked-action")));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (needsAuth(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (needsAuth(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (needsAuth(player)) {
            event.setKeepInventory(true);
        }
    }
}
