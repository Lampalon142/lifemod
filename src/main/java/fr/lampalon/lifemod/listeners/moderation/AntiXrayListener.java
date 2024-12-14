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
        obfuscateChunksAroundPlayer(player, 3); // Rayon de 3 chunks autour du joueur
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Vérifie si le bloc est un leurre (obfusqué)
        if (antiXray.isObfuscated(block)) {
            Material originalMaterial = antiXray.originalBlocks.get(block); // Récupère le matériau original

            // Annule les drops par défaut (empêche le leurre de dropper ses items)
            event.setDropItems(false);

            // Remplace immédiatement le bloc par l'original (ou rien, selon le comportement attendu)
            block.setType(originalMaterial);

            // Optionnel : Simule le drop du matériau d'origine
            if (originalMaterial != Material.STONE) { // Exemple : On ne droppe pas pour la pierre
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(originalMaterial));
            }

            // Supprime l'entrée du bloc obfusqué de la mémoire
            antiXray.originalBlocks.remove(block);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // Vérifie si le joueur a changé de chunk
        if (from.getChunk().getX() != to.getChunk().getX() || from.getChunk().getZ() != to.getChunk().getZ()) {
            Chunk currentChunk = to.getChunk();

            // Vérifie si le chunk actuel est déjà obfusqué
            if (!lastPlayerChunks.containsKey(player.getUniqueId()) || !lastPlayerChunks.get(player.getUniqueId()).equals(currentChunk)) {
                lastPlayerChunks.put(player.getUniqueId(), currentChunk);
                obfuscateChunksAroundPlayer(player, 3); // Obfusque les chunks dans un rayon de 3
            }
        }
    }

    private void obfuscateChunksAroundPlayer(Player player, int radius) {
        Chunk centerChunk = player.getLocation().getChunk();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk chunk = centerChunk.getWorld().getChunkAt(centerChunk.getX() + dx, centerChunk.getZ() + dz);
                antiXray.obfuscateRegion(player, chunk); // Appelle la méthode d'obfuscation
            }
        }
    }
}