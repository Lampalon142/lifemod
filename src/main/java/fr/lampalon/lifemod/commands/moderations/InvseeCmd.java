package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class InvseeCmd implements CommandExecutor, Listener {
    Messages messages;
    LifeMod plugin;
    public InvseeCmd(LifeMod plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("invsee")){
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noconsole));
                return true;
            }
            Player player = (Player) sender;

            if (player.hasPermission("lifemod.invsee")){
                int i;

                if (args.length != 1) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.invseeusage));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);

                if (target == player){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.yourselfinvsee));
                    return true;
                }
                String invyes = LifeMod.getInstance().getConfig().getString("inventoryname");
                Inventory inv = Bukkit.createInventory(null, 45, invyes.replace("%player%", target.getPlayer().getName()));

                for (i = 0; i < 36; i++) {
                    if (target.getInventory().getItem(i) != null) {
                        inv.setItem(i, target.getInventory().getItem(i));
                    }
                }

                inv.setItem(36, target.getInventory().getHelmet());
                inv.setItem(37, target.getInventory().getChestplate());
                inv.setItem(38, target.getInventory().getLeggings());
                inv.setItem(39, target.getInventory().getBoots());

                player.openInventory(target.getInventory());
            }
        }
        return false;
    }
}
