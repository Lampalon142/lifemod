package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
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
    if (!cmd.getName().equalsIgnoreCase("fly")) return false;

    Player target;
    boolean isSelf = false;

    if (args.length == 0) {
      if (!(sender instanceof Player)) {
        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
        return true;
      }
      target = (Player) sender;
      isSelf = true;
      if (!sender.hasPermission("lifemod.fly")) {
        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
        debug.log("commands", "Permission denied for /fly by " + sender.getName());
        return true;
      }
    } else if (args.length == 1) {
      if (!sender.hasPermission("lifemod.fly.others")) {
        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
        debug.log("commands", "Permission denied for /fly others by " + sender.getName());
        return true;
      }
      target = Bukkit.getPlayer(args[0]);
      if (target == null) {
        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("fly.usage")));
        return true;
      }
    } else {
      sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("fly.usage")));
      return true;
    }

    if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
      try {
        DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(plugin.getConfigConfig().getString("discord.fly.title"))
                .setDescription(plugin.getConfigConfig().getString("discord.fly.description")
                        .replace("%player%", sender.getName())
                        .replace("%target%", target.getName()))
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
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    boolean newState = !target.getAllowFlight();
    target.setAllowFlight(newState);
    if (newState) target.setFlying(true);

    if (isSelf) {
      String msgKey = newState ? "fly.enabled-self" : "fly.disabled-self";
      target.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString(msgKey)));
    } else {
      String msgKeySender = newState ? "fly.enabled" : "fly.disabled";
      String msgKeyTarget = newState ? "fly.enabled-self" : "fly.disabled-self";
      sender.sendMessage(MessageUtil.formatMessage(
              plugin.getLangConfig().getString(msgKeySender)
                      .replace("%player%", target.getName())
      ));
      target.sendMessage(MessageUtil.formatMessage(
              plugin.getLangConfig().getString(msgKeyTarget)
                      .replace("%player%", sender.getName())
      ));
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
    if (!cmd.getName().equalsIgnoreCase("fly")) return Collections.emptyList();

    if (args.length == 1 && sender.hasPermission("lifemod.fly.others")) {
      return Bukkit.getOnlinePlayers().stream()
              .map(Player::getName)
              .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
              .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }
}
