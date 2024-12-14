package fr.lampalon.lifemod.listeners.moderation;

import fr.lampalon.lifemod.utils.AntiXray;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiXrayListener implements Listener {

    private final AntiXray antiXray;
    private final Map<UUID, Chunk> lastPlayerChunks = new HashMap<>();

    public AntiXrayListener(AntiXray antiXray) {
        this.antiXray = antiXray;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        obfuscateChunksAroundPlayer(player, 3);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (antiXray.isObfuscated(block)) {
            Material originalMaterial = antiXray.originalBlocks.get(block);

            event.setDropItems(false);

            block.setType(originalMaterial);

            if (originalMaterial != Material.STONE) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(originalMaterial));
            }

            antiXray.originalBlocks.remove(block);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getChunk().getX() != to.getChunk().getX() || from.getChunk().getZ() != to.getChunk().getZ()) {
            Chunk currentChunk = to.getChunk();

            if (!lastPlayerChunks.containsKey(player.getUniqueId()) || !lastPlayerChunks.get(player.getUniqueId()).equals(currentChunk)) {
                lastPlayerChunks.put(player.getUniqueId(), currentChunk);
                obfuscateChunksAroundPlayer(player, 3);
            }
        }
    }

    private void obfuscateChunksAroundPlayer(Player player, int radius) {
        Chunk centerChunk = player.getLocation().getChunk();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk chunk = centerChunk.getWorld().getChunkAt(centerChunk.getX() + dx, centerChunk.getZ() + dz);
                antiXray.obfuscateRegion(player, chunk);
            }
        }
    }
}