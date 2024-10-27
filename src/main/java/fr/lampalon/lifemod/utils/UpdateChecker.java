package fr.lampalon.lifemod.utils;

import fr.lampalon.lifemod.LifeMod;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    private final LifeMod plugin;
    private final int resourceId;
    private String currentVersionString;
    private String latestVersionString;
    private UpdateCheckResult updateCheckResult;

    public UpdateChecker(LifeMod plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.currentVersionString = plugin.getDescription().getVersion();
    }

    public void checkForUpdates(Consumer<UpdateCheckResult> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            latestVersionString = getLatestVersion();

            if (latestVersionString == null) {
                updateCheckResult = UpdateCheckResult.NO_RESULT;
            } else {
                int comparison = compareVersions(currentVersionString, latestVersionString);

                if (comparison < 0) {
                    updateCheckResult = UpdateCheckResult.OUT_DATED;
                } else if (comparison == 0) {
                    updateCheckResult = UpdateCheckResult.UP_TO_DATE;
                } else {
                    updateCheckResult = UpdateCheckResult.UNRELEASED;
                }
            }

            consumer.accept(updateCheckResult);
        });
    }


    public String getLatestVersion() {
        String version = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            version = reader.readLine();

        } catch (IOException e) {
            plugin.getLogger().warning("Error while fetching the latest version: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    plugin.getLogger().warning("Error while closing the reader: " + e.getMessage());
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return version;
    }

    public enum UpdateCheckResult {
        NO_RESULT, OUT_DATED, UP_TO_DATE, UNRELEASED,
    }

    private int compareVersions(String version1, String version2) {
        int[] v1Parts = extractVersionParts(version1);
        int[] v2Parts = extractVersionParts(version2);

        for (int i = 0; i < Math.max(v1Parts.length, v2Parts.length); i++) {
            int v1 = i < v1Parts.length ? v1Parts[i] : 0;
            int v2 = i < v2Parts.length ? v2Parts[i] : 0;

            if (v1 < v2) return -1;
            if (v1 > v2) return 1;
        }

        return 0;
    }

    private int[] extractVersionParts(String version) {
        return Arrays.stream(version.split("\\D+"))
                .filter(part -> !part.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }


    private int extractVersion(String version){
        StringBuilder numericPart = new StringBuilder();
        for (char c : version.toCharArray()){
            if (Character.isDigit(c)){
                numericPart.append(c);
            } else {
                break;
            }
        }

        return numericPart.length() > 0 ? Integer.parseInt(numericPart.toString()) : 0;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getCurrentVersionS() {
        return currentVersionString;
    }

    public String getLatestVersionS() {
        return latestVersionString;
    }

    public UpdateCheckResult getUpdateCheck() {
        return updateCheckResult;
    }
}
