package gg.sunken.sdk.curves;

public interface Curve {

    double calculateValueForLevel(long level);

    long calculateLevelForValue(double value);

    default double calculateNeededValue(long level) {
        return calculateValueForLevel(level+1) - calculateValueForLevel(level);
    }
}