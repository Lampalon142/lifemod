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

public class VanishCmd implements CommandExecutor {
    private final VanishedManager playerManager;
    private final DebugManager debug;

    public VanishCmd(VanishedManager playerManager){
        this.playerManager = playerManager;
        this.debug = LifeMod.getInstance().getDebugManager();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vanish")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                debug.log("vanish", "Console tried to use /vanish");
                return true;
            }

            Player player = (Player) sender;

            if (player.hasPermission("lifemod.vanish")) {
                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                    try {
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.footer.title"),
                                        LifeMod.getInstance().getConfigConfig().getString("discord.vanish.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.vanish.color")))));
                        webhook.execute();
                        debug.log("vanish", player.getName() + " toggled vanish (Discord notified)");
                    } catch (IOException e) {
                        debug.userError(sender, "Failed to send Discord vanish alert", e);
                        debug.log("discord", "Webhook error: " + e.getMessage());
                    }
                } else {
                    debug.log("vanish", player.getName() + " toggled vanish");
                }

                if (args.length == 0) {
                    boolean isVanished = VanishedManager.isVanished(player);
                    VanishedManager.setVanished(!isVanished, player);
                    if (!isVanished) {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("vanish.activate")));
                        debug.log("vanish", player.getName() + " is now vanished");
                    } else {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("vanish.deactivate")));
                        debug.log("vanish", player.getName() + " is now visible");
                    }
                } else if (args.length == 1) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    if (targetPlayer != null) {
                        boolean isVanished = VanishedManager.isVanished(targetPlayer);
                        VanishedManager.setVanished(!isVanished, targetPlayer);
                        if (!isVanished) {
                            targetPlayer.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("vanish.activate")));
                            debug.log("vanish", player.getName() + " vanished " + targetPlayer.getName());
                        } else {
                            targetPlayer.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("vanish.deactivate")));
                            debug.log("vanish", player.getName() + " unvanished " + targetPlayer.getName());
                        }
                    } else {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                        debug.log("vanish", player.getName() + " tried to vanish unknown player " + args[0]);
                    }
                } else {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("vanish.usage")));
                    debug.log("vanish", player.getName() + " used /vanish with wrong arguments");
                }
            } else {
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                debug.log("vanish", "Permission denied for /vanish by " + player.getName());
            }

            return true;
        }
        return false;
    }
}
