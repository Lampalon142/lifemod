package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FlyCmd implements CommandExecutor, TabCompleter
{

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (LifeMod.getInstance().isFlyActive()) {
      if (label.equalsIgnoreCase("fly")) {
        if (!(sender instanceof Player)) {
          sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
          return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("lifemod.fly")) {
          if (args.length == 0) {
            if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
              DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
              webhook.addEmbed(new DiscordWebhook.EmbedObject()
                      .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.fly.title"))
                      .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.fly.description").replace("%player%", sender.getName()))
                      .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.fly.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.fly.footer.logo").replace("%player%", sender.getName()))
                      .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.fly.color")))));
              try {
                webhook.execute();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
            if (player.getAllowFlight()) {
              player.setAllowFlight(false);
              player.setFlying(false);
              player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("fly.disable")));
            } else {
              player.setAllowFlight(true);
              player.setFlying(true);
              player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("fly.enable")));
            }
          }
        } else {
          player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
        }

        return true;
      }
    }else {
      sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
    }
    return false;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
    if (cmd.getName().equalsIgnoreCase("fly")) {
      if (args.length == 1) {
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