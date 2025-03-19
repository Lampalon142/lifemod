package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.database.DatabaseManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OInvseeCommand implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final FileConfiguration langConfig;

    public OInvseeCommand(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.langConfig = LifeMod.getInstance().getLangConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.formatMessage(langConfig.getString("general.onlyplayer", "&cYou can't execute this command on the console.")));
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lifemod.oinvsee")) {
            sender.sendMessage(MessageUtil.formatMessage(langConfig.getString("general.nopermission", "&cYou don't have permission for this.")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("oinvsee.usage", "&cUsage : /oinvsee <player>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("general.offlineplayer", "&cThis person is not connected to the server.")));
            return true;
        }

        ItemStack[] savedInventory = databaseManager.getSQLiteManager().getPlayerInventory(target.getUniqueId());
        if (savedInventory == null) {
            player.sendMessage(MessageUtil.formatMessage(langConfig.getString("oinvsee.no-inventory", "&cNo saved inventory for %target%.").replace("%target%", args[0])));
            return true;
        }

        String inventoryTitle = MessageUtil.formatMessage(langConfig.getString("oinvsee.name", "&cPlayer inventory %target%").replace("%target%", args[0]));
        Inventory inv = Bukkit.createInventory(null, 45, inventoryTitle);
        inv.setContents(savedInventory);
        player.openInventory(inv);

        player.sendMessage(MessageUtil.formatMessage(langConfig.getString("oinvsee.success", "&aYou are now viewing %target%'s inventory.").replace("%target%", args[0])));
        return true;
    }
}