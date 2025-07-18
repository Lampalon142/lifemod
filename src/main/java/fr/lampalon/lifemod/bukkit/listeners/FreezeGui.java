package fr.lampalon.lifemod.bukkit.listeners;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.managers.DebugManager;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

public class FreezeGui implements Listener {
    private final LifeMod plugin;
    private final DebugManager debug;

    public FreezeGui(LifeMod plugin){
        this.plugin = plugin;
        this.debug = plugin.getDebugManager();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String freezeTitle = MessageUtil.formatMessage(plugin.getLangConfig().getString("freeze.gui.title"));
        if (event.getView().getTitle().equals(freezeTitle)) {
            UUID playerId = event.getPlayer().getUniqueId();
            if (plugin.getFreezeManager().isPlayerFrozen(playerId)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    event.getPlayer().openInventory(event.getInventory());
                    debug.log("freeze", event.getPlayer().getName() + " tried to close Freeze GUI (reopened)");
                }, 1L);
            } else {
                debug.log("freeze", event.getPlayer().getName() + " closed Freeze GUI (not frozen)");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String freezeTitle = MessageUtil.formatMessage(plugin.getLangConfig().getString("freeze.gui.title"));
        if (event.getView().getTitle().equals(freezeTitle)) {
            event.setCancelled(true);
            debug.log("freeze", event.getWhoClicked().getName() + " tried to click in Freeze GUI (cancelled)");
        }
    }
}
