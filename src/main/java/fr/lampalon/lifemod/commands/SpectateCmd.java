package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.SpectateManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class SpectateCmd implements CommandExecutor {
    private final SpectateManager spectateManager;

    public SpectateCmd(LifeMod plugin) {
        this.spectateManager = new SpectateManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        if (!sender.hasPermission("lifemod.spectate")) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("spectate.usage")));
            return true;
        }

        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "leave":
                if (spectateManager.isSpectating(player)) {
                    spectateManager.endSpectate(player, true);
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("spectate.leave.success")));
                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.vanish.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.color")))));

                        try {
                            webhook.execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("spectate.leave.error")));
                }
                break;

            case "fp":
                if (spectateManager.isSpectating(player)) {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("spectate.already-spectate")));
                } else {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("spectate.fp-spectate")));
                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.vanish.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.color")))));

                        try {
                            webhook.execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                break;

            default:
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }
                if (spectateManager.isSpectating(player)) {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("spectate.already-spectate")));
                    return true;
                }

                spectateManager.startSpectate(player, target);
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("spectate.spectate").replace("%target%", target.getName())));
                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.title"))
                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.description").replace("%player%", sender.getName()))
                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.vanish.footer.logo").replace("%player%", sender.getName()))
                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.color")))));

                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
        }

        return true;
    }
}
