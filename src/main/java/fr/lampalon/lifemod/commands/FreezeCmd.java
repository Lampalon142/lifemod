package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.FreezeManager;
import fr.lampalon.lifemod.utils.MessageUtil;
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

public class FreezeCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;
    private final FreezeManager freezeManager;

    public FreezeCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
        this.freezeManager = plugin.getFreezeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("freeze")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.freeze")) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("commands", "Permission denied for /freeze by " + player.getName());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("freeze.usage")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("freeze.yourself")));
            return true;
        }

        if (freezeManager.isPlayerFrozen(target.getUniqueId())) {
            freezeManager.unfreezePlayer(player, target);
            target.sendMessage(MessageUtil.formatMessage(
                    plugin.getLangConfig().getString("freeze.messages.unfreeze.target")
                            .replace("%player%", player.getName())));
            player.sendMessage(MessageUtil.formatMessage(
                    plugin.getLangConfig().getString("freeze.messages.unfreeze.mod")
                            .replace("%target%", target.getName())));
            debug.log("freeze", player.getName() + " unfroze " + target.getName());
        } else {
            freezeManager.freezePlayer(player, target);
            plugin.getLangConfig().getStringList("freeze.messages.onfreeze")
                    .forEach(msg -> target.sendMessage(MessageUtil.formatMessage(msg)));
            player.sendMessage(MessageUtil.formatMessage(
                    plugin.getLangConfig().getString("freeze.messages.freeze.mod")
                            .replace("%target%", target.getName())));
            debug.log("freeze", player.getName() + " froze " + target.getName());
        }

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.freeze.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.freeze.description")
                                .replace("%player%", sender.getName()))
                        .setFooter(
                                plugin.getConfigConfig().getString("discord.freeze.footer.title"),
                                plugin.getConfigConfig().getString("discord.freeze.footer.logo")
                                        .replace("%player%", sender.getName())
                        )
                        .setColor(Color.decode(Objects.requireNonNull(
                                plugin.getConfigConfig().getString("discord.freeze.color")
                        ))));
                webhook.execute();
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord freeze alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("freeze")) return List.of();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
