package fr.lampalon.lifemod.listeners.players;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import fr.lampalon.lifemod.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final LifeMod plugin;
    private final UpdateChecker updateChecker;

    public PlayerJoin(LifeMod plugin, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("lifemod.notify")) {
            updateChecker.checkForUpdates(result -> {
                Bukkit.getScheduler().runTask(plugin, () -> {

                    if (updateChecker.getLatestVersionS() == null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[LifeMod] Failed to retrieve the latest version."));
                    } else if (result == UpdateChecker.UpdateCheckResult.OUT_DATED) {
                        String message = "&dHello " + player.getName() + "\n" +
                                "&bLifeMod plugin has an available update!\n" +
                                "&eYour version: &c" + updateChecker.getCurrentVersionS() + "\n" +
                                "&eNew version: &a" + updateChecker.getLatestVersionS() + "\n" +
                                "&eLink: &bhttps://www.spigotmc.org/resources/1-8-1-20-lifemod-moderation-plugin.112381/" + "\n" +
                                "&r\n" +
                                "&e===========================";
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                    } else if (result == UpdateChecker.UpdateCheckResult.UP_TO_DATE) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a[LifeMod] LifeMod is up to date."));
                    } else if (result == UpdateChecker.UpdateCheckResult.UNRELEASED) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e[LifeMod] You are using a version not yet released."));
                    } else if (result == UpdateChecker.UpdateCheckResult.NO_RESULT) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[LifeMod] Unable to check for updates."));
                    }
                });
            });
        }
    }
}
