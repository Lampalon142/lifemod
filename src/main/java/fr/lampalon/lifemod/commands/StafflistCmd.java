package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.VanishedManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class StafflistCmd implements CommandExecutor {
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("stafflist")) return false;

        if (sender instanceof Player) {
            Player player = (Player) sender;

            StringBuilder modList = new StringBuilder(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("modlist.online")));

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("lifemod.stafflist") && !VanishedManager.isVanished(onlinePlayer)) {
                    modList.append(onlinePlayer.getName()).append(", ");
                }
            }

            if (modList.length() > LifeMod.getInstance().getLangConfig().getString("modlist.online").length()) {
                modList.delete(modList.length() - 2, modList.length());
            } else {
                modList.append(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("modlist.none")));
            }

            if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                try {
                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.title"))
                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.description").replace("%player%", sender.getName()))
                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.footer.title"),
                                    LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.footer.logo").replace("%player%", sender.getName()))
                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.stafflist.color")))));
                    webhook.execute();
                    debug.log("stafflist", player.getName() + " viewed stafflist (Discord notified)");
                } catch (IOException e) {
                    debug.userError(sender, "Failed to send Discord stafflist alert", e);
                    debug.log("discord", "Webhook error: " + e.getMessage());
                }
            } else {
                debug.log("stafflist", player.getName() + " viewed stafflist");
            }

            player.sendMessage(modList.toString());
            return true;
        } else {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            debug.log("stafflist", "Console tried to use /stafflist");
            return true;
        }
    }
}
