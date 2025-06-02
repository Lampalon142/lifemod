package fr.lampalon.lifemod.bukkit.manager;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatManager implements Listener {
    private final LifeMod plugin;
    private final DebugManager debug;
    private boolean enabled;
    private List<String> blacklist;

    public ChatManager(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
        reloadConfig();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void reloadConfig() {
        enabled = plugin.getConfig().getBoolean("chatManager.enabled", true);
        blacklist = plugin.getConfig().getStringList("chatManager.blacklist");
        debug.log("chat", "ChatManager configuration reloaded. Enabled: " + enabled + ", Blacklist: " + blacklist);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (enabled) {
            String message = event.getMessage();
            Player player = event.getPlayer();
            if (!player.hasPermission("lifemod.chat.bypass")) {
                for (String word : blacklist) {
                    if (message.toLowerCase().contains(word.toLowerCase())) {
                        event.setMessage(filterWord(message, word));
                        notifyViewers(player, word);
                        debug.log("chat", player.getName() + " used blacklisted word: " + word);
                        break;
                    }
                }
            }
        }
    }

    private String filterWord(String message, String word) {
        return message.replaceAll("(?i)" + word, "***");
    }

    private void notifyViewers(Player sender, String word) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("lifemod.chat.views")) {
                String notificationMessage = plugin.getConfigConfig().getString("chatManager.notification", "");
                player.sendMessage(MessageUtil.formatMessage("%prefix%" + notificationMessage.replace("%player%", sender.getName()).replace("%word%", word)));
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("chatManager.enabled", enabled);
        plugin.saveConfig();
        debug.log("chat", "ChatManager enabled set to " + enabled);
    }

    public void addToBlacklist(String word) {
        blacklist.add(word);
        plugin.getConfig().set("chatManager.blacklist", blacklist);
        plugin.saveConfig();
        debug.log("chat", "Added word to blacklist: " + word);
    }

    public void removeFromBlacklist(String word) {
        blacklist.remove(word);
        plugin.getConfig().set("chatManager.blacklist", blacklist);
        plugin.saveConfig();
        debug.log("chat", "Removed word from blacklist: " + word);
    }
}
