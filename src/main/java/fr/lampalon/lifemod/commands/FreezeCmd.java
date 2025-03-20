package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.FreezeManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class FreezeCmd implements CommandExecutor, TabCompleter {
    private LifeMod main;

    public FreezeCmd(LifeMod main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FreezeManager freezeManager = LifeMod.getInstance().getFreezeManager();
            if (label.equalsIgnoreCase("freeze")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.onlyplayer")));
                    return true;
                }
                Player player = (Player) sender;

                if (player.hasPermission("lifemod.freeze")) {
                    if (args.length == 1) {

                        Player target = Bukkit.getPlayer(args[0]);

                        ItemStack air = new ItemStack(Material.AIR);
                        ItemStack packedice = new ItemStack(Material.PACKED_ICE);

                        if (target != null) {
                            if (!target.equals(player)) {
                                if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")) {
                                    DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
                                    webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                            .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.freeze.title"))
                                            .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.freeze.description").replace("%player%", sender.getName()))
                                            .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.freeze.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.freeze.footer.logo").replace("%player%", sender.getName()))
                                            .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.freeze.color")))));
                                    try {
                                        webhook.execute();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                String s2 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.unfreeze.mod");
                                String s3 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.unfreeze.target");
                                if (freezeManager.isPlayerFrozen(target.getUniqueId()) && LifeMod.getInstance().getFrozenPlayers().containsKey(target.getUniqueId())) {
                                    freezeManager.unfreezePlayer(player, target);
                                    LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
                                    target.sendMessage(MessageUtil.formatMessage(s3.replace("%player%", player.getName())));
                                    player.sendMessage(MessageUtil.formatMessage(s2.replace("%target%", target.getName())));
                                } else {
                                    String s4 = LifeMod.getInstance().getLangConfig().getString("freeze.messages.freeze.mod");
                                    LifeMod.getInstance().getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
                                    InputStream input = LifeMod.getInstance().getClass().getClassLoader().getResourceAsStream("lang.yml");
                                    Yaml yaml = new Yaml();
                                    Map<String, List<String>> config = yaml.load(input);
                                    List<String> freezeMsg = config.get("freeze.messages.onfreeze");

                                    for (String msg : freezeMsg) {
                                        target.sendMessage(MessageUtil.formatMessage(msg));
                                    }

                                    freezeManager.freezePlayer(player, target);
                                    player.sendMessage(MessageUtil.formatMessage(s4.replace("%target%", target.getName())));
                                }
                            } else {
                                player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("freeze.yourself")));
                            }
                        } else {
                            player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.offlineplayer")));
                        }
                    } else {
                        player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("freeze.usage")));
                    }
                } else {
                    player.sendMessage(MessageUtil.formatMessage(LifeMod.getInstance().getLangConfig().getString("general.nopermission")));
                    return true;
                }
            }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("freeze")) {
            if (args.length == 1) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames;
            }
        }
        return null;
    }
}