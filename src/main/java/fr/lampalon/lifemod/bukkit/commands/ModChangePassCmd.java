package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ModChangePassCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.moderator")) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.usage-modchangepass")));
            return true;
        }

        String oldPass = args[0];
        String newPass = args[1];

        if (!checkPassword(player.getUniqueId(), oldPass)) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.changepass-failed")));
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
            LifeMod.getInstance().getDebugManager().log("auth", player.getName() + " was executed auth command (changepass) ");
        }

        changePassword(player.getUniqueId(), newPass);
        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("moderator-login.changepass-success")));
        return true;
    }

    private boolean checkPassword(java.util.UUID uuid, String password) {
        return LifeMod.getInstance().getModeratorAuthService().checkPassword(uuid, password);
    }

    private void changePassword(java.util.UUID uuid, String newPassword) {
        LifeMod.getInstance().getModeratorAuthService().changePassword(uuid, newPassword);
    }
}
