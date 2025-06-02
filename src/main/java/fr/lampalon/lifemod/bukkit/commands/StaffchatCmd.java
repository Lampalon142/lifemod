package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.manager.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
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

public class StaffchatCmd implements CommandExecutor, TabCompleter {
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            debug.log("staffchat", "Console tried to use /staffchat");
            return true;
        }

        Player player = (Player) sender;
        String playermsg = LifeMod.getInstance().getLangConfig().getString("staffchat.message");

        if (cmd.getName().equalsIgnoreCase("staffchat")) {
            if (player.hasPermission("lifemod.staffchat")) {
                if (args.length == 0) {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("staffchat.usage")));
                    debug.log("staffchat", "No message provided by " + player.getName());
                    return true;
                }
                StringBuilder message = new StringBuilder();
                for (String arg : args) {
                    message.append(arg).append(" ");
                }
                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission("lifemod.staffchat")) {
                        staff.sendMessage(MessageUtil.formatMessage(playermsg.replace("%player%", player.getPlayer().getName()) + ": " + message.toString()));
                    }
                }

                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                    try {
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.footer.title"),
                                        LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.staffchat.color")))));
                        webhook.execute();
                        debug.log("staffchat", player.getName() + " sent staffchat message (Discord notified)");
                    } catch (IOException e) {
                        debug.userError(sender, "Failed to send Discord staffchat alert", e);
                        debug.log("discord", "Webhook error: " + e.getMessage());
                    }
                } else {
                    debug.log("staffchat", player.getName() + " sent staffchat message");
                }

                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("staffchat.success")));
                return true;
            } else {
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                debug.log("staffchat", "Permission denied for /staffchat by " + player.getName());
                return true;
            }
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
