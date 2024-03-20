package fr.lampalon.lifemod.commands.users;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.ChatColor;
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
            sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("god")) {
            if (sender.hasPermission("lifemod.god")) {
                if (player.isInvulnerable()) {
                    player.setInvulnerable(false);
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.goddesactivate));
                } else {
                    player.setInvulnerable(true);
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.godactivate));
                }
            } else {
                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
            }
            return true;
        }

        return false;
    }
}
