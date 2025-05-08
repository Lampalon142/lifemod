package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.FreezeManager;
import fr.lampalon.lifemod.manager.PlayerManager;
import fr.lampalon.lifemod.manager.VanishedManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

public class ModItemsInteract implements Listener {

  private final DebugManager debug = LifeMod.getInstance().getDebugManager();
  private final HashMap<UUID, ItemStack> playerHelmets = new HashMap<>();
  private final HashMap<Player, Long> cooldowns = new HashMap<>();
  private final int freezeCooldownTime = 1000;
  private final HashMap<UUID, Long> vanishCooldowns = new HashMap<>();
  private final int vanishCooldownTime = 5000;

  @EventHandler
  public void onInteract(PlayerInteractEntityEvent e) {
    Player player = e.getPlayer();
    if (!PlayerManager.isInModerationMod(player))
      return;
    if (!(e.getRightClicked() instanceof Player))
      return;

    Player target = (Player) e.getRightClicked();
    e.setCancelled(true);

    Material itemType = player.getInventory().getItemInMainHand().getType();
    switch (itemType) {
      case PAPER:
        openTargetInventory(player, target);
        debug.log("mod", player.getName() + " opened inventory of " + target.getName() + " with PAPER.");
        break;
      case PACKED_ICE:
        handleFreeze(player, target);
        break;
      case BLAZE_ROD:
        handleKill(player, target);
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

    Material itemType = player.getInventory().getItemInMainHand().getType();
    switch (itemType) {
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
    String invTitle = LifeMod.getInstance().getLangConfig().getString("invsee.name");
    Inventory targetInventory = Bukkit.createInventory(null, 45, MessageUtil.formatMessage(invTitle.replace("%player%", target.getName())));
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
    debug.log("mod", player.getName() + " opened inventory of " + target.getName());
  }

  private boolean isOnFreezeCooldown(Player player) {
    return cooldowns.containsKey(player) && System.currentTimeMillis() - cooldowns.get(player) < freezeCooldownTime;
  }

  private void addToFreezeCooldown(Player player) {
    cooldowns.put(player, System.currentTimeMillis());
  }

  private void handleFreeze(Player player, Player target) {
    FreezeManager freezeManager = LifeMod.getInstance().getFreezeManager();
    String s2 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.unfreeze.mod");
    String s3 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.unfreeze.target");

    if (!isOnFreezeCooldown(player)) {
      if (freezeManager.isPlayerFrozen(target.getUniqueId()) && LifeMod.getInstance().getFrozenPlayers().containsKey(target.getUniqueId())) {
        freezeManager.unfreezePlayer(player, target);
        LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
        target.sendMessage(MessageUtil.formatMessage(s3.replace("%player%", player.getName())));
        player.sendMessage(MessageUtil.formatMessage(s2.replace("%target%", target.getName())));
        debug.log("freeze", player.getName() + " unfroze " + target.getName() + " with PACKED_ICE.");
      } else {
        String s4 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.freeze.mod");
        LifeMod.getInstance().getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
        List<String> freezeMsg = LifeMod.getInstance().getLangConfig().getStringList("freeze.messages.onfreeze");
        for (String msg : freezeMsg) {
          target.sendMessage(MessageUtil.formatMessage(msg));
        }
        freezeManager.freezePlayer(player, target);
        player.sendMessage(MessageUtil.formatMessage(s4.replace("%target%", target.getName())));
        debug.log("freeze", player.getName() + " froze " + target.getName() + " with PACKED_ICE.");
      }
      addToFreezeCooldown(player);
    } else {
      debug.log("freeze", player.getName() + " tried to freeze/unfreeze " + target.getName() + " but was on cooldown.");
    }
  }

  private void handleKill(Player player, Player target) {
    String s = LifeMod.getInstance().getLangConfig().getString("kill.target");
    String s1 = LifeMod.getInstance().getLangConfig().getString("kill.mod");
    target.setHealth(0);
    target.sendMessage(MessageUtil.formatMessage(s.replace("%moderator%", player.getName())));
    player.sendMessage(MessageUtil.formatMessage(s1.replace("%target%", target.getName())));
    debug.log("mod", player.getName() + " killed " + target.getName() + " with BLAZE_ROD.");
  }

  private void teleportRandomPlayer(Player player) {
    Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
    if (onlinePlayers.length <= 1) {
      player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("tp.none")));
      debug.log("mod", player.getName() + " tried to teleport randomly but was alone.");
      return;
    }
    Player randomPlayer;
    do {
      randomPlayer = onlinePlayers[new Random().nextInt(onlinePlayers.length)];
    } while (randomPlayer.equals(player) && onlinePlayers.length > 1);

    player.teleport(randomPlayer.getLocation());
    String messages1 = LifeMod.getInstance().getLangConfig().getString("tp.success");
    player.sendMessage(MessageUtil.formatMessage(messages1.replace("%player%", randomPlayer.getName())));
    debug.log("mod", player.getName() + " teleported to " + randomPlayer.getName() + " with ENDER_PEARL.");
  }

  private boolean isOnVanishCooldown(Player player) {
    return vanishCooldowns.containsKey(player.getUniqueId()) &&
            System.currentTimeMillis() - vanishCooldowns.get(player.getUniqueId()) < vanishCooldownTime;
  }

  private void addToVanishCooldown(Player player) {
    vanishCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
  }

  private void toggleVanish(Player player) {
    String s = LifeMod.getInstance().getLangConfig().getString("vanish.cooldown");
    long currentTime = System.currentTimeMillis();
    long lastToggleTime = vanishCooldowns.getOrDefault(player.getUniqueId(), 0L);
    long cooldown = vanishCooldownTime;

    if (currentTime - lastToggleTime < cooldown) {
      int remainingSeconds = (int) ((cooldown - (currentTime - lastToggleTime)) / 1000);
      player.sendMessage(MessageUtil.formatMessage(s.replace("%cooldown%", String.valueOf(remainingSeconds))));
      debug.log("vanish", player.getName() + " tried to toggle vanish but is on cooldown (" + remainingSeconds + "s left)");
      return;
    }

    VanishedManager vanishedManager = new VanishedManager();
    boolean newVanishState = !VanishedManager.isVanished(player);
    vanishedManager.setVanished(newVanishState, player);

    addToVanishCooldown(player);

    player.sendMessage(newVanishState
            ? MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("vanish.activate"))
            : MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("vanish.deactivate")));

    if (newVanishState) {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        if (!onlinePlayer.equals(player)) {
          onlinePlayer.hidePlayer(player);
        }
      }
      debug.log("vanish", player.getName() + " is now vanished (BLAZE_POWDER).");
    } else {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        onlinePlayer.showPlayer(player);
      }
      debug.log("vanish", player.getName() + " is now visible (BLAZE_POWDER).");
    }
  }
}