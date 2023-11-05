package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.data.configuration.Options;
import fr.lampalon.lifemod.manager.PlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

public class ModItemsInteract implements Listener {
  @EventHandler
  public void onInteract(PlayerInteractEntityEvent e) {
    Messages messages = (LifeMod.getInstance()).messages;
    String s = LifeMod.getInstance().getConfig().getString("killtargetmsg");
    String s1 = LifeMod.getInstance().getConfig().getString("killmodmsg");
    Inventory inv;
    int i;
    Player player = e.getPlayer();
    if (!PlayerManager.isInModerationMod(player))
      return;  if (!(e.getRightClicked() instanceof Player))
      return;  Player target = (Player)e.getRightClicked();
    
    e.setCancelled(true);
    
    switch (player.getInventory().getItemInMainHand().getType()) {

      case PAPER:
        inv = Bukkit.createInventory(null, 45, target.getName() + " > Inventory");
        
        for (i = 0; i < 36; i++) {
          if (target.getInventory().getItem(i) != null) {
            inv.setItem(i, target.getInventory().getItem(i));
          }
        } 
        
        inv.setItem(36, target.getInventory().getHelmet());
        inv.setItem(37, target.getInventory().getChestplate());
        inv.setItem(38, target.getInventory().getLeggings());
        inv.setItem(39, target.getInventory().getBoots());
        
        player.openInventory(inv);
        break;

      case PACKED_ICE:
        String s4 = LifeMod.getInstance().getConfig().getString("freeze-msg-six");
        if (e.getHand() == EquipmentSlot.HAND) {
          if (LifeMod.getInstance().getFrozenPlayers().containsKey(target.getUniqueId())) {
            String s2 = LifeMod.getInstance().getConfig().getString("unfreeze");
            String s3 = LifeMod.getInstance().getConfig().getString("unfreezeby");
            LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
            target.sendMessage(messages.prefixGeneral + s3.replace("%player%", player.getPlayer().getName()));
            player.sendMessage(messages.prefixGeneral + s2.replace("%target%", target.getPlayer().getName()));
            break;
          }
          LifeMod.getInstance().getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
          target.sendMessage(messages.freezeone);
          target.sendMessage(messages.freezetwo);
          target.sendMessage(messages.freezethree);
          target.sendMessage(messages.freezefour);
          target.sendMessage(messages.freezefive);
          player.sendMessage(messages.prefixGeneral + s4.replace("%target%", target.getPlayer().getName()));
        } 
        break;

      case BLAZE_ROD:
        if (e.getHand() == EquipmentSlot.HAND){
          target.damage(target.getHealth());
          target.sendMessage(messages.prefixGeneral + s.replace("%moderator%", player.getPlayer().getName()));
          player.sendMessage(messages.prefixGeneral + s1.replace("%target%", target.getPlayer().getName()));
        }
        break;
    } 
  }
  
  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    Messages messages = (LifeMod.getInstance()).messages;
    Options options = (LifeMod.getInstance()).options;
    String messages1 = (LifeMod.getInstance().getConfig().getString("tp"));
    List<Player> list;
    Player target;
    PlayerManager mod;
    Player player = e.getPlayer();
    if (!PlayerManager.isInModerationMod(player))
      return;  if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR)
      return; 
    switch (player.getInventory().getItemInMainHand().getType()) {
      case ENDER_PEARL:
        list = new ArrayList<>(Bukkit.getOnlinePlayers());
        list.remove(player);
        
        if (list.size() == 0) {
          player.sendMessage(messages.prefixGeneral + messages.nothingtp);
          
          return;
        }
        target = list.get((new Random()).nextInt(list.size()));
        player.teleport(target.getLocation());
        player.sendMessage(messages.prefixGeneral + messages1.replace("%player%", target.getPlayer().getName()));
        break;


      
      case BLAZE_POWDER:
        mod = PlayerManager.getFromPlayer(player);
        mod.setVanished(!mod.isVanished());
        player.sendMessage(mod.isVanished() ? (messages.prefixGeneral + messages.vanishon) : (messages.prefixGeneral + messages.vanishoff));
        break;
    } 
  }
}