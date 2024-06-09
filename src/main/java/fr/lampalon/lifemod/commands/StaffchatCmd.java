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

public class StaffchatCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isStaffchatActive()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                return true;
            }

            Player player = (Player) sender;

            String playermsg = LifeMod.getInstance().getConfig().getString("staffmsg");

            if (cmd.getName().equalsIgnoreCase("staffchat")) {
                if (player.hasPermission("lifemod.staffchat")) {
                    if (args.length == 0) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("staffusage")));
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
                        } catch(IOException e) {
                            LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                        }
                    }

                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("staffsuccesmsg")));
                    return true;
                } else {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                    return true;
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.commanddisable));
        }
        return false;
    }
}
