package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class FlyCmd implements CommandExecutor
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
}