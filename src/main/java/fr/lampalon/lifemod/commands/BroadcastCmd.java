package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class BroadcastCmd implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (LifeMod.getInstance().isBroadcastActive()) {
            if (cmd.getName().equalsIgnoreCase("broadcast")) {
                if (!sender.hasPermission("lifemod.bc")) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("bc-usage")));
                    return true;
                } else {
                    StringBuilder message = new StringBuilder();
                    for (String arg : args) {
                        message.append(arg).append(" ");
                    }

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("bc") + message.toString()));
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
                        } catch(IOException e) {
                            LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                        }
                    }
                }
                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }
}