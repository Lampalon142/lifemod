package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class SpectateManager {
    private final Map<UUID, Location> originalLocations = new HashMap<>();
    private final Map<UUID, UUID> lastTargets = new HashMap<>();
    private final Map<UUID, Boolean> freecamMode = new HashMap<>();

    public void startSpectate(Player spectator, Player target) {
        if (spectator.equals(target)) {
            spectator.sendMessage(MessageUtil.formatMessage(
                    LifeMod.getInstance().getLangConfig().getString("spectate.self-spectate")));
            return;
        }
        if (!isSpectating(spectator)) {
            originalLocations.put(spectator.getUniqueId(), spectator.getLocation());
        }
        lastTargets.put(spectator.getUniqueId(), target.getUniqueId());
        freecamMode.put(spectator.getUniqueId(), false);

        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.teleport(target.getLocation());
        spectator.sendMessage(MessageUtil.formatMessage(
                LifeMod.getInstance().getLangConfig().getString("spectate.spectate")
                        .replace("%target%", target.getName())));
    }

    public void startFreecam(Player spectator) {
        if (!isSpectating(spectator)) {
            originalLocations.put(spectator.getUniqueId(), spectator.getLocation());
        }
        freecamMode.put(spectator.getUniqueId(), true);

        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.sendMessage(MessageUtil.formatMessage(
                LifeMod.getInstance().getLangConfig().getString("spectate.fp-spectate")));
    }

    public void leaveSpectate(Player spectator) {
        if (!isSpectating(spectator)) {
            spectator.sendMessage(MessageUtil.formatMessage(
                    LifeMod.getInstance().getLangConfig().getString("spectate.leave.error")));
            return;
        }
        Location originalLocation = originalLocations.remove(spectator.getUniqueId());
        freecamMode.remove(spectator.getUniqueId());
        lastTargets.remove(spectator.getUniqueId());

        spectator.setGameMode(GameMode.SURVIVAL);
        if (originalLocation != null && originalLocation.getWorld() != null) {
            spectator.teleport(originalLocation);
        }
        spectator.sendMessage(MessageUtil.formatMessage(
                LifeMod.getInstance().getLangConfig().getString("spectate.leave.success")));
    }

    public void spectateRandom(Player spectator) {
        List<Player> candidates = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(spectator) && !p.hasPermission("lifemod.staff")) {
                candidates.add(p);
            }
        }
        if (candidates.isEmpty()) {
            spectator.sendMessage(MessageUtil.formatMessage(
                    LifeMod.getInstance().getLangConfig().getString("spectate.random-error")));
            return;
        }
        Player target = candidates.get(new Random().nextInt(candidates.size()));
        startSpectate(spectator, target);
        spectator.sendMessage(MessageUtil.formatMessage(
                LifeMod.getInstance().getLangConfig().getString("spectate.random-success")
                        .replace("%target%", target.getName())));
    }

    public void spectateBack(Player spectator) {
        UUID last = lastTargets.get(spectator.getUniqueId());
        if (last == null) {
            spectator.sendMessage(MessageUtil.formatMessage(
                    LifeMod.getInstance().getLangConfig().getString("spectate.back-error")));
            return;
        }
        Player target = Bukkit.getPlayer(last);
        if (target == null || !target.isOnline()) {
            spectator.sendMessage(MessageUtil.formatMessage(
                    LifeMod.getInstance().getLangConfig().getString("spectate.player-not-found")
                            .replace("%target%", "last")));
            return;
        }
        startSpectate(spectator, target);
        spectator.sendMessage(MessageUtil.formatMessage(
                LifeMod.getInstance().getLangConfig().getString("spectate.back-success")
                        .replace("%target%", target.getName())));
    }

    public void sendPlayerList(Player spectator) {
        spectator.sendMessage(MessageUtil.formatMessage(
                LifeMod.getInstance().getLangConfig().getString("spectate.list-header")));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(spectator)) {
                String display = MessageUtil.formatMessage(
                        LifeMod.getInstance().getLangConfig().getString("spectate.list-player")
                                .replace("%player%", p.getName()));
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
        return freecamMode.getOrDefault(player.getUniqueId(), false);
    }
}