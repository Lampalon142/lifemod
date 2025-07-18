package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class ModResetCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lifemod.admin")) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.usage-modreset")));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUUID = (target != null) ? target.getUniqueId() : getUUIDByName(targetName);

        if (targetUUID == null || !isRegistered(targetUUID)) {
            String msg = LifeMod.getInstance().getLangConfig()
                    .getString("moderator-login.reset-failed")
                    .replace("%player%", targetName);
            sender.sendMessage(MessageUtil.formatMessage(msg));
            return true;
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.auth.title"))
                        .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.auth.description").replace("%player%", sender.getName()))
                        .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.auth.footer.title"),
                                LifeMod.getInstance().getConfigConfig().getString("discord.auth.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.auth.color")))));
                webhook.execute();
            } catch (IOException e) {
                LifeMod.getInstance().getDebugManager().userError(sender, "Failed to send Discord Auth alert", e);
                LifeMod.getInstance().getDebugManager().log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            LifeMod.getInstance().getDebugManager().log("auth", sender.getName() + "was executed auth command (reset) " + "§7(" + target.getName() + "§7)");
        }

        resetModeratorPassword(targetUUID);

        String msg = LifeMod.getInstance().getLangConfig()
                .getString("moderator-login.reset-success")
                .replace("%player%", targetName);
        sender.sendMessage(MessageUtil.formatMessage(msg));
        return true;
    }

    private boolean isRegistered(UUID uuid) {
        return LifeMod.getInstance().getModeratorAuthService().isRegistered(uuid);
    }

    private void resetModeratorPassword(UUID uuid) {
        LifeMod.getInstance().getModeratorAuthService().resetModeratorPassword(uuid);
    }

    private UUID getUUIDByName(String name) {
        return LifeMod.getInstance().getModeratorAuthService().getUUIDByName(name);
    }
}
