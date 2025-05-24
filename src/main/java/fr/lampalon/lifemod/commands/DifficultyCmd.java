package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DebugManager;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
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
import java.util.stream.Collectors;

public class DifficultyCmd implements CommandExecutor, TabCompleter {
    private final LifeMod plugin;
    private final DebugManager debug;

    public DifficultyCmd(LifeMod plugin) {
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("lifemod.difficulty")) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            return true;
        }

        if (args.length == 0) {
            Difficulty diff = Bukkit.getWorlds().get(0).getDifficulty();
            String available = Arrays.stream(Difficulty.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.joining(", "));
            String msg = plugin.getLangConfig().getString("difficulty.current", "&eCurrent difficulty: &a%difficulty% &7(Available: &f%available%&7)")
                    .replace("%difficulty%", diff.name().toLowerCase())
                    .replace("%available%", available);
            sender.sendMessage(MessageUtil.formatMessage(msg));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("difficulty.usage")));
            return true;
        }

        String difficultyStr = args[0].toUpperCase();
        try {
            Difficulty difficulty = Difficulty.valueOf(difficultyStr);
            for (World world : Bukkit.getWorlds()) {
                world.setDifficulty(difficulty);
            }
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("difficulty.success")
                    .replace("%difficulty%", difficulty.name().toLowerCase())));
            // Discord Webhook
            if (plugin.getConfigConfig().getBoolean("discord.enabled")) {
                try {
                    DiscordWebhook webhook = new DiscordWebhook(plugin.webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(plugin.getConfigConfig().getString("discord.difficulty.title"))
                            .setDescription(plugin.getConfigConfig().getString("discord.difficulty.description")
                                    .replace("%player%", sender.getName())
                                    .replace("%difficulty%", difficultyStr))
                            .setFooter(plugin.getConfigConfig().getString("discord.difficulty.footer.title"),
                                    plugin.getConfigConfig().getString("discord.difficulty.footer.logo"))
                            .setColor(Color.decode(Objects.requireNonNull(plugin.getConfigConfig().getString("discord.difficulty.color")))));
                    webhook.execute();
                } catch (IOException e) {
                    debug.log("discord", "Webhook error: " + e.getMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("difficulty.invalid")));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.stream(Difficulty.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList());
        return new ArrayList<>();
    }
}
