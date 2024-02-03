package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class GmCmd implements CommandExecutor {
  private final Messages messages;
  public GmCmd(Messages messages){
    this.messages = messages;
  }
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if ((label.equalsIgnoreCase("gm") || label.equalsIgnoreCase("gamemode")) && args.length >= 1) {
      if (!(sender instanceof Player)) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noconsole));
        return true;
      }

      Player player = (Player) sender;

      if (args.length == 0) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.gminvalid));
        System.out.println("Debug: Invalid usage of gamemode command detected.");
        return true;
      }

      String targetPlayerName = args.length > 1 ? args[args.length - 1] : null;

      if (!player.hasPermission("lifemod.gm") && (targetPlayerName == null || !targetPlayerName.equalsIgnoreCase(player.getName()))) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.noperm));
        return true;
      }

      Player targetPlayer;

      if (targetPlayerName == null || targetPlayerName.equalsIgnoreCase(player.getName())) {
        targetPlayer = player;
      } else {
        targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.offlineplayer));
          return true;
        }
      }

      GameMode gameMode;

      if (args[0].matches("\\d+")) {
        int modeNum = Integer.parseInt(args[0]);
        switch (modeNum) {
          case 0:
            gameMode = GameMode.SURVIVAL;
            break;
          case 1:
            gameMode = GameMode.CREATIVE;
            break;
          case 2:
            gameMode = GameMode.ADVENTURE;
            break;
          case 3:
            gameMode = GameMode.SPECTATOR;
            break;
          default:
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.gminvalid));
            return true;
        }
      } else {
        String modeName = args[0].toLowerCase();
        switch (modeName) {
          case "survival":
            gameMode = GameMode.SURVIVAL;
            break;
          case "creative":
            gameMode = GameMode.CREATIVE;
            break;
          case "adventure":
            gameMode = GameMode.ADVENTURE;
            break;
          case "spectator":
            gameMode = GameMode.SPECTATOR;
            break;
          default:
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.gminvalid));
            return true;
        }
      }

      String gmyes = LifeMod.getInstance().getConfig().getString("gm-success");
      targetPlayer.setGameMode(gameMode);
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + gmyes.replace("%gamemode%", gameMode.name()).replace("%player%", targetPlayer.getName())));
      return true;
    }
    return false;
  }
}