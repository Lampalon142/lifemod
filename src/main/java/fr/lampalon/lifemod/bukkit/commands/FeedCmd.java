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

public class FeedCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public FeedCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("feed")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.feed")) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("commands", "Permission denied for /feed by " + player.getName());
            return true;
        }

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.feed.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.feed.description")
                                .replace("%player%", sender.getName()))
                        .setFooter(
                                plugin.getConfigConfig().getString("discord.feed.footer.title"),
                                plugin.getConfigConfig().getString("discord.feed.footer.logo")
                                        .replace("%player%", sender.getName())
                        )
                        .setColor(Color.decode(Objects.requireNonNull(
                                plugin.getConfigConfig().getString("discord.feed.color")
                        ))));
                webhook.execute();
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord feed alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (args.length == 0) {
            player.setFoodLevel(20);
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("feed.yourself")));
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
                return true;
            }
            target.setFoodLevel(20);
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("feed.mod").replace("%target%", target.getName())));
            target.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("feed.player")));
            return true;
        }

        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("feed.usage")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("feed")) return null;
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
