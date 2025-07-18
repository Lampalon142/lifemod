package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.models.ReportStatus;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class ReportCmd implements CommandExecutor {
    private final LifeMod plugin;

    public ReportCmd(LifeMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.usage")));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        Player player = (Player) sender;
        if (target == null || !target.isOnline()) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.offlineplayer")));
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.yourself")));
            return true;
        }

        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        UUID uuid = UUID.randomUUID();
        long now = System.currentTimeMillis();
        Location location = player.getLocation();

        Report report = new Report(
                uuid,
                player.getUniqueId(),
                target.getUniqueId(),
                reason,
                player.getServer().getName(),
                location,
                ReportStatus.OPEN,
                null,
                now,
                now,
                null,
                0,
                null
        );
        plugin.getDatabaseManager().getDatabaseProvider().saveReport(report);

        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject()
                        .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.report.title"))
                        .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.report.description").replace("%player%", sender.getName()))
                        .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.report.footer.title"),
                                LifeMod.getInstance().getConfigConfig().getString("discord.report.footer.logo").replace("%player%", sender.getName()))
                        .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.report.color")))));
                webhook.execute();
            } catch (IOException e) {
                LifeMod.getInstance().getDebugManager().userError(sender, "Failed to send Discord Report alert", e);
                LifeMod.getInstance().getDebugManager().log("discord", "Webhook error: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            LifeMod.getInstance().getDebugManager().log("report", player.getName() + " was executed report command");
        }

        sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.submitted").replace("%target%", target.getName()).replace("%reason%", reason)).replace("%server%", plugin.getServer().getName()));
        return true;
    }
}
