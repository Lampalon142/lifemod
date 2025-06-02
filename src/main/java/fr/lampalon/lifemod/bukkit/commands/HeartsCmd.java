package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.manager.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
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

        if (args.length < 3) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.usage")));
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.unit-info")));
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
            return true;
        }

        double hearts;
        try {
            hearts = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.invalid-amount")));
            return true;
        }

        hearts = Math.round(hearts * 2.0) / 2.0;

        if (hearts <= 0) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.invalid-amount")));
            return true;
        }

        if (hearts > MAX_HEARTS) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.max-reached")));
            return true;
        }

        double healthPoints = hearts * 2.0;
        double newMaxHealth;
        switch (action) {
            case "set" -> newMaxHealth = healthPoints;
            case "add" -> newMaxHealth = Math.min(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() + healthPoints, MAX_HEARTS * 2.0);
            default -> {
                sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.usage")));
                return true;
            }
        }

        newMaxHealth = Math.round(newMaxHealth * 2.0) / 2.0;

        if (newMaxHealth > MAX_HEARTS * 2.0) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.max-reached")));
            return true;
        }

        if (newMaxHealth % 1 != 0) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.half-heart-warning")));
        }

        target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
        target.setHealth(Math.min(target.getHealth(), newMaxHealth));

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.hearts.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.hearts.description")
                                .replace("%player%", sender.getName())
                                .replace("%target%", target.getName())
                                .replace("%action%", action)
                                .replace("%amount%", String.valueOf(hearts)))
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
                .replace("%amount%", String.valueOf(hearts))));

        target.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("hearts.changed")
                .replace("%amount%", String.valueOf(newMaxHealth / 2.0))));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("set", "add");
        if (args.length == 2) return null;
        return new ArrayList<>();
    }
}
