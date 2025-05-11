package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.SpectateManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCmd implements CommandExecutor {
    private final SpectateManager spectateManager;

    public SpectateCmd(LifeMod plugin) {
        this.spectateManager = plugin.getSpectateManager();
    }

    private String getLang(String key) {
        String msg = LifeMod.getInstance().getLangConfig().getString(key);
        return msg != null ? msg : "§c[Lang] Missing key: " + key;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.formatMessage(getLang("general.onlyplayer")));
            return true;
        }

        if (!player.hasPermission("lifemod.spectate")) {
            player.sendMessage(MessageUtil.formatMessage(getLang("general.nopermission")));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(MessageUtil.formatMessage(getLang("spectate.usage")));
            return true;
        }

        String arg = args[0].toLowerCase();

        switch (arg) {
            case "leave" -> spectateManager.leaveSpectate(player);
            case "fp" -> spectateManager.startFreecam(player);
            case "random" -> spectateManager.spectateRandom(player);
            case "back" -> spectateManager.spectateBack(player);
            case "list" -> spectateManager.sendPlayerList(player);
            default -> {
                Player target = Bukkit.getPlayer(arg);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(MessageUtil.formatMessage(
                            getLang("spectate.player-not-found").replace("%target%", arg)));
                    return true;
                }
                spectateManager.startSpectate(player, target);
            }
        }
        return true;
    }
}
