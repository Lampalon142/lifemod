package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Chatclear implements CommandExecutor {
    Messages messages;

    public Chatclear(Messages messages){
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

                player.sendMessage(messages.prefixGeneral + messages.chatclear);
            } else {
                player.sendMessage(messages.prefixGeneral + messages.noperm);
                return true;
            }
        } else {
            sender.sendMessage(messages.noconsole);
        }

        return true;
    }
}
