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
import java.util.List;
import java.util.Objects;

public class HealCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (label.equalsIgnoreCase("heal")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                    return true;
                }

                Player player = (Player) sender;

                String s = LifeMod.getInstance().getLangConfig().getString("heaL.mod");

                if (!player.hasPermission("lifemod.heal")) {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
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
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (args.length == 0) {
                    double maxHealth = player.getMaxHealth();
                    player.setHealth(maxHealth);
                    player.setFoodLevel(player.getFoodLevel());
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("heal.player")));
                } else if (args.length == 1) {
                    Player target = player.getServer().getPlayer(args[0]);
                    if (target != null) {
                        double maxHealth = player.getMaxHealth();
                        target.setHealth(maxHealth);
                        target.setFoodLevel(player.getFoodLevel());
                        player.sendMessage(MessageUtil.formatMessage(s.replace("%player%", player.getPlayer().getName())));
                    } else {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                    }
                } else {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("heal.usage")));
                }
            }
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("heal")){
            if (args.length == 1){
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()){
                    playerNames.add(player.getName());
                }
                return playerNames;
            }
        }
        return null;
    }
}
