package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class DebugManager {
    private final LifeMod plugin;

    public DebugManager(LifeMod plugin) {
        this.plugin = plugin;
        if (plugin.getConfigConfig() == null) {
            Bukkit.getLogger().warning("[LifeMod] DebugManager initialized before config loaded!");
        }
    }

    public boolean isEnabled() {
        return plugin.getConfigConfig().getBoolean("debug.enabled", false);
    }

    public boolean isModuleEnabled(String module) {
        return plugin.getConfigConfig().getBoolean("debug.modules." + module.toLowerCase(), false);
    }

    public Level getLogLevel() {
        String level = plugin.getConfigConfig().getString("debug.log-level", "INFO").toUpperCase();
        switch (level) {
            case "DEBUG": return Level.FINE;
            case "WARNING": return Level.WARNING;
            case "ERROR": return Level.SEVERE;
            default: return Level.INFO;
        }
    }

    public void log(String module, String message) {
        if (!isEnabled() || !isModuleEnabled(module)) return;
        String prefix = MessageUtil.formatMessage(plugin.getConfigConfig().getString("debug.messages.prefix", "[LifeMod] "));
        Bukkit.getLogger().log(getLogLevel(), prefix + "[" + module.toUpperCase() + "] " + MessageUtil.formatMessage(message));
    }

    public void userError(CommandSender sender, String context, Exception e) {
        String prefix = MessageUtil.formatMessage(plugin.getConfigConfig().getString("debug.messages.prefix", "[LifeMod] "));
        String userMsg = MessageUtil.formatMessage(plugin.getConfigConfig().getString("debug.messages.user-error", "&cOops! An error occurred: &e%context%")).replace("%context%", context);
        String helpMsg = MessageUtil.formatMessage(plugin.getConfigConfig().getString("debug.messages.user-help", "&7If this keeps happening, contact an admin or check your config."));
        sender.sendMessage(prefix + userMsg);
        sender.sendMessage(prefix + helpMsg);

        if (isEnabled() && sender.hasPermission("lifemod.debug")) {
            sendAdminError(sender, e);
        }
    }

    public void sendAdminError(CommandSender sender, Exception e) {
        String adminMsg = MessageUtil.formatMessage(plugin.getConfigConfig().getString("debug.messages.admin-error", "&8[Debug] Error: %error% - %message%"))
                .replace("%error%", e.getClass().getSimpleName())
                .replace("%message%", e.getMessage() == null ? "No message" : e.getMessage());
        sender.sendMessage(adminMsg);

        StackTraceElement top = e.getStackTrace().length > 0 ? e.getStackTrace()[0] : null;
        if (top != null) {
            String locMsg = MessageUtil.formatMessage(plugin.getConfigConfig().getString("debug.messages.admin-location", "&8[Debug] At: %location%"))
                    .replace("%location%", top.toString());
            sender.sendMessage(locMsg);
        }
    }
}
