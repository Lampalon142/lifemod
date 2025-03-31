package fr.lampalon.lifemod.commands;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.manager.DiscordWebhook;
import fr.lampalon.lifemod.manager.database.DatabaseManager;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OInvseeCommand implements CommandExecutor, TabCompleter {

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

        if (LifeMod.getInstance().getConfigConfig().getBoolean("discord.enabled")){
            DiscordWebhook webhook = new DiscordWebhook(LifeMod.getInstance().webHookUrl);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle(LifeMod.getInstance().getConfigConfig().getString("discord.oinvsee.title"))
                    .setDescription(LifeMod.getInstance().getConfigConfig().getString("discord.oinvsee.description").replace("%player%", sender.getName()))
                    .setFooter(LifeMod.getInstance().getConfigConfig().getString("discord.oinvsee.footer.title"), LifeMod.getInstance().getConfigConfig().getString("discord.oinvsee.footer.logo").replace("%player%", sender.getName()))
                    .setColor(Color.decode(Objects.requireNonNull(LifeMod.getInstance().getConfigConfig().getString("discord.oinvsee.color")))));

            try {
                webhook.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String inventoryTitle = MessageUtil.formatMessage(langConfig.getString("oinvsee.name", "&cPlayer inventory %target%").replace("%target%", args[0]));
        Inventory inv = Bukkit.createInventory(null, 45, inventoryTitle);
        inv.setContents(savedInventory);
        player.openInventory(inv);

        player.sendMessage(MessageUtil.formatMessage(langConfig.getString("oinvsee.success", "&aYou are now viewing %target%'s inventory.").replace("%target%", args[0])));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (cmd.getName().equalsIgnoreCase("oinvsee")){
            if (args.length == 1){
                String input = args[args.length - 1].toLowerCase();
                completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());

                return completions;
            }
        }
        return null;
    }
}