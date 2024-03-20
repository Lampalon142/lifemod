package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatclearCmd implements CommandExecutor {
    Messages messages;

    public ChatclearCmd(Messages messages){
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("lifemod.chatclear")){
                for (int i = 0; i < 100; i++) {
                    player.sendMessage("");
                }

                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.chatclear));
            } else {
                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
        }

        return true;
    }
}
