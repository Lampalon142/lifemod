package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class SpeedCmd implements CommandExecutor, TabCompleter {
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("speed")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
            debug.log("speed", "Console tried to use /speed");
            return false;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("speed.use")) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
            debug.log("speed", "Permission denied for /speed by " + player.getName());
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("speed.provide")));
            return false;
        }
        int speed;
        try {
            speed = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("speed.provide")));
            debug.log("speed", "Invalid speed input by " + player.getName());
            return false;
        }

        if (speed < 1 || speed > 10) {
            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("speed.provide")));
            debug.log("speed", "Speed out of range by " + player.getName());
            return false;
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.speed.title"))
                        .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.speed.description").replace("%player%", sender.getName()))
                        .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.speed.footer.title"),
                                LifeMod.getInstance().getConfigConfig().getString("discord.speed.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.speed.color")))));
                webhook.execute();
                debug.log("speed", player.getName() + " changed speed to " + speed + " (Discord notified)");
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord speed alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            debug.log("speed", player.getName() + " changed speed to " + speed);
        }

        if (player.isFlying()) {
            player.setFlySpeed((float) speed / 10);
        } else {
            player.setWalkSpeed((float) speed / 10);
        }

        String s4 = LifeMod.getInstance().getLangConfig().getString("speed.success");
        player.sendMessage(MessageUtil.formatMessage(s4.replace("%speed%", String.valueOf(speed))));
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("speed")) {
            if (args.length == 1) {
                return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            }
        }
        return null;
    }
}
