package fr.lampalon.lifemod.listeners.players;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class onInventoryClick implements Listener {
    private Player targetPlayer;
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title == null || !title.equals(ChatColor.translateAlternateColorCodes('&', config.getString("menu.title")))) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
            if (skullMeta == null || skullMeta.getOwningPlayer() == null) return;

            targetPlayer = Bukkit.getPlayer(Objects.requireNonNull(skullMeta.getOwningPlayer().getName()));
            if (targetPlayer == null || !targetPlayer.isOnline()) return;

            openSecondMenu(player, targetPlayer);
            event.setCancelled(true);
        }

        if (clickedItem.getType() == Material.ARROW){
            event.setCancelled(true);
        }
    }
    private void openSecondMenu(Player player, Player targetPlayer) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        Inventory secondMenu = Bukkit.createInventory(player, config.getInt("submenu.slots"), ChatColor.translateAlternateColorCodes('&', config.getString("submenu.title")));

        ItemStack discreetWarningButton = new ItemStack(Material.valueOf(config.getString("submenu.warn.material")));
        ItemMeta discreetWarningMeta = discreetWarningButton.getItemMeta();
        discreetWarningMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.warn.title")));
        discreetWarningMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.warn.description"))));
        discreetWarningButton.setItemMeta(discreetWarningMeta);

        ItemStack banButton = new ItemStack(Material.valueOf(config.getString("submenu.ban.material")));
        ItemMeta banMeta = banButton.getItemMeta();
        banMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.ban.title")));
        banMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.ban.description"))));
        banButton.setItemMeta(banMeta);

        ItemStack teleportButton = new ItemStack(Material.valueOf(config.getString("submenu.teleport.material")));
        ItemMeta teleportMeta = teleportButton.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.teleport.title")));
        teleportMeta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.teleport.description"))));
        teleportButton.setItemMeta(teleportMeta);

        secondMenu.setItem(config.getInt("submenu.warn.slots"), discreetWarningButton);
        secondMenu.setItem(config.getInt("submenu.ban.slots"), banButton);
        secondMenu.setItem(config.getInt("submenu.teleport.slots"), teleportButton);

        player.openInventory(secondMenu);
    }

    @EventHandler
    public void onSecondMenuClick(InventoryClickEvent event) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        String title = event.getView().getTitle();

        if (topInventory == null || !title.equals(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.title")))) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        ItemMeta itemMeta = clickedItem.getItemMeta();
        if (itemMeta == null || !itemMeta.hasDisplayName()) {
            return;
        }

        String displayName = MessageUtil.parseColors(itemMeta.getDisplayName());

        String warnTitle = ChatColor.translateAlternateColorCodes('&', config.getString("submenu.warn.title"));
        String banTitle = ChatColor.translateAlternateColorCodes('&', config.getString("submenu.ban.title"));
        String teleportTitle = ChatColor.translateAlternateColorCodes('&', config.getString("submenu.teleport.title"));

        if (displayName.equalsIgnoreCase(warnTitle)) {
            Player targetPlayer = this.targetPlayer;
            handleWarnItem(player, targetPlayer);
            event.setCancelled(true);
        } else if (displayName.equalsIgnoreCase(banTitle)) {
            Player targetPlayer = this.targetPlayer;
            handleBanItem(player, targetPlayer);
            event.setCancelled(true);
        } else if (displayName.equals(teleportTitle)) {
            Player targetPlayer = this.targetPlayer;
            handleTeleport(player, targetPlayer);
            event.setCancelled(true);
        }
    }
    private void handleTeleport(Player player, Player targetPlayer) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        if (targetPlayer != null && targetPlayer.isOnline()) {
            player.teleport(targetPlayer.getLocation());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.teleport.action.message").replace("%target%", targetPlayer.getName())));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("offlineplayer")));
        }
    }
    private void handleWarnItem(Player player, Player targetPlayer) {
        FileConfiguration config = LifeMod.getInstance().getConfig();

        targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.warn.action.message"))));
    }
    private static void handleBanItem(Player player, Player targetPlayer) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("offlineplayer")));
            return;
        }

        String banTime = config.getString("submenu.ban.action.time");

        if (banTime == null || banTime.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("lang.ban.errortime")));
            return;
        }

        Duration duration = parseDuration(banTime);
        if (duration.isZero() || duration.isNegative()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("lang.ban.time-invalid")));
            return;
        }

        if (!targetPlayer.getPlayer().isBanned()) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(targetPlayer.getName(),
                    ChatColor.translateAlternateColorCodes('&', config.getString("submenu.ban.action.message")),
                    Date.from(Instant.now().plus(duration)),
                    null);
            targetPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', config.getString("submenu.ban.action.message")));
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("lang.ban.chat").replace("%target%", targetPlayer.getName())));
        }
    }
    private static Duration parseDuration(String durationString) {
        if (durationString == null || durationString.isEmpty()) {
            return Duration.ZERO;
        }

        try {
            String[] parts = durationString.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            Duration duration = Duration.ZERO;
            for (int i = 0; i < parts.length; i += 2) {
                int amount = Integer.parseInt(parts[i]);
                String unit = parts[i + 1];

                switch (unit.toLowerCase()) {
                    case "second":
                    case "seconds":
                    case "sec":
                    case "secs":
                    case "s":
                        duration = duration.plusSeconds(amount);
                        break;
                    case "minute":
                    case "minutes":
                    case "min":
                    case "mins":
                    case "m":
                        duration = duration.plusMinutes(amount);
                        break;
                    case "hour":
                    case "hours":
                    case "hr":
                    case "hrs":
                    case "h":
                        duration = duration.plusHours(amount);
                        break;
                    case "day":
                    case "days":
                    case "d":
                        duration = duration.plusDays(amount);
                        break;
                    case "month":
                    case "months":
                    case "mon":
                    case "mons":
                        duration = duration.plusDays(amount * 30);
                        break;
                    case "year":
                    case "years":
                    case "y":
                        duration = duration.plusDays(amount * 365);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid time unit: " + unit);
                }
            }
            return duration;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return Duration.ZERO;
        }
    }
}