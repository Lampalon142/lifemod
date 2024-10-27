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

public class StaffchatCmd implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (LifeMod.getInstance().isStaffchatActive()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                return true;
            }

            Player player = (Player) sender;

            String playermsg = LifeMod.getInstance().getLangConfig().getString("staffchat.message");

            if (cmd.getName().equalsIgnoreCase("staffchat")) {
                if (player.hasPermission("lifemod.staffchat")) {
                    if (args.length == 0) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("staffchat.usage")));
                        return true;
                    }
                    StringBuilder message = new StringBuilder();
                    for (String arg : args) {
                        message.append(arg).append(" ");
                    }
                    for (Player staff : Bukkit.getOnlinePlayers()) {
                        if (staff.hasPermission("lifemod.staffchat")) {
                            staff.sendMessage(MessageUtil.parseColors(playermsg.replace("%player%", player.getPlayer().getName()) + ": " + message.toString()));
                        }
                    }

                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.color")))));
                        try {
                            webhook.execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("staffchat.success")));
                    return true;
                } else {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    return true;
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (sender instanceof Player && cmd.getName().equalsIgnoreCase("staffchat")){
            if (args.length == 1){
                completions.add("<message>");
            }
        }
        return completions;
    }
}
