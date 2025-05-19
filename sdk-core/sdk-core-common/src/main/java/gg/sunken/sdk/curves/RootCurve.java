package gg.sunken.sdk.curves;

public class RootCurve extends AbstractCurve {
    public RootCurve(double baseValue) {
        super(baseValue);
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue * Math.sqrt(level);
    }

    @Override
    public long calculateLevelForValue(double value) {
        return (long) Math.max(1, Math.floor(Math.pow(value / baseValue, 2)));
    }
}
