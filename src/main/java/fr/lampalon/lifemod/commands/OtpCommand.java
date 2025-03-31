package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.database.DatabaseManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OtpCommand implements CommandExecutor, TabCompleter {

    private final DatabaseManager databaseManager;
    private final FileConfiguration langConfig;

    public OtpCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.langConfig = LifeMod.getInstance().getLangConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(langConfig.getString("general.onlyplayer", "&cThis command must be executed by a player.")));
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.otp")) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("general.nopermission", "&cYou don't have permission to use this command.")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.usage", "&cUsage: /otp <player>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.player-not-found", "&cPlayer %target% doesn't exist or has never connected.").replace("%target%", args[0])));
            return true;
        }

        Location location = databaseManager.getSQLiteManager().getPlayerCoords(target.getUniqueId());
        if (location == null) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.no-position", "&cNo saved position for %target%.").replace("%target%", args[0])));
            return true;
        }

        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
            DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.otp.title"))
                    .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.otp.description").replace("%player%", sender.getName()))
                    .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.otp.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.otp.footer.logo").replace("%player%", sender.getName()))
                    .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.otp.color")))));

            try {
                webhook.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        player.teleport(location);
        player.sendMessage(MessageUtil.formatMessage(langConfig.getString("otp.teleported", "&aYou have been teleported to %target%'s last position.").replace("%target%", args[0])));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (cmd.getName().equalsIgnoreCase("otp")){
            if (args.length == 1){
                String input = args[args.length - 1].toLowerCase();
                completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());

                return completions;
            }
        }
        return null;
    }
}