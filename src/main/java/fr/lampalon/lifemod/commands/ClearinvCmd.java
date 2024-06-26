package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class ClearinvCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isClearinvActive()) {
            if (label.equalsIgnoreCase("clearinv")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;

                    String target = LifeMod.getInstance().getConfig().getString("clearinvmsg");
                    if (player.hasPermission("lifemod.clearinv")) {
                        if (args.length == 1) {
                            Player targetPlayer = Bukkit.getPlayer(args[0]);

                            if (targetPlayer != null) {
                                targetPlayer.getInventory().clear();
                                player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + target.replace("%player%", targetPlayer.getPlayer().getName())));
                                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.title"))
                                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.description").replace("%player%", sender.getName()))
                                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.footer.logo").replace("%player%", sender.getName()))
                                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.clearinv.color")))));
                                    try {
                                        webhook.execute();
                                    } catch(IOException e) {
                                        LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                                    }
                                }
                            } else {
                                player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                            }
                        } else {
                            player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.clearinvusage));
                        }
                    } else {
                        player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
                    }
                } else {
                    sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.commanddisable));
        }
        return true;
    }
}
