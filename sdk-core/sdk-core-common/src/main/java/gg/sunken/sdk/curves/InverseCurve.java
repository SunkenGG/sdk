package gg.sunken.sdk.curves;

public class InverseCurve extends AbstractCurve {
    private final double offset;

    public InverseCurve(double baseValue, double offset) {
        super(baseValue);
        this.offset = offset;
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue / (level + offset);
    }

    @Override
    public long calculateLevelForValue(double value) {
        return (long) Math.max(1, Math.floor((baseValue / value) - offset));
    }
}
