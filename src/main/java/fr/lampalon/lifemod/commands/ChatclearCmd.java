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

public class ChatclearCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (LifeMod.getInstance().isChatclearActive()) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (player.hasPermission("lifemod.chatclear")) {
                    for (int i = 0; i < 100; i++) {
                        player.sendMessage("");
                    }

                    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                        DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.chatclear.title"))
                                .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.chatclear.description").replace("%player%", sender.getName()))
                                .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.chatclear.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.chatclear.footer.logo").replace("%player%", sender.getName()))
                                .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.chatclear.color")))));
                        try {
                            webhook.execute();
                        } catch(IOException e) {
                            LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
                        }
                    }

                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.chatclear));
                } else {
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
                    return true;
                }
            } else {
                sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.commanddisable));
        }

        return true;
    }
}
