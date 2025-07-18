package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GodModCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public GodModCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        Player targetPlayer;
        if (args.length > 0) {
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
                return true;
            }
        } else {
            targetPlayer = (Player) sender;
        }

        if (!sender.hasPermission("lifemod.god")) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("commands", "Permission denied for /god by " + sender.getName());
            return true;
        }

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.god.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.god.description").replace("%player%", sender.getName()))
                        .setFooter(plugin.getConfigConfig().getString("discord.god.footer.title"),
                                plugin.getConfigConfig().getString("discord.god.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.god.color")))));
                webhook.execute();
                debug.log("god", sender.getName() + " used /god on " + targetPlayer.getName() + " (Discord notified)");
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord god alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            debug.log("god", sender.getName() + " used /god on " + targetPlayer.getName());
        }

        if (targetPlayer.isInvulnerable()) {
            targetPlayer.setInvulnerable(false);
            targetPlayer.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("god.deactivate.own")));
            if (!targetPlayer.equals(sender)) {
                sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("god.deactivate.other").replace("%player%", targetPlayer.getName())));
            }
            debug.log("god", "God mode disabled for " + targetPlayer.getName() + " by " + sender.getName());
        } else {
            targetPlayer.setInvulnerable(true);
            targetPlayer.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("god.activate.own")));
            if (!targetPlayer.equals(sender)) {
                sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("god.activate.other").replace("%player%", targetPlayer.getName())));
            }
            debug.log("god", "God mode enabled for " + targetPlayer.getName() + " by " + sender.getName());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("god")) return null;
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
