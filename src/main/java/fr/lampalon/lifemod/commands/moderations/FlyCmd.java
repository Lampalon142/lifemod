package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCmd implements CommandExecutor
{
  private LifeMod main;

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    Messages messages = LifeMod.getInstance().messages;
    if (label.equalsIgnoreCase("fly")) {
      if (!(sender instanceof Player)) {
        sender.sendMessage(MessageUtil.parseColors(messages.noconsole));
        return true;
      }

      Player player = (Player) sender;

      if (player.hasPermission("lifemod.fly")) {
        if (args.length == 0) {
          if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.flydisable));
          } else {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.flyenable));
          }
        }
      } else {
        player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.noperm));
      }

      return true;
    }
    return false;
  }
}