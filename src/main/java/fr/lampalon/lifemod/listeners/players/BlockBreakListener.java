package fr.lampalon.lifemod.listeners.players;

import fr.lampalon.lifemod.utils.OreTracker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    private final OreTracker oreTracker;

    public BlockBreakListener(OreTracker oreTracker) {
        this.oreTracker = oreTracker;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        oreTracker.incrementPlayerBlockCount(player);

        Material blockType = event.getBlock().getType();
        if (oreTracker.isOre(blockType)) {
            oreTracker.incrementPlayerOreCount(player);
        }
    }
}
