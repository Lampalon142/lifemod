package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.managers.PlayerManager;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ModCmd implements CommandExecutor {
    private final LifeMod plugin;
    private final DebugManager debug;

    public ModCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            debug.log("mod", "Console tried to use /mod");
            return false;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("mod") || label.equalsIgnoreCase("staff")) {

            if (!player.hasPermission("lifemod.mod")) {
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
                debug.log("mod", "Permission denied for /mod by " + player.getName());
                return false;
            }

            if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
                try {
                    DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(plugin.getConfigConfig().getString("discord.mod.title"))
                            .setDescription(plugin.getConfigConfig().getString("discord.mod.description").replace("%player%", sender.getName()))
                            .setFooter(plugin.getConfigConfig().getString("discord.mod.footer.title"),
                                    plugin.getConfigConfig().getString("discord.mod.footer.logo").replace("%player%", sender.getName()))
                            .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.mod.color")))));
                    webhook.execute();
                    debug.log("mod", player.getName() + " toggled mod mode (Discord notified)");
                } catch (IOException e) {
                    debug.userError(sender, "Failed to send Discord mod alert", e);
                    debug.log("discord", "Webhook error: " + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                debug.log("mod", player.getName() + " toggled mod mode");
            }

            if (PlayerManager.isInModerationMod(player)) {
                PlayerManager.getFromPlayer(player).destroy();
                debug.log("mod", player.getName() + " disabled moderation mode");
            } else {
                (new PlayerManager(player)).init();
                debug.log("mod", player.getName() + " enabled moderation mode");
            }
        }
        return false;
    }
}
