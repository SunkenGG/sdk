package gg.sunken.sdk.features;

public interface Feature extends Comparable<Feature> {

    default void enable() {};

    default void disable() {}
}
