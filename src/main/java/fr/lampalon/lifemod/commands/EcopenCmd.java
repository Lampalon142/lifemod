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

public class EcopenCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isEcopenActive()) {
            if (label.equalsIgnoreCase("ecopen")) {
                if (sender instanceof Player) {

                    Player player = (Player) sender;

                    if (args.length != 1) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("usageopenec")));
                        return true;
                    }

                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (!player.hasPermission("lifemod.ecopen")) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                    } else {
                        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                            DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                    .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.ecopen.title"))
                                    .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.ecopen.description").replace("%player%", sender.getName()))
                                    .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.ecopen.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.ecopen.footer.logo").replace("%player%", sender.getName()))
                                    .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.ecopen.color")))));
                            try {
                                webhook.execute();
                            } catch(IOException e) {
                                LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                            }
                        }
                        player.openInventory(targetPlayer.getEnderChest());
                    }

                    if (targetPlayer == null) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                        return true;
                    }

                    if (targetPlayer == player) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("yourselfenderchest")));
                        return true;
                    }

                } else {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                }
                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }
}
