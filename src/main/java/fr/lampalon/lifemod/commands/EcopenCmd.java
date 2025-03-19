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

public class EcopenCmd implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (label.equalsIgnoreCase("ecopen")) {
                if (sender instanceof Player) {

                    Player player = (Player) sender;

                    if (args.length != 1) {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("ec.usage")));
                        return true;
                    }

                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (!player.hasPermission("lifemod.ecopen")) {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
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
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        player.openInventory(targetPlayer.getEnderChest());
                    }

                    if (targetPlayer == null) {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                        return true;
                    }

                    if (targetPlayer == player) {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("ec.yourself")));
                        return true;
                    }

                } else {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                }
                return true;
            }
        return false;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ecopen")){
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
