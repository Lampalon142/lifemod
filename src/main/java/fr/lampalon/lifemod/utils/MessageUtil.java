package fr.lampalon.lifemod.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    public static String parseColors(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }

        message = message.replace("\\n", "\n");

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
