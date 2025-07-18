package fr.lampalon.lifemod.bukkit.managers;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class SpectateManager {
    private final Map<UUID, Location> originalLocations = new HashMap<>();
    private final Map<UUID, UUID> lastTargets = new HashMap<>();
    private final Map<UUID, Boolean> isFreecam = new HashMap<>();
    private final Map<UUID, UUID> spectateTarget = new HashMap<>();

    private String getLang(String key) {
        String msg = LifeMod.getInstance().getLangConfig().getString(key);
        return msg != null ? msg : "§c[Lang] Missing key: " + key;
    }

    public void startSpectate(Player staff, Player target) {
        if (staff.equals(target)) {
            staff.sendMessage(MessageUtil.formatMessage(getLang("spectate.self")));
            return;
        }
        if (isSpectating(staff)) {
            staff.sendMessage(MessageUtil.formatMessage(getLang("spectate.already")));
            return;
        }
        originalLocations.put(staff.getUniqueId(), staff.getLocation());
        isFreecam.put(staff.getUniqueId(), false);

        UUID previousTarget = spectateTarget.get(staff.getUniqueId());
        if (previousTarget != null) {
            lastTargets.put(staff.getUniqueId(), previousTarget);
        }

        spectateTarget.put(staff.getUniqueId(), target.getUniqueId());

        staff.setGameMode(GameMode.SPECTATOR);
        staff.setSpectatorTarget(target);
        staff.sendMessage(MessageUtil.formatMessage(
                getLang("spectate.spectate-start").replace("%target%", target.getName())));
    }

    public void startFreecam(Player staff) {
        if (isSpectating(staff) && !isFreecam.get(staff.getUniqueId())) {
            isFreecam.put(staff.getUniqueId(), true);
            staff.setSpectatorTarget(null);
            staff.sendMessage(MessageUtil.formatMessage(getLang("spectate.freecam-start")));
            return;
        }
        if (!isSpectating(staff)) {
            originalLocations.put(staff.getUniqueId(), staff.getLocation());
        }
        isFreecam.put(staff.getUniqueId(), true);
        spectateTarget.remove(staff.getUniqueId());
        staff.setGameMode(GameMode.SPECTATOR);
        staff.setSpectatorTarget(null);
        staff.sendMessage(MessageUtil.formatMessage(getLang("spectate.freecam-start")));
    }

    public void leaveSpectate(Player staff) {
        if (!isSpectating(staff)) {
            staff.sendMessage(MessageUtil.formatMessage(getLang("spectate.not-spectating")));
            return;
        }

        staff.setSpectatorTarget(null);

        Location originalLocation = originalLocations.remove(staff.getUniqueId());
        isFreecam.remove(staff.getUniqueId());
        spectateTarget.remove(staff.getUniqueId());

        staff.setGameMode(GameMode.SURVIVAL);

        if (originalLocation != null && originalLocation.getWorld() != null) {
            staff.teleport(originalLocation);
            staff.sendMessage(MessageUtil.formatMessage(getLang("spectate.spectate-leave")));
        } else {
            staff.sendMessage(MessageUtil.formatMessage(getLang("spectate.spectate-leave-no-pos")));
        }
    }

    public void spectateRandom(Player spectator) {
        List<Player> candidates = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(spectator) && !p.hasPermission("lifemod.staff")) {
                candidates.add(p);
            }
        }
        if (candidates.isEmpty()) {
            spectator.sendMessage(MessageUtil.formatMessage(getLang("spectate.random-error")));
            return;
        }
        Player target = candidates.get(new Random().nextInt(candidates.size()));
        startSpectate(spectator, target);
        spectator.sendMessage(MessageUtil.formatMessage(
                getLang("spectate.random-success").replace("%target%", target.getName())));
    }

    public void spectateBack(Player spectator) {
        UUID last = lastTargets.get(spectator.getUniqueId());
        if (last == null) {
            spectator.sendMessage(MessageUtil.formatMessage(getLang("spectate.back-error")));
            return;
        }
        Player target = Bukkit.getPlayer(last);
        if (target == null || !target.isOnline()) {
            spectator.sendMessage(MessageUtil.formatMessage(
                    getLang("spectate.player-not-found").replace("%target%", "last")));
            return;
        }
        startSpectate(spectator, target);
        spectator.sendMessage(MessageUtil.formatMessage(
                getLang("spectate.back-success").replace("%target%", target.getName())));
    }

    public void sendPlayerList(Player spectator) {
        spectator.sendMessage(MessageUtil.formatMessage(getLang("spectate.list-header")));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(spectator)) {
                String display = MessageUtil.formatMessage(
                        getLang("spectate.list-player").replace("%player%", p.getName()));
                net.md_5.bungee.api.chat.TextComponent msg =
                        new net.md_5.bungee.api.chat.TextComponent(display);
                msg.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                        net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/spectate " + p.getName()));
                msg.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                        net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                        new net.md_5.bungee.api.chat.ComponentBuilder("Click to spectate " + p.getName()).create()));
                spectator.spigot().sendMessage(msg);
            }
        }
    }

    public boolean isSpectating(Player player) {
        return originalLocations.containsKey(player.getUniqueId());
    }

    public boolean isFreecam(Player player) {
        return isFreecam.getOrDefault(player.getUniqueId(), false);
    }
}