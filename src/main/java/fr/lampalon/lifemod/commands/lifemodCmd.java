package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class lifemodCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;

    public lifemodCmd(LifeMod plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
                    throw new RuntimeException(e);
                }
            }
            if (sender.hasPermission("lifemod.use")) {
                if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

                    plugin.reloadConfig();
                    plugin.reloadPluginConfig();

                    sender.sendMessage(MessageUtil.formatMessage(plugin.getConfigConfig().getString("prefix") + plugin.getLangConfig().getString("general.config-reloaded")));
                } //else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {}
                else {
                   sender.sendMessage(MessageUtil.formatMessage(plugin.getConfigConfig().getString("prefix") + plugin.getConfigConfig().getString("lifemod.usage")));
                }
                return true;
            } else {
                sender.sendMessage(MessageUtil.formatMessage(plugin.getConfigConfig().getString("prefix") + plugin.getLangConfig().getString("general.nopermission")));
                return false;
            }
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("lifemod")) {
            if (args.length == 1){
                List<String> suggestions = new ArrayList<>();
                suggestions.add("help");
                suggestions.add("reload");
                return suggestions;
            }
        }
        return null;
    }
}
