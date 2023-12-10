package fr.lampalon.lifemod.commands.users;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class FreezeCmd implements CommandExecutor, Listener {
    private Messages messages;
    private LifeMod main;

    public FreezeCmd(LifeMod main, Messages messages){
        this.main = main;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Conditions et exécution //
        if (label.equalsIgnoreCase("freeze")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.noconsole);
                return true;
            }
            Player player = (Player) sender;
            if (player.hasPermission("lifemod.freeze")) {
                if (args.length == 1) {

                    Player target = Bukkit.getPlayer(args[0]);

                    if (target.equals(player)) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.freezeno));
                        return true;
                    }

                    if (main.getFrozenPlayers().containsKey(target.getUniqueId())) {
                        String s2 = LifeMod.getInstance().getConfig().getString("unfreeze");
                        String s3 = LifeMod.getInstance().getConfig().getString("unfreezeby");
                        LifeMod.getInstance().getFrozenPlayers().remove(target.getUniqueId());
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s3.replace("%player%", player.getPlayer().getName())));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s2.replace("%target%", target.getPlayer().getName())));
                    } else {
                        InputStream input = LifeMod.getInstance().getClass().getClassLoader().getResourceAsStream("config.yml");
                        Yaml yaml = new Yaml();
                        Map<String, List<String>> config = yaml.load(input);

                        List<String> freezeMsg = config.get("freeze-msg");

                        main.getFrozenPlayers().put(target.getUniqueId(), target.getLocation());
                        for (String msg : freezeMsg) {
                            target.sendMessage(msg);
                        }

                        String s4 = main.getConfig().getString("freeze-msg-six");
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + s4.replace("%target%", target.getPlayer().getName())));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.freezeusage));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noperm));
                return true;
            }
        }
        return false;
    }
}