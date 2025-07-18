package fr.lampalon.lifemod.bukkit.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModeratorSessionManager {

    private final Set<UUID> authenticated = new HashSet<>();
    private final java.util.Map<UUID, Integer> attempts = new java.util.HashMap<>();
    private final java.util.Set<UUID> locked = new java.util.HashSet<>();

    private final int maxAttempts;

    public ModeratorSessionManager(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean isAuthenticated(UUID uuid) {
        return authenticated.contains(uuid);
    }

    public void authenticate(UUID uuid) {
        authenticated.add(uuid);
        attempts.remove(uuid);
        locked.remove(uuid);
    }

    public void logout(UUID uuid) {
        authenticated.remove(uuid);
        attempts.remove(uuid);
        locked.remove(uuid);
    }

    public int decrementAttempts(UUID uuid) {
        int left = attempts.getOrDefault(uuid, maxAttempts);
        left--;
        attempts.put(uuid, left);
        return left;
    }

    public void resetAttempts(UUID uuid) {
        attempts.remove(uuid);
    }

    public void lock(UUID uuid) {
        locked.add(uuid);
    }

    public boolean isLocked(UUID uuid) {
        return locked.contains(uuid);
    }
}
