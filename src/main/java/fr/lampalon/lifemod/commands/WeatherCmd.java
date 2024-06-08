package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WeatherCmd implements CommandExecutor {
    private final LifeMod plugin;

    public WeatherCmd(LifeMod plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (plugin.isWeatherActive()) {
            if (label.equalsIgnoreCase("weather")) {

                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("onlyplayer")));
                    return true;
                }

                Player player = (Player) sender;

                boolean vanishEnabled = LifeMod.getInstance().getConfig().getBoolean("commands-enabled.weather");

                if (!vanishEnabled) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("prefix") + LifeMod.getInstance().getConfigConfig().getString("command-deactivate")));
                    return true;
                }

                World world = player.getWorld();

                if (args.length != 1) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("weatherusage")));
                    return false;
                }

                if (!player.hasPermission("lifemod.weather")) {
                    player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("nopermission")));
                    return false;
                }

                String weatherType = args[0].toLowerCase();

                switch (weatherType) {
                    case "clear":
                    case "sun":
                        world.setStorm(false);
                        world.setThundering(false);
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("weathersun")));
                        break;
                    case "rain":
                        world.setStorm(true);
                        world.setThundering(false);
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("weatherrain")));
                        break;
                    case "storm":
                        world.setStorm(true);
                        world.setThundering(true);
                        player.sendMessage(MessageUtil.parseColors(LifeMod.getInstance().getConfigConfig().getString("weatherstorm")));
                        break;
                }

                return true;
            }
        } else {
            sender.sendMessage(MessageUtil.parseColors(messages.prefixGeneral + messages.commanddisable));
        }

        return true;
    }
}
