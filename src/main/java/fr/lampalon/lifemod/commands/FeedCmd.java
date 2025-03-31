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

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FeedCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (label.equalsIgnoreCase("feed")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer"));
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
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (args.length == 0) {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("feed.yourself")));
                        player.setFoodLevel(20);

                    } else if (args.length == 1) {

                        Player target = Bukkit.getPlayer(args[0]);

                        if (target != null) {

                            target.setFoodLevel(20);
                            String message = LifeMod.getInstance().getLangConfig().getString("feed.mod");

                            sender.sendMessage(MessageUtil.formatMessage(message.replace("%target%", target.getName())));
                            target.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("feed.player")));

                        } else {
                            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                        }

                    } else {
                        sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("feed.usage")));
                    }

                } else {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                }

                return true;
            }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (cmd.getName().equalsIgnoreCase("feed")){
            if (args.length == 1) {
                String input = args[args.length - 1].toLowerCase();
                completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());

                return completions;
            }
        }
        return null;
    }
}
