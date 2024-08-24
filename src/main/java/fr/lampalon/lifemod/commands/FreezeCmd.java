package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.FreezeManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FreezeCmd implements CommandExecutor, Listener {
    private LifeMod main;

    public FreezeCmd(LifeMod main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FreezeManager freezeManager = LifeMod.getInstance().getFreezeManager();
        Messages messages = (LifeMod.getInstance()).messages;
        if (main.isFreezeActive()) {
            if (label.equalsIgnoreCase("freeze")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                    return true;
                }
                Player player = (Player) sender;

                if (player.hasPermission("lifemod.freeze")) {
                    if (args.length == 1) {

                        Player target = Bukkit.getPlayer(args[0]);

                        ItemStack air = new ItemStack(Material.AIR);
                        ItemStack packedice = new ItemStack(Material.PACKED_ICE);

                        if (target != null && !target.equals(player)) {
                            if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
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
                            if (freezeManager.isPlayerFrozen(target.getUniqueId()) && LifeMod.getInstance().getFrozenPlayers().containsKey(target.getUniqueId())) {
                                freezeManager.unfreezePlayer(player, target);
                                LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
                                target.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + LifeMod.getInstance().getConfigConfig().getString("unfreezeby").replace("%player%", player.getName())));
                                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + LifeMod.getInstance().getConfigConfig().getString("unfreeze").replace("%target%", target.getName())));
                            } else {
                                String s4 = LifeMod.getInstance().getConfig().getString("freeze-msg-six");
                                LifeMod.getInstance().getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
                                InputStream input = LifeMod.getInstance().getClass().getClassLoader().getResourceAsStream("config.yml");
                                Yaml yaml = new Yaml();
                                Map<String, List<String>> config = yaml.load(input);
                                List<String> freezeMsg = config.get("freeze-msg");

                                for (String msg : freezeMsg) {
                                    target.sendMessage(MessageUtil.parseColors(msg));
                                }

                                freezeManager.freezePlayer(player, target);
                                player.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + s4.replace("%target%", target.getName())));
                            }
                        } else {
                            player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("offlineplayer")));
                        }
                    } else {
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("freeze-usage")));
                    }
                } else {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                    return true;
                }
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
        }
        return false;
    }
}