package fr.lampalon.lifemod.listeners.utils;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Staffchatevent implements Listener {
    public Messages messages;
    private LifeMod plugin;

    public Staffchatevent(LifeMod plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playermsg = LifeMod.getInstance().getConfig().getString("staffmsg");

        if (player.hasPermission("lifemod.staffchat")) {
            String message = event.getMessage();

            if (message.startsWith("@")) {
                event.setCancelled(true);

                for (Player recipient : plugin.getServer().getOnlinePlayers()) {
                    if (recipient.hasPermission("lifemod.staffchat")) {
                        recipient.sendMessage( ChatColor.translateAlternateColorCodes('&', playermsg.replace("%player%", event.getPlayer().getName()) + "» " + message.substring(1)));
                    }
                }
            }
        }
    }
}
