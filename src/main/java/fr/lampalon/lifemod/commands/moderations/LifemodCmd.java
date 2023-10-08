package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LifemodCmd implements CommandExecutor {
    private final LifeMod plugin;
    Messages messages;
    public LifemodCmd(LifeMod plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.lifemodusage);
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage(messages.help);
                player.sendMessage(messages.helpone);
                player.sendMessage(messages.helptwo);
                player.sendMessage(messages.helpthree);
                player.sendMessage(messages.helpfour);
                player.sendMessage(messages.helpsix);
                player.sendMessage(messages.helpseven);
                player.sendMessage(messages.helpeight);
                player.sendMessage(messages.helpnine);
                player.sendMessage(messages.helpten);
                player.sendMessage(messages.helpeleven);
                player.sendMessage(messages.helptwelve);
                player.sendMessage(messages.helpthirteen);
                player.sendMessage(messages.helpfourteen);
            } else {
                sender.sendMessage(messages.noconsole);
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            Player player = (Player) sender;
            if(player.hasPermission("lifemod.reload")){
                player.sendMessage(messages.prefixGeneral + messages.configreload);
                LifeMod.getInstance().reloadConfig();
            }
        }

        return true;
    }
}
