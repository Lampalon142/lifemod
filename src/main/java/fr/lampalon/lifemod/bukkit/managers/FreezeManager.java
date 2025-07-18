package fr.lampalon.lifemod.bukkit.managers;

import fr.lampalon.lifemod.bukkit.LifeMod;
import fr.lampalon.lifemod.bukkit.utils.MessageUtil;
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
    private final HashMap<UUID, ItemStack> playerHelmets = new HashMap<>();
    private final HashMap<UUID, Location> frozenPlayers = new HashMap<>();
    private final DebugManager debug = LifeMod.getInstance().getDebugManager();

    public boolean isPlayerFrozen(UUID playerId) {
        return frozenPlayers.containsKey(playerId);
    }

    public void freezePlayer(Player moderator, Player target) {
        try {
            ItemStack helmet = target.getInventory().getHelmet();
            if (helmet != null) {
                playerHelmets.put(target.getUniqueId(), helmet);
            }
            frozenPlayers.put(target.getUniqueId(), target.getLocation());
            target.getInventory().setHelmet(new ItemStack(Material.PACKED_ICE));

            openFreezeGUI(target);
            debug.log("freeze", moderator.getName() + " froze " + target.getName());
        } catch (Exception e) {
            debug.userError(moderator, "Error while freezing " + target.getName(), e);
        }
    }

    public void unfreezePlayer(Player moderator, Player target) {
        try {
            if (frozenPlayers.containsKey(target.getUniqueId())) {
                target.getInventory().setHelmet(null);

                if (playerHelmets.containsKey(target.getUniqueId())) {
                    target.getInventory().setHelmet(playerHelmets.get(target.getUniqueId()));
                    playerHelmets.remove(target.getUniqueId());
                }

                frozenPlayers.remove(target.getUniqueId());
                debug.log("freeze", moderator.getName() + " unfroze " + target.getName());
            }
        } catch (Exception e) {
            debug.userError(moderator, "Error while unfreezing " + target.getName(), e);
        }
    }

    private void openFreezeGUI(Player player) {
        try {
            Material glassMat;
            short data = 0;

            Material modern = Material.matchMaterial("BLACK_STAINED_GLASS_PANE");
            if (modern != null) {
                glassMat = modern;
            } else {
                glassMat = Material.matchMaterial("STAINED_GLASS_PANE");
            }

            ItemStack blackPane = new ItemStack(glassMat, 1, data);
            ItemMeta meta = blackPane.getItemMeta();
            meta.setDisplayName(
                    MessageUtil.formatMessage(
                            LifeMod.getInstance().getLangConfig().getString("freeze.gui.glass-title")
                    )
            );
            blackPane.setItemMeta(meta);

            Inventory freezeMenu = Bukkit.createInventory(
                    null,
                    27,
                    MessageUtil.formatMessage(
                            LifeMod.getInstance().getLangConfig().getString("freeze.gui.title")
                    )
            );
            freezeMenu.setItem(11, blackPane);
            freezeMenu.setItem(13, blackPane);
            freezeMenu.setItem(15, blackPane);
            player.openInventory(freezeMenu);

            debug.log("freeze", player.getName() + " opened Freeze GUI");
        } catch (Exception e) {
            debug.userError(player, "Error while opening Freeze GUI", e);
        }
    }

    public HashMap<UUID, Location> getFrozenPlayers() {
        return frozenPlayers;
    }
}