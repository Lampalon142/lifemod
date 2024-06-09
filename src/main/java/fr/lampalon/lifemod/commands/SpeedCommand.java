package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class SpeedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages msg = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isSpeedActive()) {
            if (label.equalsIgnoreCase("speed")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                    return false;
                }

                Player player = (Player) sender;
                if (!player.hasPermission("speed.use")) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                } else {
                    if (args.length == 0) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("speed.provide")));
                        return false;
                    }
                    int speed;
                    try {
                        speed = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("speed.provide")));
                        return false;
                    }

                    if (speed < 1 || speed > 10) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("speed.provide")));
                        return false;
                    }

                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.speed.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.speed.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.speed.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.speed.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.speed.color")))));
                        try {
                            webhook.execute();
                        } catch(IOException e) {
                            LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                        }
                    }

                    if (player.isFlying()) {
                        player.setFlySpeed((float) speed / 10);
                    } else {
                        player.setWalkSpeed((float) speed / 10);
                    }

                    String s4 = LifeMod.getInstance().getConfig().getString("speed.success");
                    assert s4 != null;
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + s4.replace("%speed%", String.valueOf(speed))));
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(msg.prefixGeneral + msg.commanddisable));
        }
        return false;
    }
}
