package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class GmCmd implements CommandExecutor{
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    Messages messages = LifeMod.getInstance().messages;
    if (label.equalsIgnoreCase("gm")) {
      if (!(sender instanceof Player)) {
        sender.sendMessage(messages.noconsole);
        return true;
      }

      Player player = (Player) sender;

      if (!player.hasPermission("lifemod.gm")) {
        player.sendMessage(messages.prefixGeneral + messages.noperm);
        return true;
      }

      if (args.length < 2) {
        player.sendMessage(messages.prefixGeneral + messages.gmusage);
        return true;
      }

      String targetPlayerName = args[args.length - 1];
      Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

      if (targetPlayer == null) {
        player.sendMessage(messages.prefixGeneral + messages.offlineplayer);
        return true;
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
            player.sendMessage(messages.prefixGeneral + messages.gminvalid);
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
            player.sendMessage(messages.prefixGeneral + messages.gminvalid);
            return true;
        }
      }

      targetPlayer.setGameMode(gameMode);
      player.sendMessage(messages.prefixGeneral + messages.gmsucces + gameMode.toString() + " §7for §c" + targetPlayer.getName() + ".");
      return true;
    } else if (label.equalsIgnoreCase("gamemode")) {
      if (!(sender instanceof Player)) {
        sender.sendMessage(messages.noconsole);
        return true;
      }

      Player player = (Player) sender;

      if (!player.hasPermission("lifemod.mod.gm.use")) {
        player.sendMessage(messages.prefixGeneral + messages.noperm);
        return true;
      }

      if (args.length < 2) {
        player.sendMessage(messages.prefixGeneral + messages.gmusage);
        return true;
      }

      String targetPlayerName = args[args.length - 1];
      Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

      if (targetPlayer == null) {
        player.sendMessage(messages.prefixGeneral + messages.offlineplayer);
        return true;
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
            player.sendMessage(messages.prefixGeneral + messages.gminvalid);
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
            player.sendMessage(messages.prefixGeneral + messages.gminvalid);
            return true;
        }
      }

      targetPlayer.setGameMode(gameMode);
      player.sendMessage(messages.prefixGeneral + messages.gmsucces + gameMode.toString() + " §7for §c" + targetPlayer.getName() + ".");
      return true;
    }
    return false;
  }
}