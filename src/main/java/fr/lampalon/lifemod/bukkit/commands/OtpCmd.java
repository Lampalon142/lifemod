package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.managers.database.DatabaseManager;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OtpCmd implements CommandExecutor, TabCompleter {

    private final DatabaseManager databaseManager;
    private final FileConfiguration langConfig;
    private final DebugManager debug;

    public OtpCmd(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.langConfig = LifeMod.getInstance().getLangConfig();
        this.debug = LifeMod.getInstance().getDebugManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(langConfig.getString("general.onlyplayer", "&cThis command must be executed by a player.")));
            debug.log("otp", "Console tried to use /otp");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.otp")) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("general.nopermission", "&cYou don't have permission to use this command.")));
            debug.log("otp", "Permission denied for /otp by " + player.getName());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.usage", "&cUsage: /otp <player>")));
            debug.log("otp", "Invalid usage by " + player.getName());
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.player-not-found", "&cPlayer %target% doesn't exist or has never connected.").replace("%target%", args[0])));
            debug.log("otp", "Target never played: " + args[0]);
            return true;
        }

        Location location = databaseManager.getDatabaseProvider().getPlayerCoords(target.getUniqueId());
        if (location == null) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.no-position", "&cNo saved position for %target%.").replace("%target%", args[0])));
            debug.log("otp", "No saved position for " + args[0]);
            return true;
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.otp.title"))
                        .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.otp.description").replace("%player%", sender.getName()))
                        .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.otp.footer.title"),
                                LifeMod.getInstance().getConfigConfig().getString("discord.otp.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.otp.color")))));
                webhook.execute();
                debug.log("otp", player.getName() + " teleported to offline position of " + args[0] + " (Discord notified)");
            } catch (IOException e) {
                debug.userError(sender, "Failed to send Discord otp alert", e);
                debug.log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            debug.log("otp", player.getName() + " teleported to offline position of " + args[0]);
        }

        player.teleport(location);
        player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.teleported", "&aYou have been teleported to %target%'s last position.").replace("%target%", args[0])));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("otp")) return null;
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
