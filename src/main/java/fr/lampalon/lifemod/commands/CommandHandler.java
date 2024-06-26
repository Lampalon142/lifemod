package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class CommandHandler implements CommandExecutor {
    private final LifeMod plugin;

    public CommandHandler(LifeMod plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (plugin.isLifemodActive()) {
            if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
                DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.lifemod.title"))
                        .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.lifemod.description").replace("%player%", sender.getName()))
                        .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.lifemod.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.lifemod.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.lifemod.color")))));
                try {
                    webhook.execute();
                } catch (IOException e) {
                    LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                }
            }
            if (sender.hasPermission("lifemod.use")) {
                if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

                    plugin.reloadConfig();
                    plugin.ConfigConfig();

                    sender.sendMessage(MessageUtil.parseColors(plugin.getConfigConfig().getString("prefix") + plugin.getConfigConfig().getString("config-reloaded")));
                } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {

                    if (!(sender instanceof Player)){
                        sender.sendMessage(MessageUtil.parseColors(plugin.getConfigConfig().getString("prefix") + plugin.getConfigConfig().getString("onlyplayer")));
                    } else {
                        Inventory inv = Bukkit.getServer().createInventory(null, plugin.getConfigConfig().getInt("lifemod.help.slots"), plugin.getConfigConfig().getString("lifemod.help.title"));

                    }

                } else {
                    sender.sendMessage(MessageUtil.parseColors(plugin.getConfigConfig().getString("prefix") + plugin.getConfigConfig().getString("lifemod.usage")));
                }
                return true;
            } else {
                sender.sendMessage(MessageUtil.parseColors(plugin.getConfigConfig().getString("prefix") + plugin.getConfigConfig().getString("nopermission")));
                return false;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(plugin.getConfigConfig().getString("prefix") + plugin.getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }
}
