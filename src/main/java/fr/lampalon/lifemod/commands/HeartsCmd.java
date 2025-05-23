package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HeartsCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;
    private static final double MAX_HEARTS = 20.0;

    public HeartsCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("lifemod.hearts")) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.usage")));
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.invalid-amount")));
            return true;
        }

        double newHealth = switch (action) {
            case "set" -> Math.min(amount, MAX_HEARTS);
            case "add" -> Math.min(target.getHealth() + amount, MAX_HEARTS);
            default -> target.getHealth();
        };

        if (newHealth >= MAX_HEARTS) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.max-reached")));
            return true;
        }

        target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        target.setHealth(newHealth);

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.hearts.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.hearts.description")
                                .replace("%player%", sender.getName())
                                .replace("%target%", target.getName())
                                .replace("%action%", action)
                                .replace("%amount%", String.valueOf(amount)))
                        .setFooter(plugin.getConfigConfig().getString("discord.hearts.footer.title"),
                                plugin.getConfigConfig().getString("discord.hearts.footer.logo"))
                        .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.hearts.color")))));
                webhook.execute();
            } catch (IOException e) {
                debug.log("discord", "Webhook error: " + e.getMessage());
            }
        }

        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.success")
                .replace("%action%", action)
                .replace("%target%", target.getName())
                .replace("%amount%", String.valueOf(amount))));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("set", "add");
        if (args.length == 2) return null;
        return new ArrayList<>();
    }
}
