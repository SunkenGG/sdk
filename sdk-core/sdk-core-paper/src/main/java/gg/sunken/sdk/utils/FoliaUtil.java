package gg.sunken.sdk.utils;

import gg.sunken.sdk.obj.Lazy;

public class FoliaUtil {

    public final static Lazy<Boolean> FOLIA = Lazy.of(() -> {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    });

    public static boolean isFolia() {
        return FOLIA.get();
    }
}
