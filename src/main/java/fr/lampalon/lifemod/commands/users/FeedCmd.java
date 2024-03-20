package fr.lampalon.lifemod.commands.users;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FeedCmd implements CommandExecutor {
    private final Messages messages;

    public FeedCmd(Messages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("feed")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.noconsole);
                return false;
            }

            Player player = (Player) sender;

            if (player.hasPermission("lifemod.feed")) {

                if (args.length == 0) {
                    player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.yourselffeed));
                    player.setFoodLevel(20);

                } else if (args.length == 1) {

                    Player target = Bukkit.getPlayer(args[0]);

                    if (target != null) {

                        target.setFoodLevel(20);
                        String message = LifeMod.getInstance().getConfig().getString("mod-feed");

                        sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + message.replace("%target%", target.getName())));
                        target.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.playerfeed));

                    } else {
                        sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.offlineplayer));
                    }

                } else {
                    sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.feedusage));
                }

            } else {
                player.sendMessage(MessageUtil.parseColors(messages.noperm));
            }

            return true;
        }
        return false;
    }
}
