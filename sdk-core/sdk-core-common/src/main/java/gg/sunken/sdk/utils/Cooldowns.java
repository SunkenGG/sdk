package gg.sunken.sdk.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import gg.sunken.sdk.cache.Cache;
import gg.sunken.sdk.cache.CaffeineCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Cooldowns {
    private static final Map<String, Cache<UUID, Long>> cooldowns = new HashMap<>();

    public static void cooldown(String reason, UUID uuid, long duration) {
        if (reason == null || uuid == null) {
            throw new IllegalArgumentException("Reason and UUID cannot be null");
        }

        if (!cooldowns.containsKey(reason)) {
            cooldowns.put(reason, new CaffeineCache<>(
                    Caffeine.newBuilder()
                            .evictionListener((key, value, cause) -> {
                                if ((Long) value > System.currentTimeMillis()) {
                                    cooldowns.get(reason).put(uuid, (Long) value);
                                }
                            })
                            .expireAfterAccess(30, TimeUnit.SECONDS)
            ));
        }

        Cache<UUID, Long> cache = cooldowns.get(reason);
        if (cache == null) {
            throw new IllegalStateException("Cooldown cache for reason '" + reason + "' is not initialized");
        }

        long expirationTime = System.currentTimeMillis() + duration;
        cache.put(uuid, expirationTime);
    }

    public static boolean isCooldown(String reason, UUID uuid) {
        if (reason == null || uuid == null) {
            throw new IllegalArgumentException("Reason and UUID cannot be null");
        }

        Cache<UUID, Long> cache = cooldowns.get(reason);
        if (cache == null) {
            return false;
        }

        Long expirationTime = cache.getIfPresent(uuid);
        if (expirationTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expirationTime) {
            cache.invalidate(uuid);
            return false;
        }

        return true;
    }
}
