package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

public class ModCancels
  implements Listener {
  @EventHandler
  public void onItemDrop(PlayerDropItemEvent e) {
    e.setCancelled((PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())));
  }
  
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    e.setCancelled((PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())));
  }
  
  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    e.setCancelled((PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())));
  }
  
  @EventHandler
  public void onItemPickup(PlayerPickupItemEvent e) {
    e.setCancelled((PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())));
  }
  
  @EventHandler
  public void onEntityDamage(EntityDamageEvent e) {
    if (!(e.getEntity() instanceof Player))
      return; 
    e.setCancelled((PlayerManager.isInModerationMod((Player)e.getEntity()) || LifeMod.getInstance().isFreeze((Player)e.getEntity())));
    
    if (e instanceof EntityDamageByEntityEvent) {
      EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)e;
      e.setCancelled((ev.getEntity() instanceof Player && LifeMod.getInstance().isFreeze((Player)ev.getEntity())));
    } 
  }
  
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    e.setCancelled((PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())));
  }
  
  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    e.setCancelled(LifeMod.getInstance().isFreeze((Player)e.getWhoClicked()));
  }
  
  @EventHandler
  public void onMove(PlayerMoveEvent e) {
    if (LifeMod.getInstance().isFreeze(e.getPlayer()))
      e.setTo(e.getFrom()); 
  }
}