package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BroadcastCmd implements CommandExecutor, TabCompleter {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (cmd.getName().equalsIgnoreCase("broadcast")) {
                if (!sender.hasPermission("lifemod.bc")) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("bc.usage")));
                    return true;
                } else {
                    StringBuilder message = new StringBuilder();
                    for (String arg : args) {
                        message.append(arg).append(" ");
                    }

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("bc.prefix") + message.toString()));
                    }
                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.broadcast.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.broadcast.description").replace("%player%", sender.getName()).replace("%message%", message))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.broadcast.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.broadcast.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.broadcast.color")))));
                        try {
                            webhook.execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return true;
            }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("broadcast") || cmd.getName().equalsIgnoreCase("bc")){
            if (args.length == 1) {
                return LifeMod.getInstance().getLangConfig().getStringList("bc.tabcompleter");
            }
        }
        return null;
    }
}