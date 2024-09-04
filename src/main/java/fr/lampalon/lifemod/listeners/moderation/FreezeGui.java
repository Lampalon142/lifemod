package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.LifeMod;
import fr.lampalon.lifemod.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

public class FreezeGui implements Listener {
    private final LifeMod plugin;

    public FreezeGui(LifeMod plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("freeze.gui.title")))) {
            UUID playerId = event.getPlayer().getUniqueId();
            if (LifeMod.getInstance().getFreezeManager().isPlayerFrozen(playerId)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> event.getPlayer().openInventory(event.getInventory()), 1L);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(MessageUtil.parseColors(LifeMod.getInstance().getLangConfig().getString("freeze.gui.title")))) {
            event.setCancelled(true);
        }
    }
}
