package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.VanishedManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class VanishCmd implements CommandExecutor {
    private final VanishedManager playerManager;
    private BukkitRunnable actionBarTask;

    public VanishCmd(VanishedManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isVanishActive()) {
            if (command.getName().equalsIgnoreCase("vanish")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                    return true;
                }

                Player player = (Player) sender;

                if (player.hasPermission("lifemod.vanish")) {
                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.vanish.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.color")))));

                        try {
                            webhook.execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (args.length == 0) {
                        boolean isVanished = VanishedManager.isVanished(player);
                        VanishedManager.setVanished(!isVanished, player);
                        if (!isVanished) {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishon")));
                        } else {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishoff")));
                        }
                    } else if (args.length == 1) {
                        Player targetPlayer = Bukkit.getPlayer(args[0]);

                        if (targetPlayer != null) {
                            boolean isVanished = VanishedManager.isVanished(targetPlayer);
                            VanishedManager.setVanished(!isVanished, targetPlayer);
                            if (!isVanished) {
                                targetPlayer.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishon")));
                            } else {
                                targetPlayer.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishoff")));
                            }
                        } else {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                        }
                    } else {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("vanishusage")));
                    }
                } else {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                }

                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.commanddisable));
        }
        return false;
    }
}
