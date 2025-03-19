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

public class InvseeCmd implements CommandExecutor, TabCompleter {
    LifeMod plugin;
    public InvseeCmd(LifeMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("invsee")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                    return true;
                }

                if (!sender.hasPermission("lifemod.invsee")) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    return true;
                }

                if (args.length != 1) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("invsee.usage")));
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                    return true;
                }

                Player player = (Player) sender;

                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.invsee.title"))
                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.invsee.description").replace("%player%", sender.getName()))
                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.invsee.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.invsee.footer.logo").replace("%player%", sender.getName()))
                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.invsee.color")))));
                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                player.openInventory(targetPlayer.getInventory());
                return true;
        }

        return false;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("invsee")){
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