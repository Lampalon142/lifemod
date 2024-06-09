package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class GmCmd implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    Messages messages = (LifeMod.getInstance()).messages;
    Player player = (Player) sender;
    if(LifeMod.getInstance().isGamemodeActive()) {

      if (label.equalsIgnoreCase("gm") || label.equalsIgnoreCase("gamemode")) {
        if (!(sender instanceof Player)) {
          sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
        }
        if (!player.hasPermission("lifemod.gm")) {
          player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
        } else {

          if (args.length < 1 || args.length > 2) {
            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("gm-invalid")));
            return true;
          }

          Player targetPlayer;
          String targetPlayerName = null;

          if (args.length == 2) {
            targetPlayerName = args[1];
            targetPlayer = Bukkit.getPlayer(targetPlayerName);
            if (targetPlayer == null) {
              player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
              return true;
            }
          } else {
            if (!(player instanceof Player)) {
              player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
              return true;
            }
            targetPlayer = (Player) sender;
          }

          GameMode gameMode;
          String modeArg = args[0].toLowerCase();

          if (modeArg.matches("\\d+")) {
            int modeNum = Integer.parseInt(modeArg);
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
                player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("gm-invalid")));
                return true;
            }
          } else {
            switch (modeArg) {
              case "s":
              case "survival":
                gameMode = GameMode.SURVIVAL;
                break;
              case "c":
              case "creative":
                gameMode = GameMode.CREATIVE;
                break;
              case "a":
              case "adventure":
                gameMode = GameMode.ADVENTURE;
                break;
              case "sp":
              case "spectator":
                gameMode = GameMode.SPECTATOR;
                break;
              default:
                player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("gm-invalid")));
                return true;
            }
          }

          if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
            DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.title"))
                    .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.description").replace("%player%", sender.getName()))
                    .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.footer.logo").replace("%player%", sender.getName()))
                    .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.color")))));
            try {
              webhook.execute();
            } catch(IOException e) {
              LifeMod.getInstance().getLogger().severe(e.getStackTrace().toString());
            }
          }
          targetPlayer.setGameMode(gameMode);
          if (targetPlayerName != null) {
            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("gm-other").replace("%gamemode%", gameMode.name()).replace("%player%", targetPlayer.getName())));
          } else {
            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("gm-own").replace("%gamemode%", gameMode.name())));
          }
          return true;
        }
      }
    } else {
      sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
    }
    return false;
  }
}