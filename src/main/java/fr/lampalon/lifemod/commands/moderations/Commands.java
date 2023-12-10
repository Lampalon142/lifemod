package fr.lampalon.lifemod.commands.moderations;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.data.configuration.Messages;
import fr.lampalon.lifemod.manager.PlayerManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    // private Map<Player, Long> reportCooldown = new HashMap<>();
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages messages = (LifeMod.getInstance()).messages;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.noconsole));
            return false;
        }

        Player player = (Player)sender;

        if (label.equalsIgnoreCase("mod")) {
            if (!player.hasPermission("lifemod.mod")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.noperm));
                return false;
            }

            if (PlayerManager.isInModerationMod(player)) {
                PlayerManager.getFromPlayer(player).destroy();
            } else {
                (new PlayerManager(player)).init();
            }
        } else if (label.equalsIgnoreCase("staff")){
            if (!player.hasPermission("lifemod.mod")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.prefixGeneral + messages.noperm));
                return false;
            }
            if (PlayerManager.isInModerationMod(player)) {
                PlayerManager.getFromPlayer(player).destroy();
            } else {
                (new PlayerManager(player)).init();
            }
        }

        // if (label.equalsIgnoreCase("report")) {
            // if (args.length != 1) {
                // player.sendMessage(ChatColor.RED + "Veuillez saisir le pseudo du joueur concerné.");
                // return false;
            // }

            // String targetName = args[0];


            // if (Bukkit.getPlayer(targetName) == null) {
                // player.sendMessage(ChatColor.RED + "Ce joueur n'est pas connecté ou n'existe pas !");
                // return false;
            // }

        // Player target = Bukkit.getPlayer(targetName);


        // if (player == target) {
        // player.sendMessage("§cVous ne pouvez pas vous report vous même, petit malin !");
        // return false;
        // }

        // if (this.reportCooldown.containsKey(player)) {
        // long time = (System.currentTimeMillis() - ((Long)this.reportCooldown.get(player)).longValue()) / 1000L;

        // if (time < 60L) {
        //  player.sendMessage("§cMerci de patientez 1 minute entre chaque signalement.");
        // player.closeInventory();
        // return false;
        // }
        // this.reportCooldown.remove(player);
        // }


            // Main.getInstance().getWhoReport().put((Player)sender, Bukkit.getPlayer(args[0]));

        // Inventory inv = Bukkit.createInventory(null, 18, ChatColor.AQUA + "Report: " + ChatColor.RED + target.getName());

        // inv.setItem(0, (new ItemBuilder(Material.IRON_SWORD)).setName(ChatColor.RED + "NoFear RP").toItemStack());
        // inv.setItem(1, (new ItemBuilder(Material.BOW)).setName(ChatColor.RED + "FreeKill").toItemStack());

        // player.openInventory(inv);

        // this.reportCooldown.put(player, Long.valueOf(System.currentTimeMillis()));
        // }

        return false;
    }
}