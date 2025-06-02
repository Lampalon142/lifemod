package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class ModCancels implements Listener {

  private final DebugManager debug = LifeMod.getInstance().getDebugManager();

  private boolean isRestricted(Player player) {
    return PlayerManager.isInModerationMod(player) || LifeMod.getInstance().isFreeze(player);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onItemDrop(PlayerDropItemEvent e) {
    if (isRestricted(e.getPlayer())) {
      e.setCancelled(true);
      debug.log("mod", e.getPlayer().getName() + " tried to drop item in mod/freeze mode");
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockPlace(BlockPlaceEvent e) {
    if (isRestricted(e.getPlayer())) {
      e.setCancelled(true);
      debug.log("mod", e.getPlayer().getName() + " tried to place block in mod/freeze mode");
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockBreak(BlockBreakEvent e) {
    if (isRestricted(e.getPlayer())) {
      e.setCancelled(true);
      debug.log("mod", e.getPlayer().getName() + " tried to break block in mod/freeze mode");
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onItemPickup(PlayerPickupItemEvent e) {
    if (isRestricted(e.getPlayer())) {
      e.setCancelled(true);
      debug.log("mod", e.getPlayer().getName() + " tried to pick up item in mod/freeze mode");
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDamage(EntityDamageEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) e.getEntity();
    if (isRestricted(player)) {
      e.setCancelled(true);
      debug.log("mod", player.getName() + " tried to take damage in mod/freeze mode");
    }

    if (e instanceof EntityDamageByEntityEvent) {
      EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e;
      if (ev.getDamager() instanceof Player && isRestricted((Player) ev.getDamager())) {
        e.setCancelled(true);
        debug.log("mod", ((Player) ev.getDamager()).getName() + " tried to deal damage in mod/freeze mode");
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerInteract(PlayerInteractEvent e) {
    if (isRestricted(e.getPlayer())) {
      e.setCancelled(true);
      debug.log("mod", e.getPlayer().getName() + " tried to interact in mod/freeze mode");
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;

    Player player = (Player) event.getWhoClicked();

    if (isRestricted(player)) {
      event.setCancelled(true);
      debug.log("mod", player.getName() + " tried to click inventory in mod/freeze mode");
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMove(PlayerMoveEvent e) {
    if (LifeMod.getInstance().isFreeze(e.getPlayer())) {
      e.setTo(e.getFrom());
      debug.log("freeze", e.getPlayer().getName() + " tried to move while frozen");
    }
  }
}
