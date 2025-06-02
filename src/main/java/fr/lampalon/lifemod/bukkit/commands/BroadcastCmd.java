package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.manager.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BroadcastCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public BroadcastCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("broadcast") && !cmd.getName().equalsIgnoreCase("bc")) return false;

        if (!sender.hasPermission("lifemod.bc")) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("commands", "Permission denied for /broadcast by " + sender.getName());
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("bc.usage")));
            return true;
        }

        String message = String.join(" ", args).replace("\\n", "\n");
        String broadcast = MessageUtil.parseColors(plugin.getLangConfig().getString("bc.prefix", "") + message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(broadcast);
        }
        Bukkit.getConsoleSender().sendMessage(broadcast);

        debug.log("broadcast", "Broadcast sent by " + sender.getName() + ": " + message);

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.broadcast.title"))
                        .setDescription(plugin.getConfigConfig()
                                .getString("discord.broadcast.description")
                                .replace("%player%", sender.getName())
                                .replace("%message%", message))
                        .setFooter(
                                plugin.getConfigConfig().getString("discord.broadcast.footer.title"),
                                plugin.getConfigConfig().getString("discord.broadcast.footer.logo")
                                        .replace("%player%", sender.getName())
                        )
                        .setColor(Color.decode(Objects.requireNonNull(
                                plugin.getConfigConfig().getString("discord.broadcast.color")
                        ))));
                webhook.execute();
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("broadcast") && !cmd.getName().equalsIgnoreCase("bc")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = plugin.getLangConfig().getStringList("bc.tabcompleter");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}