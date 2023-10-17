package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EcopenCmd implements CommandExecutor {
    Messages messages;

    public EcopenCmd(Messages messages) {
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String s = LifeMod.getInstance().getConfig().getString("msgopenec");
        if (label.equalsIgnoreCase("ecopen")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("lifemod.ecopen")){
                    player.sendMessage(messages.prefixGeneral + messages.noperm);
                    return true;
                }
                if (args.length != 1) {
                    player.sendMessage(messages.prefixGeneral + messages.usageopenec);
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[0]);

                if (targetPlayer == null) {
                    player.sendMessage(messages.prefixGeneral + messages.offlineplayer);
                    return true;
                }

                if (player.hasPermission("lifemod.ecopen")) {
                    Inventory enderChest = Bukkit.createInventory(player, 27, messages.prefixGeneral + s.replace("%player%", targetPlayer.getPlayer().getName()));
                    enderChest.setContents(targetPlayer.getEnderChest().getContents());
                    player.openInventory(enderChest);
                }
            }
            if (!(sender instanceof Player)){
                sender.sendMessage(messages.noconsole);
            }
            return true;
        }
        return false;
    }
}
