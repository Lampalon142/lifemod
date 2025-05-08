package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FlyCmd implements CommandExecutor, TabCompleter {
  private final LifeMod plugin;
  private final DebugManager debug;

  public FlyCmd(LifeMod plugin) {
    this.plugin = plugin;
    this.debug = plugin.getDebugManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!label.equalsIgnoreCase("fly")) return false;

    if (!(sender instanceof Player)) {
      sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
      return true;
    }

    Player player = (Player) sender;

    if (!player.hasPermission("lifemod.fly")) {
      player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
      debug.log("commands", "Permission denied for /fly by " + player.getName());
      return true;
    }

    if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
      try {
        DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(plugin.getConfigConfig().getString("discord.fly.title"))
                .setDescription(plugin.getConfigConfig().getString("discord.fly.description")
                        .replace("%player%", sender.getName()))
                .setFooter(
                        plugin.getConfigConfig().getString("discord.fly.footer.title"),
                        plugin.getConfigConfig().getString("discord.fly.footer.logo")
                                .replace("%player%", sender.getName())
                )
                .setColor(Color.decode(Objects.requireNonNull(
                        plugin.getConfigConfig().getString("discord.fly.color")
                ))));
        webhook.execute();
      } catch (IOException e) {
        debug.userError(sender, "Failed to send Discord fly alert", e);
        debug.log("discord", "Webhook error: " + e.getMessage());
      }
    }

    if (player.getAllowFlight()) {
      player.setAllowFlight(false);
      player.setFlying(false);
      player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("fly.disable")));
    } else {
      player.setAllowFlight(true);
      player.setFlying(true);
      player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("fly.enable")));
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
    if (!cmd.getName().equalsIgnoreCase("fly")) return null;
    if (args.length == 1) {
      String input = args[0].toLowerCase();
      return Bukkit.getOnlinePlayers().stream()
              .map(Player::getName)
              .filter(name -> name.toLowerCase().startsWith(input))
              .collect(Collectors.toList());
    }
    return null;
  }
}