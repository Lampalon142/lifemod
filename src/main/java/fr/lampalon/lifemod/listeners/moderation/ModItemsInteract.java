package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.FreezeManager;
import fr.lampalon.lifemod.manager.PlayerManager;

import java.awt.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import fr.lampalon.lifemod.manager.VanishedManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;

public class ModItemsInteract implements Listener {
  public HashMap<UUID, ItemStack> playerHelmets = new HashMap<>();

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
    if (!PlayerManager.isInModerationMod(player)) {
      return;
    }

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
    String invyes = LifeMod.getInstance().getLangConfig().getString("invsee.name");
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

  private HashMap<Player, Long> cooldowns = new HashMap<>();
  private final int cooldownTime = 1000;
  private boolean isOnCooldown(Player player) {
    return cooldowns.containsKey(player) && System.currentTimeMillis() - cooldowns.get(player) < cooldownTime;
  }

  private void addToCooldown(Player player) {
    cooldowns.put(player, System.currentTimeMillis());
  }

  private void handleFreeze(Player player, Player target) {
    String s2 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.unfreeze.mod");
    String s3 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.unfreeze.target");
    FreezeManager freezeManager = LifeMod.getInstance().getFreezeManager();

    if (!isOnCooldown(player)) {
      if (freezeManager.isPlayerFrozen(target.getUniqueId()) && LifeMod.getInstance().getFrozenPlayers().containsKey(target.getUniqueId())) {
        freezeManager.unfreezePlayer(player, target);
        LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
        target.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + s3.replace("%player%", player.getName())));
        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + s2.replace("%target%", target.getName())));
      } else {
        String s4 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.freeze.mod");
        LifeMod.getInstance().getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
        InputStream input = LifeMod.getInstance().getClass().getClassLoader().getResourceAsStream("lang.yml");
        Yaml yaml = new Yaml();
        Map<String, List<String>> config = yaml.load(input);
        List<String> freezeMsg = config.get("freeze.messages.onfreeze");

        for (String msg : freezeMsg) {
          target.sendMessage(MessageUtil.parseColors(msg));
        }

        freezeManager.freezePlayer(player, target);
        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + s4.replace("%target%", target.getName())));
      }
      addToCooldown(player);
    }
  }

  private void handleKill(Player player, Player target) {
    String s = LifeMod.getInstance().getLangConfig().getString("kill.target");
    String s1 = LifeMod.getInstance().getLangConfig().getString("kill.mod");
    target.setHealth(0);
    target.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + s.replace("%moderator%", player.getPlayer().getName())));
    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + s1.replace("%target%", target.getPlayer().getName())));
  }

  private void teleportRandomPlayer(Player player) {
    Messages messages = (LifeMod.getInstance()).messages;
    Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

    if (onlinePlayers.length == 0) {
      player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("tp.none")));
      return;
    }

    Player randomPlayer = onlinePlayers[new Random().nextInt(onlinePlayers.length)];

    if (randomPlayer.isEmpty()){
      player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("tp.none")));
      return;
    }

    player.teleport(randomPlayer.getLocation());

    String messages1 = LifeMod.getInstance().getLangConfig().getString("tp.success");
    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + messages1.replace("%player%", randomPlayer.getPlayer().getName())));
  }

  private final HashMap<UUID, Long> vanishCooldowns = new HashMap<>();
  private BukkitRunnable actionBarTask;

  private void toggleVanish(Player player) {
    String s = LifeMod.getInstance().getLangConfig().getString("vanish.cooldown");
    Messages messages = LifeMod.getInstance().messages;
    long currentTime = System.currentTimeMillis();
    long lastToggleTime = vanishCooldowns.getOrDefault(player.getUniqueId(), 0L);
    long cooldown = 5000;

    if (currentTime - lastToggleTime < cooldown) {
      int remainingSeconds = (int) ((cooldown - (currentTime - lastToggleTime)) / 1000);
      player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s.replace("%cooldown%", String.valueOf(remainingSeconds))));
      return;
    }

    VanishedManager vanishedManager = new VanishedManager();
    boolean newVanishState = !VanishedManager.isVanished(player);
    vanishedManager.setVanished(newVanishState, player);

    vanishCooldowns.put(player.getUniqueId(), currentTime);

    player.sendMessage(newVanishState ? MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("vanish.activate")) : MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("vanish.deactivate")));

    if (newVanishState) {
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