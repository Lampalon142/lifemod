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

public class ClearinvCmd implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (label.equalsIgnoreCase("clearinv")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    if (player.hasPermission("lifemod.clearinv")) {
                        if (args.length == 1) {
                            Player targetPlayer = Bukkit.getPlayer(args[0]);

                            if (targetPlayer != null) {
                                targetPlayer.getInventory().clear();
                                player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("clearinv.message").replace("%target%", targetPlayer.getPlayer().getName())));
                                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.title"))
                                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.description").replace("%player%", sender.getName()))
                                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.footer.logo").replace("%player%", sender.getName()))
                                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.color")))));
                                    try {
                                        webhook.execute();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                            }
                        } else {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("clearinv.usage")));
                        }
                    } else {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    }
                } else {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                }
            }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("clearinv")){
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