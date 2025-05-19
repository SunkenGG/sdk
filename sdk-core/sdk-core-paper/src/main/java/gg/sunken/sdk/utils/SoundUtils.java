package gg.sunken.sdk.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SoundUtils {
    /**
     * Play a sound for a player based off of the distance between the player and the location.
     *
     * @param location The location to play the sound at.
     * @param sound    The sound to play.
     * @param range    The range to play the sound within.
     */
    public static void playSoundWithinRange(Location location, String sound, int range) {
        for (Player nearbyPlayer : location.getNearbyPlayers(range)) {
            nearbyPlayer.playSound(nearbyPlayer.getLocation(), sound, convertForSound((float) nearbyPlayer.getLocation().distance(location), range), 1);
        }
    }

    private static float convertForSound(float x, int range) {
        return Math.max(0, 1 - (x / range));
    }
}
