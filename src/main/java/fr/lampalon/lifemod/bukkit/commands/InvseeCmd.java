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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            debug.log("invsee", sender.getName() + " opened inventory of " + targetPlayer.getName());
        }

        player.openInventory(openTargetInventory(player, targetPlayer));
        return true;
    }

    private Inventory openTargetInventory(Player player, Player target) {
        String invTitle = LifeMod.getInstance().getLangConfig().getString("invsee.name");
        Inventory targetInventory = Bukkit.createInventory(null, 45, MessageUtil.formatMessage(invTitle.replace("%player%", target.getName())));
        PlayerInventory targetPlayerInventory = target.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack item = targetPlayerInventory.getItem(i);
            if (item != null) {
                targetInventory.setItem(i, item.clone());
            }
        }
        targetInventory.setItem(36, targetPlayerInventory.getHelmet());
        targetInventory.setItem(37, targetPlayerInventory.getChestplate());
        targetInventory.setItem(38, targetPlayerInventory.getLeggings());
        targetInventory.setItem(39, targetPlayerInventory.getBoots());

        player.openInventory(targetInventory);
        debug.log("mod", player.getName() + " opened inventory of " + target.getName());
        return targetInventory;
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
