package fr.lampalon.lifemod.utils;

import fr.lampalon.lifemod.LifeMod;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("(&#|#)([A-Fa-f0-9]{6})");
    private static final Pattern RGB_PATTERN = Pattern.compile("rgb\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)");
    private static final String PREFIX_PLACEHOLDER = "%prefix%";

    public static String parseColors(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }

        message = message.replace("\\n", "\n");

        Matcher hexMatcher = HEX_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(2);
            hexMatcher.appendReplacement(sb, ChatColor.of("#" + hexColor).toString());
        }
        hexMatcher.appendTail(sb);
        message = sb.toString();

        Matcher rgbMatcher = RGB_PATTERN.matcher(message);
        sb = new StringBuffer();
        while (rgbMatcher.find()) {
            int r = Integer.parseInt(rgbMatcher.group(1));
            int g = Integer.parseInt(rgbMatcher.group(2));
            int b = Integer.parseInt(rgbMatcher.group(3));
            rgbMatcher.appendReplacement(sb, ChatColor.of(new Color(r, g, b)).toString());
        }
        rgbMatcher.appendTail(sb);
        message = sb.toString();

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatMessage(String message) {
        if (message == null) {
            return "";
        }

        String prefix = LifeMod.getInstance().getConfigConfig().getString("prefix", "");
        if (prefix == null) {
            prefix = "";
        }

        if (message.contains(PREFIX_PLACEHOLDER)) {
            message = message.replace(PREFIX_PLACEHOLDER, prefix);
        } else {
            message = prefix + message;
        }

        return parseColors(message);
    }
}
