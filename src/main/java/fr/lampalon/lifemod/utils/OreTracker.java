package fr.lampalon.lifemod.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreTracker {
    private final Map<String, Integer> playerOreCount = new HashMap<>();
    private Map<String, Integer> playerBlockCount = new HashMap<>();
    private List<String> customOres;

    public OreTracker(FileConfiguration config) {
        this.customOres = config.getStringList("ores-detected");
    }

    public void incrementPlayerOreCount(Player player) {
        String playerName = player.getName();
        int count = playerOreCount.getOrDefault(playerName, 0);
        playerOreCount.put(playerName, count + 1);
    }

    /**
     * Increments the block count for the specified player.
     * @param player
     */
    public void incrementPlayerBlockCount(Player player) {
        String playerName = player.getName();
        int count = playerBlockCount.getOrDefault(playerName, 0);
        playerBlockCount.put(playerName, count + 1);
    }

    public double getPlayerPercentage(Player player) {
        String playerName = player.getName();
        int oreCount = playerOreCount.getOrDefault(playerName, 0);
        int blockCount = playerBlockCount.getOrDefault(playerName, 0);

        if (blockCount == 0) {
            return 0;
        }

        double percentage = ((double) oreCount / blockCount) * 100;

        System.out.println("Percentage for " + playerName + ": " + percentage);

        return percentage;
    }


    public boolean isOre(Material material) {
        return customOres != null && customOres.contains(material.toString());
    }
}
