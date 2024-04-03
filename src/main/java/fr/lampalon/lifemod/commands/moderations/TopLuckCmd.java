package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.menu.MainMenu;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class TopLuckCmd implements CommandExecutor {
    private final LifeMod plugin;
    private final MainMenu menuManager;
    private FileConfiguration config = LifeMod.getInstance().getConfig();
    public TopLuckCmd(LifeMod plugin, MainMenu menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("topluck")){

            if (!(sender instanceof Player)){
                sender.sendMessage(Objects.requireNonNull(MessageUtil.parseColors(config.getString("prefix") + config.getString("onlyplayer"))));
            }

            Player player = (Player) sender;

            if (!player.hasPermission("lifemod.topluck")){
                player.sendMessage(MessageUtil.parseColors(config.getString("prefix") + config.getString("noperm")));
                return false;
            }

            menuManager.openMenu(player, 1);
        }
        return false;
    }
}
