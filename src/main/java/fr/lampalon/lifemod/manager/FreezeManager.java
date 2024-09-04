package fr.lampalon.lifemod.manager;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class FreezeManager {
    private HashMap<UUID, ItemStack> playerHelmets = new HashMap<>();
    private HashMap<UUID, Location> frozenPlayers = new HashMap<>();

    public boolean isPlayerFrozen(UUID playerId) {
        return frozenPlayers.containsKey(playerId);
    }

    public void freezePlayer(Player player, Player target) {
        ItemStack helmet = target.getInventory().getHelmet();
        if (helmet != null) {
            playerHelmets.put(target.getUniqueId(), helmet);
        }
        frozenPlayers.put(target.getUniqueId(), target.getLocation());
        target.getInventory().setHelmet(new ItemStack(Material.PACKED_ICE));

        openFreezeGUI(target);
    }

    public void unfreezePlayer(Player player, Player target) {
        if (frozenPlayers.containsKey(target.getUniqueId())) {
            target.getInventory().setHelmet(null);

            if (playerHelmets.containsKey(target.getUniqueId())) {
                target.getInventory().setHelmet(playerHelmets.get(target.getUniqueId()));
                playerHelmets.remove(target.getUniqueId());
            }

            frozenPlayers.remove(target.getUniqueId());
        }
    }

    private void openFreezeGUI(Player player) {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("freeze.gui.glass-title")));
        item.setItemMeta(meta);

        Inventory freezeMenu = Bukkit.createInventory(null, 27, MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("freeze.gui.title")));
        freezeMenu.setItem(11, item);
        freezeMenu.setItem(13, item);
        freezeMenu.setItem(15, item);
        player.openInventory(freezeMenu);
    }

    public HashMap<UUID, Location> getFrozenPlayers() {
        return frozenPlayers;
    }
}
