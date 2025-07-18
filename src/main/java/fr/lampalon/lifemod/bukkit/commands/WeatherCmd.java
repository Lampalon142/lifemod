package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WeatherCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public WeatherCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("weather")) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            debug.log("weather", "Console tried to use /weather");
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        if (args.length != 1) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("weather.usage")));
            debug.log("weather", "Invalid usage by " + player.getName());
            return false;
        }

        if (!player.hasPermission("lifemod.weather")) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            debug.log("weather", "Permission denied for /weather by " + player.getName());
            return false;
        }

        String weatherType = args[0].toLowerCase();

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.weather.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.weather.description").replace("%player%", sender.getName()))
                        .setFooter(plugin.getConfigConfig().getString("discord.weather.footer.title"),
                                plugin.getConfigConfig().getString("discord.weather.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.weather.color")))));
                webhook.execute();
                debug.log("weather", player.getName() + " changed weather to " + weatherType + " (Discord notified)");
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord weather alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            debug.log("weather", player.getName() + " changed weather to " + weatherType);
        }

        switch (weatherType) {
            case "clear":
            case "sun":
                world.setStorm(false);
                world.setThundering(false);
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("weather.sun")));
                break;
            case "rain":
                world.setStorm(true);
                world.setThundering(false);
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("weather.rain")));
                break;
            case "storm":
                world.setStorm(true);
                world.setThundering(true);
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("weather.storm")));
                break;
            default:
                player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("weather.usage")));
                debug.log("weather", player.getName() + " entered unknown weather type: " + weatherType);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("weather")) {
            if (args.length == 1){
                List<String> suggestions = new ArrayList<>();
                suggestions.add("sun");
                suggestions.add("rain");
                suggestions.add("storm");
                return suggestions;
            }
        }
        return null;
    }
}
