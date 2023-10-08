package fr.lampalon.lifemod.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaUtils {
    public static boolean isValidEnum(Class<Enum> enumClass, String enumName) {
        if (enumClass == null || enumName == null)
            return false;
        try {
            Enum.valueOf(enumClass, enumName);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static boolean isInteger(String string) {
        boolean isInteger = true;
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException exception) {
            isInteger = false;
        }
        return isInteger;
    }

    public static List<String> stringToList(String commas) {
        if (commas == null)
            throw new IllegalArgumentException("Commas may not be null.");
        return new ArrayList<>(Arrays.asList(commas.split("\\s*,\\s*")));
    }

    public static String insertCommas(String string) {
        StringBuilder builder = new StringBuilder();
        String[] words = string.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String suffix = ",";
            if (i + 1 == words.length)
                suffix = "";
            builder.append(word).append(suffix);
        }
        return builder.toString();
    }

    public static String compileWords(String[] args, int index) {
        StringBuilder builder = new StringBuilder();
        for (int i = index; i < args.length; i++)
            builder.append(args[i]).append(" ");
        return builder.toString().trim();
    }

    public static void reverse(Object[] array) {
        if (array != null) {
            int i = 0;
            for (int j = array.length - 1; j > i; i++) {
                Object tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                j--;
            }
        }
    }

    public static int getItemSlot(PlayerInventory inventory, ItemStack item) {
        int slot = 0;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack current = contents[i];
            if (current != null)
                if (current.equals(item)) {
                    slot = i;
                    break;
                }
        }
        return slot;
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }

    public static String serializeLocation(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    public static String formatTypeName(Material type) {
        return type.name().replace("_", " ").toLowerCase();
    }

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public static Player getTargetPlayer(Player player) {
        Location location = player.getLocation();
        Player targetPlayer = null;
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != p.getWorld())
                continue;
            if (location.distanceSquared(p.getLocation()) > 36.0D || player.getName().equals(p.getName()))
                continue;
            Vector targetVector = p.getLocation().toVector().subtract(location.toVector()).normalize();
            if (Math.round(targetVector.dot(location.getDirection())) == 1L) {
                targetPlayer = p;
                break;
            }
        }
        return targetPlayer;
    }

    public static boolean hasInventorySpace(Player player) {
        return (player.getInventory().firstEmpty() != -1);
    }

    public static int parseMcVer(String ver) {
        return Integer.parseInt(ver.split("\\.")[1].replaceAll("[^0-9]", ""));
    }

    public static Vector makeVelocitySafe(Vector velocity) {
        while (velocity.getX() > 4.0D)
            velocity.setX(velocity.getX() - 0.5D);
        while (velocity.getY() > 4.0D)
            velocity.setY(velocity.getY() - 0.5D);
        while (velocity.getZ() > 4.0D)
            velocity.setZ(velocity.getZ() - 0.5D);
        while (velocity.getX() < -4.0D)
            velocity.setX(velocity.getX() + 0.5D);
        while (velocity.getY() < -4.0D)
            velocity.setY(velocity.getY() + 0.5D);
        while (velocity.getZ() < -4.0D)
            velocity.setZ(velocity.getZ() + 0.5D);
        return velocity;
    }
}
