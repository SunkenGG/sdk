package gg.sunken.sdk.utils;

import org.bukkit.entity.Player;

import java.util.UUID;

public class GeyserUtil {

    public static boolean isBedrock(Player player) {
        return isBedrock(player.getUniqueId());
    }

    public static boolean isBedrock(UUID uuid) {
        return uuid.getMostSignificantBits() == 0;
    }
}
