package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class HealCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isHealActive()) {
            if (label.equalsIgnoreCase("heal")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                    return true;
                }

                Player player = (Player) sender;

                String s = LifeMod.getInstance().getConfig().getString("healmodmsg");

                if (!player.hasPermission("lifemod.heal")) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                    return true;
                }

                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.heal.title"))
                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.heal.description").replace("%player%", sender.getName()))
                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.heal.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.heal.footer.logo").replace("%player%", sender.getName()))
                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.heal.color")))));
                    try {
                        webhook.execute();
                    } catch(IOException e) {
                        LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                    }
                }

                if (args.length == 0) {
                    double maxHealth = player.getMaxHealth();
                    player.setHealth(maxHealth);
                    player.setFoodLevel(player.getFoodLevel());
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("healmsgplayer")));
                } else if (args.length == 1) {
                    Player target = player.getServer().getPlayer(args[0]);
                    if (target != null) {
                        double maxHealth = player.getMaxHealth();
                        target.setHealth(maxHealth);
                        target.setFoodLevel(player.getFoodLevel());
                        player.sendMessage(MessageUtil.parseColors(s.replace("%player%", player.getPlayer().getName())));
                    } else {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                    }
                } else {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("healusage")));
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return true;
    }
}
