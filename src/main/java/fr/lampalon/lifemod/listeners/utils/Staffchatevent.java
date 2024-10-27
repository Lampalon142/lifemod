package fr.lampalon.lifemod.listeners.utils;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Staffchatevent implements Listener {
    private LifeMod plugin;

    public Staffchatevent(LifeMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playermsg = LifeMod.getInstance().getLangConfig().getString("staffchat.message");

        if (player.hasPermission("lifemod.staffchat")) {
            String message = event.getMessage();

            if (message.startsWith(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix")))) {
                event.setCancelled(true);

                for (Player recipient : plugin.getServer().getOnlinePlayers()) {
                    if (recipient.hasPermission("lifemod.staffchat")) {
                        recipient.sendMessage(MessageUtil.parseColors(playermsg.replace("%player%", event.getPlayer().getName()) + "» " + message.substring(1)));
                    }
                }
            }
        }
    }
}
