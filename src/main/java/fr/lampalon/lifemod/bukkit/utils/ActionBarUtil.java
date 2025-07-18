package fr.lampalon.lifemod.bukkit.utils;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ActionBarUtil {

    public static void sendActionBar(Player player, String message) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Method getHandle = craftPlayerClass.getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(craftPlayer);

            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + getVersion() + ".PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + getVersion() + ".IChatBaseComponent");
            Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + getVersion() + ".IChatBaseComponent$ChatSerializer");

            Method aMethod = chatSerializerClass.getMethod("a", String.class);
            Object chatBaseComponent = aMethod.invoke(null, "{\"text\":\"" + message + "\"}");

            Constructor<?> packetConstructor = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, byte.class);
            Object packet = packetConstructor.newInstance(chatBaseComponent, (byte) 2);

            Method playerConnectionGet = entityPlayer.getClass().getField("playerConnection").get(entityPlayer).getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + getVersion() + ".Packet"));
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

            playerConnectionGet.invoke(playerConnection, packet);

        } catch (Exception e) {
            // Fallback simple (envoi dans le chat)
            player.sendMessage(message);
        }
    }

    private static String getVersion() {
        String name = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
