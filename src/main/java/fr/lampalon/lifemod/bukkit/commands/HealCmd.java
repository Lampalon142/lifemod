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

public class HealCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public HealCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("heal")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.heal")) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("commands", "Permission denied for /heal by " + player.getName());
            return true;
        }

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.heal.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.heal.description").replace("%player%", sender.getName()))
                        .setFooter(plugin.getConfigConfig().getString("discord.heal.footer.title"),
                                plugin.getConfigConfig().getString("discord.heal.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.heal.color")))));
                webhook.execute();
                debug.log("heal", sender.getName() + " used /heal (Discord notified)");
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord heal alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            debug.log("heal", sender.getName() + " used /heal");
        }

        if (args.length == 0) {
            double maxHealth = player.getMaxHealth();
            player.setHealth(maxHealth);
            player.setFoodLevel(player.getFoodLevel());
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("heal.player")));
            debug.log("heal", player.getName() + " healed himself");
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                double maxHealth = target.getMaxHealth();
                target.setHealth(maxHealth);
                target.setFoodLevel(target.getFoodLevel());
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("heal.mod").replace("%player%", target.getName())));
                target.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("heal.player")));
                debug.log("heal", player.getName() + " healed " + target.getName());
            } else {
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
            }
        } else {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("heal.usage")));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("heal")) return null;
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
