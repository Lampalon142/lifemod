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

public class FeedCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isFeedActive()) {
            if (label.equalsIgnoreCase("feed")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(LifeMod.getInstance().getConfigConfig().getString("onlyplayer"));
                    return false;
                }

                Player player = (Player) sender;

                if (player.hasPermission("lifemod.feed")) {

                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.feed.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.feed.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.feed.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.feed.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.feed.color")))));
                        try {
                            webhook.execute();
                        } catch(IOException e) {
                            LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                        }
                    }

                    if (args.length == 0) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("yourself-feed")));
                        player.setFoodLevel(20);

                    } else if (args.length == 1) {

                        Player target = Bukkit.getPlayer(args[0]);

                        if (target != null) {

                            target.setFoodLevel(20);
                            String message = LifeMod.getInstance().getConfig().getString("mod-feed");

                            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + message.replace("%target%", target.getName())));
                            target.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("player-feed")));

                        } else {
                            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                        }

                    } else {
                        sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("feed-usage")));
                    }

                } else {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("noperm")));
                }

                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }
}
