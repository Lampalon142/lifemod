package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.PlayerManager;
import fr.lampalon.lifemod.utils.MessageUtil;
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
import org.bukkit.inventory.Inventory;

public class ModCancels
  implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onItemDrop(PlayerDropItemEvent e) {
    if(PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())){
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockPlace(BlockPlaceEvent e) {
    if(PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())){
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockBreak(BlockBreakEvent e) {
    if(PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())){
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onItemPickup(PlayerPickupItemEvent e) {
    if(PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())){
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDamage(EntityDamageEvent e) {
    if (!(e.getEntity() instanceof Player))
      return;
    if(PlayerManager.isInModerationMod(((Player) e.getEntity()).getPlayer()) || LifeMod.getInstance().isFreeze(((Player) e.getEntity()).getPlayer())){
      e.setCancelled(true);
    }

    if (e instanceof EntityDamageByEntityEvent) {
      EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)e;
      if(ev.getEntity() instanceof Player && LifeMod.getInstance().isFreeze((Player)ev.getEntity())){
        e.setCancelled(true);
      }
    } 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerInteract(PlayerInteractEvent e) {
    if(PlayerManager.isInModerationMod(e.getPlayer()) || LifeMod.getInstance().isFreeze(e.getPlayer())){
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;

    Player player = (Player) event.getWhoClicked();

    if (PlayerManager.isInModerationMod(player) || LifeMod.getInstance().isFreeze(player)) {
      event.setCancelled(true);
      return;
    }
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMove(PlayerMoveEvent e) {
    if (LifeMod.getInstance().isFreeze(e.getPlayer()))
      e.setTo(e.getFrom()); 
  }
}