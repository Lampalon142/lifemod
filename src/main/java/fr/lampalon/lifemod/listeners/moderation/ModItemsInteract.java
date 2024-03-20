package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.data.configuration.Options;
import fr.lampalon.lifemod.manager.PlayerManager;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import fr.lampalon.lifemod.utils.MessageUtil;
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
    Player player = e.getPlayer();
    if (!PlayerManager.isInModerationMod(player))
      return;

    if (!(e.getRightClicked() instanceof Player))
      return;
    
    e.setCancelled(true);

    switch (player.getInventory().getItemInMainHand().getType()) {
      case PAPER:
        openTargetInventory(player, (Player) e.getRightClicked());
        break;
      case PACKED_ICE:
        handleFreeze(player, (Player) e.getRightClicked());
        break;
      case BLAZE_ROD:
        handleKill(player, (Player) e.getRightClicked());
        break;
      default:
        break;
    } 
  }
  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    if (!PlayerManager.isInModerationMod(player))
      return;

    if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR)
      return;

    switch (player.getInventory().getItemInMainHand().getType()) {
      case ENDER_PEARL:
        teleportRandomPlayer(player);
        break;
      case BLAZE_POWDER:
        toggleVanish(player);
        break;
      default:
        break;
    }
  }

  private void openTargetInventory(Player player, Player target) {
    String invyes = LifeMod.getInstance().getConfig().getString("inventoryname");
    Inventory targetInventory = Bukkit.createInventory(null, 45, MessageUtil.parseColors(invyes.replace("%player%", target.getPlayer().getName())));
    PlayerInventory targetPlayerInventory = target.getInventory();

    for (int i = 0; i < 36; i++) {
      ItemStack item = targetPlayerInventory.getItem(i);
      if (item != null) {
        targetInventory.setItem(i, item.clone());
      }
    }

    targetInventory.setItem(36, targetPlayerInventory.getHelmet());
    targetInventory.setItem(37, targetPlayerInventory.getChestplate());
    targetInventory.setItem(38, targetPlayerInventory.getLeggings());
    targetInventory.setItem(39, targetPlayerInventory.getBoots());

    player.openInventory(targetInventory);
  }

  private void handleFreeze(Player player, Player target) {
    Messages messages = (LifeMod.getInstance()).messages;
    ItemStack air = new ItemStack(Material.AIR);
    String s2 = LifeMod.getInstance().getConfig().getString("unfreeze");
    String s3 = LifeMod.getInstance().getConfig().getString("unfreezeby");
    if (LifeMod.getInstance().getFrozenPlayers().containsKey(target.getUniqueId())) {
      target.getInventory().remove(air);
      LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
      target.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s2.replace("%target%", target.getPlayer().getName())));
      player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s3.replace("%player%", player.getPlayer().getName())));
    } else {
      String s4 = LifeMod.getInstance().getConfig().getString("freeze-msg-six");
      LifeMod.getInstance().getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
      InputStream input = LifeMod.getInstance().getClass().getClassLoader().getResourceAsStream("config.yml");
      Yaml yaml = new Yaml();
      Map<String, List<String>> config = yaml.load(input);

      List<String> freezeMsg = config.get("freeze-msg");

      for (String msg : freezeMsg) {
        target.sendMessage(MessageUtil.parseColors(msg));
      }

      ItemStack packedice = new ItemStack(Material.PACKED_ICE);

      player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s4.replace("%target%", target.getPlayer().getName())));

      target.getInventory().setHelmet(packedice);

      player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s2.replace("%target%", target.getPlayer().getName())));}
  }

  private void handleKill(Player player, Player target) {
    Messages messages = (LifeMod.getInstance()).messages;
    String s = LifeMod.getInstance().getConfig().getString("killtargetmsg");
    String s1 = LifeMod.getInstance().getConfig().getString("killmodmsg");
    target.setHealth(0);
    target.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s.replace("%moderator%", player.getPlayer().getName())));
    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s1.replace("%target%", target.getPlayer().getName())));
  }

  private void teleportRandomPlayer(Player player) {
    Messages messages = (LifeMod.getInstance()).messages;
    Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

    if (onlinePlayers.length == 0) {
      player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.nothingtp));
      return;
    }

    Player randomPlayer = onlinePlayers[new Random().nextInt(onlinePlayers.length)];

    if (randomPlayer.isEmpty()){
      player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.nothingtp));
      return;
    }

    player.teleport(randomPlayer.getLocation());

    String messages1 = (LifeMod.getInstance().getConfig().getString("tp"));
    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages1.replace("%player%", randomPlayer.getPlayer().getName())));
  }

  private final HashMap<UUID, Long> vanishCooldowns = new HashMap<>();

  private void toggleVanish(Player player) {
    String s = LifeMod.getInstance().getConfig().getString("vanishcooldown");
    Messages messages = (LifeMod.getInstance()).messages;
    long currentTime = System.currentTimeMillis();
    long lastToggleTime = vanishCooldowns.getOrDefault(player.getUniqueId(), 0L);
    long cooldown = 5000;

    if (currentTime - lastToggleTime < cooldown) {
      int remainingSeconds = (int) ((cooldown - (currentTime - lastToggleTime)) / 1000);
      player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s.replace("%cooldown%", String.valueOf(remainingSeconds))));
      return;
    }

    PlayerManager mod = PlayerManager.getFromPlayer(player);
    boolean isVanished = !mod.isVanished();

    mod.setVanished(isVanished);

    vanishCooldowns.put(player.getUniqueId(), currentTime);

    player.sendMessage(isVanished ? MessageUtil.parseColors(messages.prefixGeneral + messages.vanishon) : MessageUtil.parseColors(messages.prefixGeneral + messages.vanishoff));

    if (isVanished) {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        if (!onlinePlayer.equals(player)) {
          onlinePlayer.hidePlayer(player);
        }
      }
    } else {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        onlinePlayer.showPlayer(player);
      }
    }
  }
}