package fr.lampalon.lifemod.commands.users;

import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodModCmd implements CommandExecutor {
    private Messages messages;
    public GodModCmd(Messages messages){
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.noconsole);
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("god")) {
            if (sender.hasPermission("lifemod.god")) {
                if (player.isInvulnerable()) {
                    player.setInvulnerable(false);
                    player.sendMessage(messages.prefixGeneral + messages.goddesactivate);
                } else {
                    player.setInvulnerable(true);
                    player.sendMessage(messages.prefixGeneral + messages.godactivate);
                }
            } else {
                player.sendMessage(messages.prefixGeneral + messages.noperm);
            }
            return true;
        }

        return false;
    }
}
