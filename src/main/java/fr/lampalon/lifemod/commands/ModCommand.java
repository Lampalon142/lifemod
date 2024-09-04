package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.PlayerManager;

import fr.lampalon.lifemod.utils.MessageUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ModCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (LifeMod.getInstance().isModActive()) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                return false;
            }

            Player player = (Player) sender;

            if (label.equalsIgnoreCase("mod") || label.equalsIgnoreCase("staff")) {

                if (!player.hasPermission("lifemod.mod")) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    return false;
                }

                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.mod.title"))
                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.mod.description").replace("%player%", sender.getName()))
                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.mod.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.mod.footer.logo").replace("%player%", sender.getName()))
                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.mod.color")))));
                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (PlayerManager.isInModerationMod(player)) {
                    PlayerManager.getFromPlayer(player).destroy();
                } else {
                    (new PlayerManager(player)).init();
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }
}