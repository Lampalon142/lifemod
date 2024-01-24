package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.data.configuration.Options;
import fr.lampalon.lifemod.manager.PlayerManager;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.yaml.snakeyaml.Yaml;

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
        openPlayerInventoryGUI(player, target);

      case PACKED_ICE:
        ItemStack air = new ItemStack(Material.AIR);
        ItemStack packedice = new ItemStack(Material.PACKED_ICE);
        String s4 = LifeMod.getInstance().getConfig().getString("freeze-msg-six");
        if (e.getHand() == EquipmentSlot.HAND) {
          if (LifeMod.getInstance().getFrozenPlayers().containsKey(target.getUniqueId())) {
            String s2 = LifeMod.getInstance().getConfig().getString("unfreeze");
            String s3 = LifeMod.getInstance().getConfig().getString("unfreezeby");
            LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s3.replace("%player%", player.getPlayer().getName())));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s2.replace("%target%", target.getPlayer().getName())));
            target.getInventory().remove(air);
            break;
          }
          LifeMod.getInstance().getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
          InputStream input = LifeMod.getInstance().getClass().getClassLoader().getResourceAsStream("config.yml");
          Yaml yaml = new Yaml();
          Map<String, List<String>> config = yaml.load(input);

          List<String> freezeMsg = config.get("freeze-msg");

          for (String msg : freezeMsg) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
          }

          target.getInventory().setHelmet(packedice);

          player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s4.replace("%target%", target.getPlayer().getName())));
        }
        break;

      case BLAZE_ROD:
        if (e.getHand() == EquipmentSlot.HAND){
          target.damage(target.getHealth());
          target.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s.replace("%moderator%", player.getPlayer().getName())));
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s1.replace("%target%", target.getPlayer().getName())));
        }
        break;
    } 
  }
  ItemStack itemInHand;
  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    Messages messages = (LifeMod.getInstance()).messages;
    List<Player> list;
    Player target;
    PlayerManager mod;
    Player player = e.getPlayer();
    if (!PlayerManager.isInModerationMod(player)) return;
    if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;

    try {
      Method getItemInMainHandMethod = PlayerInventory.class.getMethod("getItemInMainHand");
      itemInHand = (ItemStack) getItemInMainHandMethod.invoke(player.getInventory());
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      try {
        if (Bukkit.getBukkitVersion().contains("1.8") || Bukkit.getBukkitVersion().contains("1.20.2")) {
          itemInHand = player.getItemInHand();
        } else {
          throw new NoSuchMethodException("Both getItemInMainHand and getItemInHand methods not found.");
        }
      } catch (NoSuchMethodException ex2) {
        ex2.printStackTrace();
      }
    }

    switch (itemInHand.getType()) {
      case ENDER_PEARL:
        list = new ArrayList<>(Bukkit.getOnlinePlayers());
        list.remove(player);

        if (list.size() == 0) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.nothingtp));
          return;
        }
        target = list.get((new Random()).nextInt(list.size()));
        player.teleport(target.getLocation());
        String messages1 = (LifeMod.getInstance().getConfig().getString("tp"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages1.replace("%player%", target.getPlayer().getName())));
        break;

      case BLAZE_POWDER:
        mod = PlayerManager.getFromPlayer(player);
        mod.setVanished(!mod.isVanished());
        player.sendMessage(mod.isVanished() ? (ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishon)) : (ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.vanishoff)));
        break;
    }
  }

  private void openPlayerInventoryGUI(Player moderator, Player target) {
    Inventory inventory = Bukkit.createInventory(null, 45, target.getName() + " > Inventory");

    for (int i = 0; i < 36; i++) {
      ItemStack item = target.getInventory().getItem(i);
      if (item != null) {
        inventory.setItem(i, item.clone());
      }
    }

    inventory.setItem(36, target.getInventory().getHelmet());
    inventory.setItem(37, target.getInventory().getChestplate());
    inventory.setItem(38, target.getInventory().getLeggings());
    inventory.setItem(39, target.getInventory().getBoots());

    moderator.openInventory(inventory);
  }
}