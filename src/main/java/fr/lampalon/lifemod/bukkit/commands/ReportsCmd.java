package fr.lampalon.lifemod.bukkit.commands;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DiscordWebhook;
import fr.lampalon.lifemod.bukkit.models.Report;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ReportsCmd implements CommandExecutor {
    private final LifeMod plugin;

    public ReportsCmd(LifeMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.onlyplayer")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.reports")) {
            player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("general.nopermission")));
            return true;
        }

        int page = parsePage(args);
        int itemsPerPage = plugin.getLangConfig().getInt("report.main.items-per-page", 45);

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

        fetchAndOpenReports(player, page, itemsPerPage);
        return true;
    }

    private int parsePage(String[] args) {
        if (args.length < 1) return 0;
        try {
            return Math.max(0, Integer.parseInt(args[0]) - 1);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void fetchAndOpenReports(Player player, int page, int itemsPerPage) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<Report> reports = plugin.getDatabaseManager().getDatabaseProvider().getAllReports();
                reports.sort(Comparator.comparingLong(Report::getCreatedAt).reversed());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (reports.isEmpty()) {
                        player.sendMessage(MessageUtil.formatMessage(plugin.getLangConfig().getString("report.no-reports")));
                        return;
                    }
                    plugin.getGuiManager().openMainMenu(player, reports, page, itemsPerPage);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}