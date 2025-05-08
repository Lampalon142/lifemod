package fr.lampalon.lifemod.listeners.utils;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Staffchatevent implements Listener {
    private final LifeMod plugin;
    private final DebugManager debug;

    public Staffchatevent(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playermsg = plugin.getLangConfig().getString("staffchat.message");
        String prefix = MessageUtil.parseColors(plugin.getConfigConfig().getString("prefix"));

        if (player.hasPermission("lifemod.staffchat")) {
            String message = event.getMessage();

            if (message.startsWith(prefix)) {
                event.setCancelled(true);

                String staffMessage = MessageUtil.parseColors(
                        playermsg.replace("%player%", player.getName()) + "» " + message.substring(prefix.length())
                );

                for (Player recipient : plugin.getServer().getOnlinePlayers()) {
                    if (recipient.hasPermission("lifemod.staffchat")) {
                        recipient.sendMessage(staffMessage);
                    }
                }
                debug.log("staffchat", player.getName() + " sent staffchat message: " + message.substring(prefix.length()));
            }
        }
    }
}
