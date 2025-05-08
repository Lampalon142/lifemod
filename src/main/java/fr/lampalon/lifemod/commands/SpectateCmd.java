package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
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
    private final DebugManager debug;

    public SpectateCmd(LifeMod plugin) {
        this.spectateManager = new SpectateManager();
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            debug.log("spectate", "Console tried to use /spectate");
            return true;
        }

        if (!sender.hasPermission("lifemod.spectate")) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
            debug.log("spectate", "Permission denied for /spectate by " + sender.getName());
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.usage")));
            debug.log("spectate", "Invalid usage by " + sender.getName());
            return true;
        }

        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "leave":
                if (spectateManager.isSpectating(player)) {
                    spectateManager.endSpectate(player, true);
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.leave.success")));
                    debug.log("spectate", player.getName() + " left spectate mode");
                    sendDiscord(player, "leave");
                } else {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.leave.error")));
                }
                break;

            case "fp":
                if (spectateManager.isSpectating(player)) {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.already-spectate")));
                } else {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.fp-spectate")));
                    debug.log("spectate", player.getName() + " entered free spectate mode");
                    sendDiscord(player, "fp");
                }
                break;

            default:
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.player-not-found").replace("%target%", args[0])));
                    debug.log("spectate", "Target not found: " + args[0]);
                    return true;
                }
                if (spectateManager.isSpectating(player)) {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.already-spectate")));
                    return true;
                }

                spectateManager.startSpectate(player, target);
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("spectate.spectate").replace("%target%", target.getName())));
                debug.log("spectate", player.getName() + " started spectating " + target.getName());
                sendDiscord(player, target.getName());
                break;
        }

        return true;
    }

    private void sendDiscord(Player player, String context) {
        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.title"))
                        .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.description").replace("%player%", player.getName()))
                        .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.footer.title"),
                                LifeMod.getInstance().getConfigConfig().getString("discord.spectate.footer.logo").replace("%player%", player.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.spectate.color")))));
                webhook.execute();
                debug.log("spectate", player.getName() + " used /spectate (" + context + ") (Discord notified)");
            } catch (IOException e) {
                debug.userError(player, "Failed to send Discord spectate alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            }
        }
    }
}
