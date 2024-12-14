package fr.lampalon.lifemod.utils;

import fr.lampalon.lifemod.LifeMod;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AntiXray {

    private final LifeMod plugin;
    private final List<String> rareOres;
    private final List<String> fakeBlocks;
    private final Random random = new Random();

    // Map pour sauvegarder les blocs originaux
    public final Map<Block, Material> originalBlocks = new HashMap<>();

    public AntiXray(LifeMod plugin) {
        this.plugin = plugin;

        // Chargement de la configuration
        FileConfiguration config = plugin.getConfig();
        rareOres = config.getStringList("rare-ores");
        fakeBlocks = config.getStringList("fake-blocks");
    }

    public void obfuscateRegion(Player player, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    Material blockType = block.getType();

                    // Si c'est un minerai rare, le remplacer par de la pierre
                    if (isRareOre(blockType)) {
                        originalBlocks.put(block, blockType); // Sauvegarde le bloc original
                        block.setType(Material.STONE); // Cache le minerai
                    }
                    else if (random.nextDouble() < 0.1) { // 10% de chances d'ajouter un leurre
                        if (isBlockNatural(block)) { // Vérifie si le bloc est naturel (ex. pierre)
                            originalBlocks.put(block, blockType); // Sauvegarde l'original
                            block.setType(getRandomFakeBlock()); // Place un bloc de leurre
                        }
                    }
                }
            }
        }
    }

    private boolean isBlockNatural(Block block) {
        // Vérifie si le bloc fait partie de l'environnement naturel
        Material type = block.getType();
        return type == Material.STONE || type == Material.DEEPSLATE || type == Material.DIRT || type == Material.GRAVEL;
    }

    public void restoreBlock(Block block) {
        if (originalBlocks.containsKey(block)) {
            Material originalMaterial = originalBlocks.get(block);
            block.setType(originalMaterial); // Remet le bloc d'origine
            originalBlocks.remove(block); // Supprime l'entrée une fois restauré
        }
    }

    private boolean isRareOre(Material material) {
        return rareOres.contains(material.name());
    }

    private Material getRandomFakeBlock() {
        String blockName = fakeBlocks.get(random.nextInt(fakeBlocks.size()));
        return Material.valueOf(blockName);
    }

    public boolean isObfuscated(Block block) {
        return originalBlocks.containsKey(block);
    }
}