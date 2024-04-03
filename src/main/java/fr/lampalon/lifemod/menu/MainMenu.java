package fr.lampalon.lifemod.menu;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.OreTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {
    private final int PLAYERS_PER_PAGE = 45;
    private final OreTracker oreTracker;

    public MainMenu(OreTracker oreTracker) {
        this.oreTracker = oreTracker;
    }

    public void openMenu(Player player, int page) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        List<Player> playersOnPage = calculatePlayersForPage(page);

        Inventory menu = Bukkit.createInventory(player, config.getInt("menu.slots"), ChatColor.translateAlternateColorCodes('&', config.getString("menu.title")));
        for (Player targetPlayer : playersOnPage) {
            double percentage = oreTracker.getPlayerPercentage(targetPlayer);
            ItemStack item = createItem(targetPlayer.getName(), percentage);
            menu.addItem(item);
        }

        menu.setItem(45, createPrevPageItem(page));
        menu.setItem(53, createNextPageItem(page));

        player.openInventory(menu);
    }

    private List<Player> calculatePlayersForPage(int page) {
        int startIndex = (page - 1) * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, Bukkit.getOnlinePlayers().size());
        List<Player> playersOnPage = new ArrayList<>(Bukkit.getOnlinePlayers());
        playersOnPage = playersOnPage.subList(startIndex, endIndex);
        return playersOnPage;
    }

    private ItemStack createPrevPageItem(int currentPage) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("menu.previous-page.title")));
        meta.setCustomModelData(currentPage - 1);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextPageItem(int currentPage) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("menu.next-page.title")));
        meta.setCustomModelData(currentPage + 1);
        item.setItemMeta(meta);
        return item;
    }

    private boolean hasPreviousPage(int currentPage) {
        return currentPage > 1;
    }

    private boolean hasNextPage(int currentPage) {
        int maxPages = (int) Math.ceil((double) Bukkit.getOnlinePlayers().size() / PLAYERS_PER_PAGE);
        return currentPage < maxPages;
    }

    private ItemStack createItem(String playerName, double percentage) {
        FileConfiguration config = LifeMod.getInstance().getConfig();
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("menu.headers.title").replace("%target%", playerName)));

        List<String> lore = new ArrayList<>();
        String percentageString = Double.toString(percentage);

        lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("menu.headers.description").replace("%percentage%", percentageString)));

        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
