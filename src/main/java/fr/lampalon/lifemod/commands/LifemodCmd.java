package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LifemodCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public LifemodCmd(LifeMod plugin){
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("lifemod")) return false;

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.lifemod.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.lifemod.description").replace("%player%", sender.getName()))
                        .setFooter(plugin.getConfigConfig().getString("discord.lifemod.footer.title"),
                                plugin.getConfigConfig().getString("discord.lifemod.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.lifemod.color")))));
                webhook.execute();
                debug.log("lifemod", sender.getName() + " used /lifemod (Discord notified)");
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord lifemod alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            }
        } else {
            debug.log("lifemod", sender.getName() + " used /lifemod");
        }

        if (sender.hasPermission("lifemod.use")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                long start = System.currentTimeMillis();
                try {
                    plugin.reloadConfig();
                    plugin.reloadPluginConfig();
                    plugin.reloadLangConfig();
                    sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.config-reloaded")));
                    Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
                    Bukkit.getConsoleSender().sendMessage("§6LifeMod §8| §aConfig & Lang reloaded by §e" + sender.getName() + " §7(§b" + (System.currentTimeMillis()-start) + "ms§7)");
                    Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
                    debug.log("lifemod", sender.getName() + " reloaded LifeMod config in " + (System.currentTimeMillis()-start) + "ms");
                } catch (Exception e) {
                    sender.sendMessage("§c[LifeMod] §cError while reloading config: " + e.getMessage());
                    Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
                    Bukkit.getConsoleSender().sendMessage("§c[LifeMod] §cReload error: " + e.getMessage());
                    Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------------");
                    debug.log("lifemod", "Reload error: " + e.getMessage());
                }
                return true;
            }
            else if (args.length == 1 && args[0].equalsIgnoreCase("info")){
                sendInfo(sender);
                debug.log("lifemod", sender.getName() + " requested LifeMod info");
                return true;
            } else {
                sender.sendMessage(MessageUtil.formatMessage(plugin.getConfigConfig().getString("lifemod.usage")));
                debug.log("lifemod", sender.getName() + " used /lifemod with wrong arguments");
            }
            return true;
        } else {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("lifemod", "Permission denied for /lifemod by " + sender.getName());
            return false;
        }
    }

    private void sendInfo(CommandSender sender) {
        String pluginVersion = plugin.getDescription().getVersion();
        String serverVersion = Bukkit.getVersion();
        String bukkitVersion = Bukkit.getBukkitVersion();
        String databaseType = plugin.getConfigConfig().getString("database.type", "Not configured");
        boolean updateNotifier = plugin.getConfigConfig().getBoolean("update-notifier", false);
        boolean discordEnabled = plugin.getConfigConfig().getBoolean("discord.enabled", false);
        boolean useLuckPerms = plugin.getConfigConfig().getBoolean("UseLuckPerms", false);
        String authors = String.join(", ", plugin.getDescription().getAuthors());

        sender.sendMessage("§8§m--------------------------------------------------");
        sender.sendMessage("§6§lLifeMod §7§o- §fPlugin Info");
        sender.sendMessage(" §e• §fPlugin version: §a" + pluginVersion);
        sender.sendMessage(" §e• §fServer version: §a" + serverVersion + " §7(" + bukkitVersion + ")");
        sender.sendMessage(" §e• §fAuthors: §b" + authors);
        sender.sendMessage(" §e• §fDatabase: §d" + databaseType);
        sender.sendMessage(" §e• §fDiscord integration: " + (discordEnabled ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage(" §e• §fLuckPerms integration: " + (useLuckPerms ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage(" §e• §fUpdate notifier: " + (updateNotifier ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage(" §e• §fGithub: §9https://github.com/Lampalon/LifeMod");
        sender.sendMessage("§8§m--------------------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("lifemod")) {
            if (args.length == 1){
                List<String> suggestions = new ArrayList<>();
                suggestions.add("reload");
                suggestions.add("info");
                return suggestions;
            }
        }
        return null;
    }
}
