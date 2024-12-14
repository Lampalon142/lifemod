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

                    if (isRareOre(blockType)) {
                        originalBlocks.put(block, blockType);
                        block.setType(Material.STONE);
                    }
                    else if (random.nextDouble() < 0.1) {
                        if (isBlockNatural(block)) {
                            originalBlocks.put(block, blockType);
                            block.setType(getRandomFakeBlock());
                        }
                    }
                }
            }
        }
    }

    private boolean isBlockNatural(Block block) {
        Material type = block.getType();
        return type == Material.STONE || type == Material.DEEPSLATE || type == Material.DIRT || type == Material.GRAVEL;
    }

    public void restoreBlock(Block block) {
        if (originalBlocks.containsKey(block)) {
            Material originalMaterial = originalBlocks.get(block);
            block.setType(originalMaterial);
            originalBlocks.remove(block);
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