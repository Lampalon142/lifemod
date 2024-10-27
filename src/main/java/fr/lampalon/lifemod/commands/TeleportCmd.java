package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public class TeleportCmd implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (LifeMod.getInstance().isTeleportActive()) {
            if (label.equalsIgnoreCase("tp")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("lifemod.tp")) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    return true;
                }

                if (args.length != 1 && args.length != 3 && args.length != 2) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("tp.usage")));
                    return true;
                }

                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.teleport.title"))
                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.teleport.description").replace("%player%", sender.getName()))
                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.teleport.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.teleport.footer.logo").replace("%player%", sender.getName()))
                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.teleport.color")))));
                    try {
                        webhook.execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (args.length == 1) {
                    String targetName = args[0];
                    Player target = Bukkit.getPlayer(targetName);

                    if (target == null) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                        return true;
                    }

                    if (target == player) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("tp.yourself")));
                        return false;
                    }

                    String target1 = LifeMod.getInstance().getLangConfig().getString("tp.success");
                    player.teleport(target);
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + target1.replace("%player%", target.getPlayer().getName())));

                } else if (args.length == 2) {
                    Player target1 = Bukkit.getPlayer(args[0]);
                    Player target2 = Bukkit.getPlayer(args[1]);

                    if (target1 == null || target2 == null) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                        return true;
                    }

                    target1.teleport(target2.getLocation());
                    String messages = LifeMod.getInstance().getLangConfig().getString("tp.twoplayers");
                    player.sendMessage(MessageUtil.parseColors(messages.replace("%player1%", target1.getName()).replace("%player2%", target2.getName())));
                } else if (args.length == 3) {

                    try {
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        double z = Double.parseDouble(args[2]);

                        Location targetLocation = new Location(player.getWorld(), x, y, z);
                        String target1 = LifeMod.getInstance().getLangConfig().getString("tp.success");
                        player.teleport(targetLocation);
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + target1.replace("%player%", "coordinates")));
                    } catch (NumberFormatException e) {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("invalidcoordinates")));
                    }
                }

                return true;
            } else if (label.equalsIgnoreCase("tphere")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("lifemod.tphere")) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    return true;
                }

                if (args.length != 1) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("tphere.usage")));
                    return true;
                }

                Player senderPlayer = (Player) sender;
                Player targetPlayer = Bukkit.getPlayer(args[0]);

                if (targetPlayer == null) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                    return true;
                }

                String target2 = LifeMod.getInstance().getLangConfig().getString("tphere.success");
                targetPlayer.teleport(senderPlayer.getLocation());
                player.sendMessage(MessageUtil.parseColors(target2.replace("%player%", targetPlayer.getPlayer().getName())));
                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("tp")) {
            List<String> playersList = new ArrayList<>();

            if (args.length == 1 || args.length == 2){
                for (Player player : Bukkit.getOnlinePlayers()){
                    playersList.add(player.getName());
                }
            }
            return playersList;
        }
        return null;
    }
}
