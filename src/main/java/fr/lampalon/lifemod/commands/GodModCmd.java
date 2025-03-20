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

public class GodModCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        Player targetPlayer;
        if (args.length > 0) {
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                return true;
            }
        } else {
            targetPlayer = (Player) sender;
        }

        if (!sender.hasPermission("lifemod.god")) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
            return true;
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
            DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.god.title"))
                    .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.god.description").replace("%player%", sender.getName()))
                    .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.god.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.god.footer.logo").replace("%player%", sender.getName()))
                    .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.god.color")))));
            try {
                webhook.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (targetPlayer.isInvulnerable()) {
            targetPlayer.setInvulnerable(false);
            targetPlayer.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("god.deactivate.own")));
            if (!targetPlayer.equals(sender)) {
                sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("god.deactivate.other").replace("%target%", targetPlayer.getName())));
            }
        } else {
            targetPlayer.setInvulnerable(true);
            targetPlayer.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("god.activate.own")));
            if (!targetPlayer.equals(sender)) {
                sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("god.activate.other").replace("%target%", targetPlayer.getName())));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("god")){
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