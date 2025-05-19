package gg.sunken.sdk.features;

import org.bukkit.event.Listener;

public interface PaperFeature extends Feature, Listener {

    default void load() {}

    default void tick(long tick) {}
}
