package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GmCmd implements CommandExecutor, TabCompleter {

  private LuckPerms luckPerms;

  public GmCmd(){
    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null){
      luckPerms = LuckPermsProvider.get();
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

      if (label.equalsIgnoreCase("gm") || label.equalsIgnoreCase("gamemode")) {
        if (!(sender instanceof Player)) {
          sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
        }

        Player player = (Player) sender;
        if (!player.hasPermission("lifemod.gm")) {
          player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
        } else {

          if (args.length < 1 || args.length > 2) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("gamemode.invalid")));
            return true;
          }

          Player targetPlayer;
          String targetPlayerName = null;

          if (args.length == 2) {
            targetPlayerName = args[1];
            targetPlayer = Bukkit.getPlayer(targetPlayerName);
            if (targetPlayer == null) {
              player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
              return true;
            }
          } else {
            if (!(player instanceof Player)) {
              player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
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
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("gamemode.invalid")));
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
                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("gamemode.invalid")));
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
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }

          String playerPrefix = "";
          if (luckPerms != null){
            User user = luckPerms.getUserManager().getUser(targetPlayer.getName());
            if (user != null){
              playerPrefix = user.getCachedData().getMetaData().getPrefix();
            }
            if (playerPrefix == null) playerPrefix = "";
          }

          targetPlayer.setGameMode(gameMode);
          if (targetPlayerName != null) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("gamemode.other").replace("%gamemode%", gameMode.name()).replace("%player%", targetPlayer.getName()).replace("%luckperms_prefix%", playerPrefix)));
          } else {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("gamemode.own").replace("%gamemode%", gameMode.name()).replace("%luckperms_prefix%", playerPrefix)));
          }
          return true;
        }
      }
    return false;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
    if (cmd.getName().equalsIgnoreCase("gamemode") || cmd.getName().equalsIgnoreCase("gm")) {
      if (args.length == 1) {
        return Arrays.asList("survival", "creative", "adventure", "spectator", "s", "c", "a", "sp", "0", "1", "2", "3");
      } else if (args.length == 2) {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
          playerNames.add(player.getName());
        }
        return playerNames;
      }
    }
    return null;
  }
}