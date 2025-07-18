package fr.lampalon.lifemod.bukkit.managers.gui.menus;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerInfoMenu {
    private final LifeMod plugin;
    private final Player viewer;
    private final UUID targetUuid;

    public PlayerInfoMenu(LifeMod plugin, Player viewer, UUID targetUuid) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.targetUuid = targetUuid;
    }

    public void open() {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
        String playerName = target.getName() != null ? target.getName() : "Unknown";
        String title = MessageUtil.formatMessage(
                plugin.getLangConfig().getString("report.detail.playerinfo.title", "&bPlayer Information: %player%")
        ).replace("%player%", playerName);

        Inventory inv = Bukkit.createInventory(null, 27, title);

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.uuid.enabled")) {
            Map<String, String> map = new HashMap<>();
            map.put("uuid", targetUuid.toString());
            inv.setItem(0, buildItem("uuid", Material.PAPER, map));
        }

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.name.enabled")) {
            Map<String, String> map = new HashMap<>();
            map.put("name", playerName);
            inv.setItem(1, buildItem("name", Material.NAME_TAG, map));
        }

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.ip.enabled")) {
            String ip = "Unknown";
            if (target.isOnline() && target instanceof Player) {
                Player p = (Player) target;
                if (p.getAddress() != null && p.getAddress().getAddress() != null) {
                    ip = p.getAddress().getAddress().getHostAddress();
                }
            }
            Map<String, String> map = new HashMap<>();
            map.put("ip", ip);
            inv.setItem(2, buildItem("ip", Material.COMPASS, map));
        }

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.first_join.enabled")) {
            String firstJoin = target.getFirstPlayed() > 0 ? formatDate(target.getFirstPlayed()) : "Unknown";
            Map<String, String> map = new HashMap<>();
            map.put("first_join", firstJoin);
            inv.setItem(3, buildItem("first_join", getClockItem().getType(), map));
        }

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.last_join.enabled")) {
            String lastJoin = target.getLastPlayed() > 0 ? formatDate(target.getLastPlayed()) : "Unknown";
            Map<String, String> map = new HashMap<>();
            map.put("last_join", lastJoin);
            inv.setItem(4, buildItem("last_join", getClockItem().getType(), map));
        }

        boolean isOnline = target.isOnline();

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.online_status.enabled")) {
            String status = isOnline ? "§aOnline" : "§cOffline";
            Map<String, String> map = new HashMap<>();
            map.put("status", status);
            inv.setItem(5, buildItem("online_status", isOnline ? Material.EMERALD : Material.REDSTONE, map));
        }

        Player p = null;
        if (isOnline && target instanceof Player) {
            p = (Player) target;
        }

        if (p != null) {
            if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.location.enabled")) {
                Map<String, String> map = new HashMap<>();
                map.put("world", p.getWorld().getName());
                map.put("x", String.valueOf(p.getLocation().getBlockX()));
                map.put("y", String.valueOf(p.getLocation().getBlockY()));
                map.put("z", String.valueOf(p.getLocation().getBlockZ()));
                inv.setItem(6, buildItem("location", Material.MAP, map));
            }

            if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.gamemode.enabled")) {
                Map<String, String> map = new HashMap<>();
                map.put("gamemode", p.getGameMode().name());
                inv.setItem(7, buildItem("gamemode", Material.DIAMOND_SWORD, map));
            }

            if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.health.enabled")) {
                Map<String, String> map = new HashMap<>();
                map.put("health", String.valueOf((int) p.getHealth()));
                inv.setItem(8, buildItem("health", Material.GOLDEN_APPLE, map));
            }

            if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.food.enabled")) {
                Map<String, String> map = new HashMap<>();
                map.put("food", String.valueOf(p.getFoodLevel()));
                inv.setItem(9, buildItem("food", Material.BREAD, map));
            }

            if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.ping.enabled")) {
                int ping = 0;
                try {
                    ping = getPing(p);
                } catch (Exception ignored) {}
                Map<String, String> map = new HashMap<>();
                map.put("ping", String.valueOf(ping));
                inv.setItem(10, buildItem("ping", Material.REDSTONE, map));
            }
        }

        long playtimeMillis = target.getLastPlayed() - target.getFirstPlayed();
        String playtime = playtimeMillis > 0 ? (playtimeMillis / (1000 * 60 * 60)) + "h" : "Unknown";
        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.playtime.enabled")) {
            Map<String, String> map = new HashMap<>();
            map.put("playtime", playtime);
            inv.setItem(11, buildItem("playtime", Material.EXPERIENCE_BOTTLE, map));
        }

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.op.enabled")) {
            Map<String, String> map = new HashMap<>();
            map.put("op", target.isOp() ? "§aYes" : "§cNo");
            inv.setItem(12, buildItem("op", Material.NETHER_STAR, map));
        }

        if (plugin.getLangConfig().getBoolean("report.detail.playerinfo.items.permissions.enabled")) {
            if (p != null) {
                StringBuilder perms = new StringBuilder();
                int count = 0;
                for (PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
                    if (perm.getValue()) {
                        if (perms.length() > 0) perms.append(", ");
                        perms.append(perm.getPermission());
                        count++;
                        if (count >= 10) break;
                    }
                }
                Map<String, String> map = new HashMap<>();
                map.put("permissions", perms.toString());
                inv.setItem(13, buildItem("permissions", Material.BOOK, map));
            }
        }

        viewer.openInventory(inv);
    }

    private ItemStack buildItem(String key, Material material, Map<String, String> placeholders) {
        String basePath = "report.detail.playerinfo.items." + key;
        String name = MessageUtil.formatMessage(plugin.getLangConfig().getString(basePath + ".name", "&f" + key));
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getLangConfig().getStringList(basePath + ".lore")) {
            String replaced = line;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                replaced = replaced.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            lore.add(MessageUtil.formatMessage(replaced));
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String formatDate(long millis) {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(millis));
    }

    public int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return -1;
        }
    }

    public ItemStack getClockItem() {
        Material mat;
        try {
            mat = Material.valueOf("CLOCK");
        } catch (IllegalArgumentException e) {
            mat = Material.valueOf("WATCH");
        }
        return new ItemStack(mat);
    }
}
