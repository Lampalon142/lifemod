package fr.lampalon.lifemod.utils;

import fr.lampalon.lifemod.LifeMod;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class Update {
    private final LifeMod plugin;
    private final int resourceId;
    public Update(LifeMod plugin, int resourceId){
        this.plugin = plugin;
        this.resourceId = resourceId;
    }
    public void getLatestVersion(Consumer<String> consumer){
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
             try (InputStream in = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream();
             Scanner scan = new Scanner(in)) {
                 if (scan.hasNext()) {
                     consumer.accept(scan.next());
                 }
             } catch (IOException exception){
                 plugin.getLogger().info("Update checker is broken, can't find an update!" + exception.getMessage());
             }
        });
    }
}
