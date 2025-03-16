package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatManager implements Listener {
    private final LifeMod plugin;
    private boolean enabled;
    private List<String> blacklist;

    public ChatManager(LifeMod plugin) {
        this.plugin = plugin;
        reloadConfig();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void reloadConfig() {
        enabled = plugin.getConfig().getBoolean("chatManager.enabled", true);
        blacklist = plugin.getConfig().getStringList("chatManager.blacklist");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (enabled) {
            String message = event.getMessage();
            if (!event.getPlayer().hasPermission("lifemod.chat.bypass")){
                for (String word : blacklist) {
                    if (message.contains(word)) {
                        event.setMessage(filterWord(message, word));
                        notifyViewers(event.getPlayer());
                        break;
                    }
                }
            }
        }
    }

    private String filterWord(String message, String word) { return message.replaceAll(word, "***"); }

    private void notifyViewers(Player sender) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("lifemod.chat.views")) {
                String notificationMessage = LifeMod.getInstance().getConfigConfig().getString("chatManager.notification", "");
                notificationMessage = notificationMessage.replace("%player%", sender.getName());
                player.sendMessage(MessageUtil.formatMessage("%prefix%" + notificationMessage));
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("chatFilter.enabled", enabled);
        plugin.saveConfig();
    }

    public void addToBlacklist(String word) {
        blacklist.add(word);
        plugin.getConfig().set("chatFilter.blacklist", blacklist);
        plugin.saveConfig();
    }

    public void removeFromBlacklist(String word) {
        blacklist.remove(word);
        plugin.getConfig().set("chatFilter.blacklist", blacklist);
        plugin.saveConfig();
    }
}
