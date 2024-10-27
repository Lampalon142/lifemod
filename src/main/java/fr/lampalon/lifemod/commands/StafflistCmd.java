package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.VanishedManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class StafflistCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (LifeMod.getInstance().isStafflistActive()) {
            if (label.equalsIgnoreCase("stafflist")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    StringBuilder modList = new StringBuilder(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("modlist.online")));

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("lifemod.stafflist") && !VanishedManager.isVanished(onlinePlayer)) {
                            modList.append(onlinePlayer.getName()).append(", ");
                        }
                    }

                    if (modList.length() > LifeMod.getInstance().getLangConfig().getString("modlist.online").length()) {
                        modList.delete(modList.length() - 2, modList.length());
                    } else {
                        modList.append(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("modlist.none")));
                    }

                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.color")))));
                        try {
                            webhook.execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    player.sendMessage(modList.toString());
                    return true;
                } else {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                    return true;
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }
}
