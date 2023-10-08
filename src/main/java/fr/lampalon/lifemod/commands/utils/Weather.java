package fr.lampalon.lifemod.commands.utils;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Weather implements CommandExecutor {
    private Messages messages;
    private final LifeMod plugin;

    public Weather(LifeMod plugin, Messages messages) {
        this.messages = messages;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(label.equalsIgnoreCase("weather")){
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.noconsole);
                return true;
            }

            Player player = (Player) sender;
            World world = player.getWorld();

            if (args.length != 1) {
                player.sendMessage(messages.weatherusage);
                return false;
            }

            if (!player.hasPermission("lifemod.weather")){
                player.sendMessage(messages.noperm);
                return false;
            }

            String weatherType = args[0].toLowerCase();

            switch (weatherType) {
                case "clear":
                case "sun":
                    world.setStorm(false);
                    world.setThundering(false);
                    player.sendMessage(messages.weathersun);
                    break;
                case "rain":
                    world.setStorm(true);
                    world.setThundering(false);
                    player.sendMessage(messages.weatherrain);
                    break;
                case "storm":
                    world.setStorm(true);
                    world.setThundering(true);
                    player.sendMessage(messages.weatherstorm);
                    break;
            }
            return true;
        }
        return true;
    }
}
