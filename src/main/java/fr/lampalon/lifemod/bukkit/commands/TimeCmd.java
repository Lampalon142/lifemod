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

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TimeCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public TimeCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("lifemod.time")) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("time.usage")));
            return true;
        }

        String time = args[0].toLowerCase();
        long ticks;
        switch (time) {
            case "day":
                ticks = 1000;
                break;
            case "noon":
                ticks = 6000;
                break;
            case "night":
                ticks = 13000;
                break;
            case "midnight":
                ticks = 18000;
                break;
            default:
                ticks = -1;
                break;
        }

        if (ticks == -1) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("time.invalid")));
            return true;
        }

        Bukkit.getWorlds().forEach(world -> world.setTime(ticks));
        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("time.success").replace("%time%", time)));

        if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(plugin.getConfigConfig().getString("discord.time.title"))
                        .setDescription(plugin.getConfigConfig().getString("discord.time.description")
                                .replace("%player%", sender.getName())
                                .replace("%time%", time))
                        .setFooter(plugin.getConfigConfig().getString("discord.time.footer.title"),
                                plugin.getConfigConfig().getString("discord.time.footer.logo"))
                        .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.time.color")))));
                webhook.execute();
            } catch (IOException e) {
                debug.log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("day", "noon", "night", "midnight");
        return new ArrayList<>();
    }
}
