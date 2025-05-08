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
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InvseeCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public InvseeCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("invsee")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            debug.log("invsee", "Console tried to use /invsee");
            return true;
        }

        if (!sender.hasPermission("lifemod.invsee")) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("invsee", "Permission denied for /invsee by " + sender.getName());
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("invsee.usage")));
            debug.log("invsee", "Invalid usage by " + sender.getName());
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
            debug.log("invsee", "Target offline: " + args[0]);
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.invsee.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.invsee.description").replace("%player%", sender.getName()))
                        .setFooter(plugin.getConfigConfig().getString("discord.invsee.footer.title"),
                                plugin.getConfigConfig().getString("discord.invsee.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.invsee.color")))));
                webhook.execute();
                debug.log("invsee", sender.getName() + " opened inventory of " + targetPlayer.getName() + " (Discord notified)");
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord invsee alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            }
        } else {
            debug.log("invsee", sender.getName() + " opened inventory of " + targetPlayer.getName());
        }

        player.openInventory(targetPlayer.getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("invsee")) return null;
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
