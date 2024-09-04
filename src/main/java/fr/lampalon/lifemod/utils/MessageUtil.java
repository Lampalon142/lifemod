package fr.lampalon.lifemod.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    /**public static boolean isVersionAbove(String version) {
        String[] serverVersion = Bukkit.getVersion().split(" ")[0].split("\\.");
        String[] targetVersion = version.split("\\.");

        for (int i = 0; i < Math.min(serverVersion.length, targetVersion.length); i++) {
            int serverPart = Integer.parseInt(serverVersion[i]);
            int targetPart = Integer.parseInt(targetVersion[i]);

            if (serverPart < targetPart) {
                return false;
            } else if (serverPart > targetPart) {
                return true;
            }
        }
        return true;
    }*/
    public static String parseColors(String message) {
        Pattern hexColorPattern = Pattern.compile("#[a-fA-F0-9]{6}");

        Matcher matcher = hexColorPattern.matcher(message);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, ChatColor.of(matcher.group()).toString());
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}
