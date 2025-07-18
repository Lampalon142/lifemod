package fr.lampalon.lifemod.bukkit.managers.gui.menus;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.models.ReportStatus;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ReportDetailMenu {

    private final LifeMod plugin;
    private final Player player;
    private final Report report;
    private final FileConfiguration lang;

    private static final int getSlot(FileConfiguration lang, String path) {
        return lang.getInt(path);
    }

    public ReportDetailMenu(LifeMod plugin, Player player, Report report) {
        this.plugin = plugin;
        this.player = player;
        this.report = report;
        this.lang = plugin.getLangConfig();
    }

    public void open() {
        int rows = 6;
        String title = format(lang.getString("report.detail.title", "&d&lReport Details &7- &e%uuid%"))
                .replace("%uuid%", report.getUuid().toString().substring(0, 8));
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        inv.setItem(getSlot(lang, "report.detail.reporter.slot"),
                buildPlayerHead(report.getReporterUuid(), "report.detail.reporter", "playerinfo_reporter"));
        inv.setItem(getSlot(lang, "report.detail.target.slot"),
                buildPlayerHead(report.getTargetUuid(), "report.detail.target", "playerinfo_target"));
        inv.setItem(getSlot(lang, "report.detail.info.slot"), buildReportInfoItem());
        inv.setItem(getSlot(lang, "report.detail.evidence.slot"), buildEvidenceItem());
        inv.setItem(getSlot(lang, "report.detail.teleport.slot"), buildActionItem("report.detail.teleport", Material.ENDER_PEARL, "teleport"));
        inv.setItem(getSlot(lang, "report.detail.assign.slot"), buildActionItem("report.detail.assign",
                report.getAssignedTo() != null ? Material.LIME_DYE : Material.GRAY_DYE, "assign"));
        inv.setItem(getSlot(lang, "report.detail.close.slot"), buildActionItem("report.detail.close", Material.BARRIER, "close"));
        inv.setItem(getSlot(lang, "report.detail.notes.slot"), buildActionItem("report.detail.notes", Material.WRITABLE_BOOK, "notes"));
        inv.setItem(getSlot(lang, "report.detail.history.slot"), buildActionItem("report.detail.history", Material.KNOWLEDGE_BOOK, "history"));
        inv.setItem(getSlot(lang, "report.detail.suppress.slot"), buildActionItem("report.detail.suppress", Material.CHEST, "suppress"));

        if (report.getAssignedTo() != null) {
            inv.setItem(getSlot(lang, "report.detail.assigned-mod.slot"),
                    buildPlayerHead(report.getAssignedTo(), "report.detail.assigned-mod", "playerinfo_staff"));
        }

        inv.setItem(getSlot(lang, "report.detail.back.slot"), buildActionItem("report.detail.back", Material.ARROW, "back"));
        inv.setItem(getSlot(lang, "report.detail.priority.slot"), buildPriorityItem());
        inv.setItem(getSlot(lang, "report.detail.status.slot"), buildStatusItem());
        fillBorders(inv);

        player.openInventory(inv);
    }

    private ItemStack buildPlayerHead(UUID uuid, String configPath, String actionType) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer != null && offlinePlayer.getName() != null ?
                offlinePlayer.getName() :
                lang.getString("report.detail.unknown.staff", "Unknown Staff");

        boolean isOnline = offlinePlayer.isOnline();
        String status = format(lang.getString(isOnline ? "report.status.online" : "report.status.offline", "&coffline"));

        String displayName = format(lang.getString(configPath + ".name"))
                .replace("%name%", name);

        List<String> lore = new ArrayList<>();
        for (String line : lang.getStringList(configPath + ".lore")) {
            lore.add(format(line)
                    .replace("%name%", name)
                    .replace("%status%", status)
                    .replace("%uuid%", uuid.toString())
                    .replace("%join_date%", getPlayerJoinDate(uuid)));
        }

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = skull.getItemMeta();

        if (meta != null && meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwner(name);
            meta = skullMeta;
        }

        NamespacedKey actionKey = new NamespacedKey(plugin, "action_type");
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, actionType);

        NamespacedKey uuidKey = new NamespacedKey(plugin, "player_uuid");
        meta.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, uuid.toString());

        meta.setDisplayName(displayName);
        meta.setLore(lore);
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack buildReportInfoItem() {
        String displayName = format(lang.getString("report.detail.info.name", "&bReport Details"));
        List<String> lore = new ArrayList<>();
        for (String line : lang.getStringList("report.detail.info.lore")) {
            lore.add(format(line)
                    .replace("%reason%", report.getReason())
                    .replace("%server%", report.getServerName())
                    .replace("%status%", report.getStatus().getDisplayName())
                    .replace("%date%", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(report.getCreatedAt())))
                    .replace("%uuid%", report.getUuid().toString())
                    .replace("%priority%", calculatePriority())
                    .replace("%category%", getReportCategory())
            );
        }
        return buildItem(Material.BOOK, displayName, lore, "report_uuid", report.getUuid().toString());
    }

    private ItemStack buildEvidenceItem() {
        String displayName = format(lang.getString("report.detail.evidence.name", "&6Evidence & Context"));
        List<String> lore = new ArrayList<>();
        for (String line : lang.getStringList("report.detail.evidence.lore")) {
            lore.add(format(line)
                    .replace("%location%", getLocationString())
                    .replace("%world%", getWorldName())
                    .replace("%biome%", getBiome())
                    .replace("%weather%", getWeather())
                    .replace("%time%", getTimeOfDay())
                    .replace("%nearby_players%", getNearbyPlayersCount())
            );
        }

        ItemStack item = buildItem(Material.GLASS, displayName, lore, "action_type", "evidence");

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "target_uuid"),
                org.bukkit.persistence.PersistentDataType.STRING,
                report.getTargetUuid().toString()
        );
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "report_uuid"),
                PersistentDataType.STRING,
                report.getUuid().toString()
        );
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack buildActionItem(String configPath, Material material, String actionType) {
        String name = format(lang.getString(configPath + ".name", "&aAction"));
        List<String> lore = new ArrayList<>();
        for (String line : lang.getStringList(configPath + ".lore")) {
            lore.add(format(line)
                    .replace("%assigned_to%", getAssignedStaffName())
                    .replace("%time_since%", getTimeSinceReport())
                    .replace("%staff_count%", getOnlineStaffCount())
            );
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        NamespacedKey actionKey = new NamespacedKey(plugin, "action_type");
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, actionType);
        NamespacedKey reportKey = new NamespacedKey(plugin, "report_uuid");
        meta.getPersistentDataContainer().set(reportKey, PersistentDataType.STRING, report.getUuid().toString());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildPriorityItem() {
        String priority = calculatePriority();
        Material material = getPriorityMaterial(priority);
        String displayName = format(lang.getString("report.detail.priority.name", "&cPriority: %priority%"))
                .replace("%priority%", priority);
        List<String> lore = new ArrayList<>();
        for (String line : lang.getStringList("report.detail.priority.lore")) {
            lore.add(format(line)
                    .replace("%priority%", priority)
                    .replace("%factors%", getPriorityFactors())
            );
        }
        return buildItem(material, displayName, lore, "action_type", "priority");
    }

        private ItemStack buildStatusItem() {
            String status = report.getStatus().getDisplayName();
            Material material = getStatusMaterial(report.getStatus());
            String displayName = format(lang.getString("report.detail.status.name", "&bStatus: %status%"))
                    .replace("%status%", status);
            List<String> lore = new ArrayList<>();
            for (String line : lang.getStringList("report.detail.status.lore")) {
                lore.add(format(line)
                        .replace("%status%", status)
                        .replace("%updated_by%", getLastUpdatedBy())
                        .replace("%updated_at%", getLastUpdatedTime())
                        .replace("%time_since%", getTimeSinceReport())
                );
            }
            return buildItem(material, displayName, lore, "action_type", "status");
        }

    private ItemStack buildItem(Material material, String displayName, List<String> lore, String key, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        NamespacedKey nkey = new NamespacedKey(plugin, key);
        meta.getPersistentDataContainer().set(nkey, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorders(Inventory inv) {
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = borderItem.getItemMeta();
        meta.setDisplayName(" ");
        borderItem.setItemMeta(meta);
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, borderItem);
            if (inv.getItem(i + 45) == null) inv.setItem(i + 45, borderItem);
        }
        for (int i = 0; i < 6; i++) {
            if (inv.getItem(i * 9) == null) inv.setItem(i * 9, borderItem);
            if (inv.getItem(i * 9 + 8) == null) inv.setItem(i * 9 + 8, borderItem);
        }
    }

    private String calculatePriority() {
        int priorityScore = 0;
        priorityScore += calculateSeverityScore(report.getReason());
        long hoursSinceReport = (System.currentTimeMillis() - report.getCreatedAt()) / (1000 * 60 * 60);
        priorityScore += calculateAgingScore(hoursSinceReport);
        return getPriorityLevel(priorityScore);
    }

    private int calculateSeverityScore(String reason) {
        String lowerReason = reason.toLowerCase();
        if (lowerReason.contains("hack") || lowerReason.contains("cheat")) return 3;
        if (lowerReason.contains("grief") || lowerReason.contains("spam")) return 2;
        return 1;
    }

    private int calculateAgingScore(long hours) {
        if (hours > 24) return 2;
        if (hours > 12) return 1;
        return 0;
    }

    private String getPriorityLevel(int score) {
        ConfigurationSection priorities = lang.getConfigurationSection("report.detail.priorities");
        if (priorities == null) return "LOW";
        List<String> order = Arrays.asList("CRITICAL", "HIGH", "NORMAL", "LOW");
        Map<String, Integer> thresholds = new LinkedHashMap<>();
        thresholds.put("CRITICAL", 5);
        thresholds.put("HIGH", 3);
        thresholds.put("NORMAL", 2);
        thresholds.put("LOW", 0);
        for (String key : order) {
            Integer threshold = thresholds.get(key);
            if (threshold != null && score >= threshold) return key;
        }
        return "LOW";
    }

    private Material getPriorityMaterial(String priority) {
        String path = "report.detail.priorities." + priority.toUpperCase();
        String materialName = lang.getString(path, "LIME_CONCRETE");
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException ignored) {
            return Material.LIME_CONCRETE;
        }
    }

    private Material getStatusMaterial(ReportStatus status) {
        switch (status) {
            case PENDING:
                return getClockItem().getType();
            case IN_PROGRESS:
                return Material.COMPASS;
            case CLOSED:
                return Material.EMERALD_BLOCK;
            default:
                return Material.BARRIER;
        }
    }

    private String getReportCategory() {
        String reason = report.getReason().toLowerCase();
        ConfigurationSection categoriesSection = lang.getConfigurationSection("report.detail.categories");
        if (categoriesSection != null) {
            for (String category : categoriesSection.getKeys(false)) {
                for (String keyword : categoriesSection.getStringList(category)) {
                    if (reason.contains(keyword.toLowerCase())) return category;
                }
            }
        }
        return "Other";
    }

    private String getPriorityFactors() {
        List<String> factors = lang.getStringList("report.detail.priority-factors");
        if (factors == null || factors.isEmpty()) factors = Arrays.asList("Gravity", "Recidivism", "Seniority");
        return factors.stream().map(f -> "§7- " + f).collect(Collectors.joining("\n"));
    }

    private String getTimeSinceReport() {
        long minutes = (System.currentTimeMillis() - report.getCreatedAt()) / (1000 * 60);
        if (minutes < 60) {
            String format = lang.getString("report.detail.time.minutes", "%duration% minutes ago");
            return format.replace("%duration%", String.valueOf(minutes));
        }
        long hours = minutes / 60;
        if (hours < 24) {
            String format = lang.getString("report.detail.time.hours", "%duration% hours ago");
            return format.replace("%duration%", String.valueOf(hours));
        }
        long days = hours / 24;
        String format = lang.getString("report.detail.time.days", "%duration% days ago");
        return format.replace("%duration%", String.valueOf(days));
    }

    private String getPlayerJoinDate(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player != null && player.hasPlayedBefore()) {
            return new SimpleDateFormat("dd/MM/yyyy").format(new Date(player.getFirstPlayed()));
        }
        return lang.getString("report.detail.unknown.join-date", "Unknown");
    }

    private String getAssignedStaffName() {
        if (report.getAssignedTo() == null) return lang.getString("report.detail.unassigned", "Unassigned");
        OfflinePlayer staff = Bukkit.getOfflinePlayer(report.getAssignedTo());
        return staff != null && staff.getName() != null ? staff.getName() : lang.getString("report.detail.unknown.staff", "Unknown Staff");
    }

    private String getLastUpdatedBy() {
        UUID uuid = report.getLastUpdatedBy();
        if (uuid != null) {
            OfflinePlayer staff = Bukkit.getOfflinePlayer(uuid);
            return staff.getName() != null ? staff.getName() : uuid.toString();
        }
        return lang.getString("report.detail.updated-by-system", "System");
    }

    private String getLastUpdatedTime() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(report.getUpdatedAt()));
    }

    private String getLocationString() {
        if (report.getLocation() == null) return lang.getString("report.detail.unknown.location", "Unknown");
        return lang.getString("report.detail.location-format", "X: %x%, Y: %y%, Z: %z%")
                .replace("%x%", String.valueOf(report.getLocation().getBlockX()))
                .replace("%y%", String.valueOf(report.getLocation().getBlockY()))
                .replace("%z%", String.valueOf(report.getLocation().getBlockZ()));
    }

    private String getWorldName() {
        if (report.getLocation() == null || report.getLocation().getWorld() == null)
            return lang.getString("report.detail.unknown.world", "Unknown");
        return report.getLocation().getWorld().getName();
    }

    private String getBiome() {
        if (report.getLocation() == null) return lang.getString("report.detail.unknown.biome", "Unknown");
        return report.getLocation().getBlock().getBiome().name();
    }

    private String getWeather() {
        if (report.getLocation() == null || report.getLocation().getWorld() == null)
            return lang.getString("report.detail.unknown.weather", "Unknown");
        World w = report.getLocation().getWorld();
        if (w.hasStorm()) return lang.getString("report.detail.weather.rain", "Rain");
        if (w.isThundering()) return lang.getString("report.detail.weather.thunder", "Thunderstorm");
        return lang.getString("report.detail.weather.clear", "Clear");
    }

    private String getTimeOfDay() {
        if (report.getLocation() == null || report.getLocation().getWorld() == null)
            return lang.getString("report.detail.unknown.time", "Unknown");
        long time = report.getLocation().getWorld().getTime();
        if (time >= 0 && time < 12300) return lang.getString("report.detail.time-day.day", "Day");
        if (time >= 12300 && time < 23850) return lang.getString("report.detail.time-day.night", "Night");
        return lang.getString("report.detail.unknown.time", "Unknown");
    }

    private String getNearbyPlayersCount() {
        if (report.getLocation() == null || report.getLocation().getWorld() == null) {
            return lang.getString("report.detail.unknown.nearby-players", "0");
        }
        int radius = 20;
        long count = report.getLocation().getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(report.getLocation()) <= radius)
                .count();
        return String.valueOf(count);
    }

    private String getOnlineStaffCount() {
        return String.valueOf(Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("lifemod.staff")).count());
    }

    private String format(String s) {
        return MessageUtil.formatMessage(s);
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