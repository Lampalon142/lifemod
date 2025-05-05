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
import java.util.stream.Collectors;

public class GmCmd implements CommandExecutor, TabCompleter {

  private final boolean useLuckPerms;
  private LuckPerms luckPerms;

  public GmCmd() {

    this.useLuckPerms = LifeMod.getInstance().getConfigConfig().getBoolean("UseLuckPerms");

    if(this.useLuckPerms) {
      this.luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class) != null
              ? LuckPermsProvider.get()
              : null;

      if(this.luckPerms == null) {
        Bukkit.getLogger().warning("[LifeMod] LuckPerms is configured but not detected !");
      }
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (label.equalsIgnoreCase("gm") || label.equalsIgnoreCase("gamemode")) {
      if (!sender.hasPermission("lifemod.gm")) {
        sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
        return true;
      }

      if (args.length < 1 || args.length > 2) {
        sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("gamemode.invalid")));
        return true;
      }

      Player targetPlayer;
      String targetPlayerName = null;

      if (args.length == 2) {
        targetPlayerName = args[1];
        targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
          sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
          return true;
        }
      } else {
        if (!(sender instanceof Player)) {
          sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
          return true;
        }
        targetPlayer = (Player) sender;
      }

      GameMode gameMode = parseGameMode(args[0]);
      if (gameMode == null) {
        sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("gamemode.invalid")));
        return true;
      }

      sendDiscordWebhook(sender);

      String playerPrefix = getPlayerPrefix(targetPlayer);

      targetPlayer.setGameMode(gameMode);
      String message = targetPlayerName != null
              ? LifeMod.getInstance().getLangConfig().getString("gamemode.other")
              : LifeMod.getInstance().getLangConfig().getString("gamemode.own");
      
      sender.sendMessage(MessageUtil.formatMessage(
              message.replace("%gamemode%", gameMode.name())
                      .replace("%player%", targetPlayer.getName())
                      .replace("%luckperms_prefix%", playerPrefix)));
      return true;
    }
    return false;
  }

  private GameMode parseGameMode(String modeArg) {
    modeArg = modeArg.toLowerCase();
    if (modeArg.matches("\\d+")) {
      switch (Integer.parseInt(modeArg)) {
        case 0: return GameMode.SURVIVAL;
        case 1: return GameMode.CREATIVE;
        case 2: return GameMode.ADVENTURE;
        case 3: return GameMode.SPECTATOR;
        default: return null;
      }
    } else {
      switch (modeArg) {
        case "s": case "survival": return GameMode.SURVIVAL;
        case "c": case "creative": return GameMode.CREATIVE;
        case "a": case "adventure": return GameMode.ADVENTURE;
        case "sp": case "spectator": return GameMode.SPECTATOR;
        default: return null;
      }
    }
  }

  private void sendDiscordWebhook(CommandSender sender) {
    if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
      DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
      webhook.addEmbed(new DiscordWebhook.EmbedObject()
              .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.title"))
              .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.description").replace("%player%", sender.getName()))
              .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.footer.logo").replace("%player%", sender.getName()))
              .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.gamemode.color")))));
      try {
        webhook.execute();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private String getPlayerPrefix(Player player) {
    if(this.useLuckPerms && this.luckPerms != null) {
      User user = luckPerms.getUserManager().getUser(player.getUniqueId());
      if (user != null) {
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
      }
    }
    return "";
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
    List<String> completions = new ArrayList<>();
    if (cmd.getName().equalsIgnoreCase("gamemode") || cmd.getName().equalsIgnoreCase("gm")) {
      if (args.length == 1) {
        return Arrays.asList("survival", "creative", "adventure", "spectator", "s", "c", "a", "sp", "0", "1", "2", "3");
      } else if (args.length == 2) {
        String input = args[args.length - 1].toLowerCase();
        completions = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input))
                .collect(Collectors.toList());

        return completions;
      }
    }
    return Collections.emptyList();
  }
}