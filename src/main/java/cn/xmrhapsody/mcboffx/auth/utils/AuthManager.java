package cn.xmrhapsody.mcboffx.auth.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthManager {
    
    private static final Set<UUID> authenticatedPlayers = new HashSet<>();
    
    public static boolean isAuthenticated(UUID uuid) {
        return authenticatedPlayers.contains(uuid);
    }
    
    public static void authenticate(UUID uuid) {
        authenticatedPlayers.add(uuid);
    }
    
    public static void deauthenticate(UUID uuid) {
        authenticatedPlayers.remove(uuid);
    }
} 