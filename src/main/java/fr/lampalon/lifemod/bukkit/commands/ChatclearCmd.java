package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.manager.DebugManager;
import fr.lampalon.lifemod.bukkit.manager.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ChatclearCmd implements CommandExecutor {
    private final LifeMod plugin;
    private final DebugManager debug;

    public ChatclearCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lifemod.chatclear")) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("commands", "Permission denied for /chatclear by " + sender.getName());
            return true;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                player.sendMessage("");
            }
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("chatclear.message")));
        }
        debug.log("chatclear", "Chat cleared by " + sender.getName());

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.chatclear.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.chatclear.description")
                                .replace("%player%", sender.getName()))
                        .setFooter(
                                plugin.getConfigConfig().getString("discord.chatclear.footer.title"),
                                plugin.getConfigConfig().getString("discord.chatclear.footer.logo")
                                        .replace("%player%", sender.getName())
                        )
                        .setColor(Color.decode(Objects.requireNonNull(
                                plugin.getConfigConfig().getString("discord.chatclear.color")
                        ))));
                webhook.execute();
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord chatclear alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            }
        }

        return true;
    }
}