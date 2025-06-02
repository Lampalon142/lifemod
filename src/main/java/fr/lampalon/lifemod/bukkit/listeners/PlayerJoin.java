package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final LifeMod plugin;
    private final UpdateChecker updateChecker;
    private final DebugManager debug;

    public PlayerJoin(LifeMod plugin, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;
        this.debug = plugin.getDebugManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("lifemod.notify")) {
            updateChecker.checkForUpdates(result -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (updateChecker.getLatestVersionS() == null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[LifeMod] Failed to retrieve the latest version."));
                        debug.log("update", "Failed to retrieve latest version for player " + player.getName());
                    } else if (result == UpdateChecker.UpdateCheckResult.OUT_DATED) {
                        String rawMessage = plugin.getLangConfig().getString("general.update.message");
                        if (rawMessage == null) {
                            rawMessage = "&dHello %player%\n" +
                                    "&bLifeMod plugin has an available update!\n" +
                                    "&eYour version: &c%current_version%\n" +
                                    "&eNew version: &a%latest_version%\n" +
                                    "&eLink: &bhttps://www.spigotmc.org/resources/1-8-1-20-lifemod-moderation-plugin.112381/\n" +
                                    "&r\n" +
                                    "&e===========================";
                        }
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                rawMessage.replace("%player%", player.getName())
                                        .replace("%current_version%", updateChecker.getCurrentVersionS())
                                        .replace("%latest_version%", updateChecker.getLatestVersionS())));
                        debug.log("update", "Notified player " + player.getName() + " about update.");
                    } else if (result == UpdateChecker.UpdateCheckResult.UP_TO_DATE) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[LifeMod] LifeMod is up to date."));
                        debug.log("update", "Player " + player.getName() + " has up-to-date plugin.");
                    } else if (result == UpdateChecker.UpdateCheckResult.UNRELEASED) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e[LifeMod] You are using a version not yet released."));
                        debug.log("update", "Player " + player.getName() + " is using unreleased version.");
                    } else if (result == UpdateChecker.UpdateCheckResult.NO_RESULT) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[LifeMod] Unable to check for updates."));
                        debug.log("update", "Unable to check updates for player " + player.getName());
                    }
                });
            });
        }
    }
}
