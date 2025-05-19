package gg.sunken.sdk.curves;

public class TriangularCurve extends AbstractCurve {

    public TriangularCurve(double baseValue) {
        super(baseValue);
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue * (level * (level + 1)) / 2.0;
    }

    @Override
    public long calculateLevelForValue(double value) {
        // Inverse of total XP formula: n(n+1)/2 = x
        // Solve: n^2 + n - (2x/baseValue) = 0
        double discriminant = Math.sqrt(1 + 8 * value / baseValue);
        long level = (long) Math.floor((-1 + discriminant) / 2.0);
        return Math.max(1, level);
    }

    @Override
    protected double incomingValue(double value) {
        return calculateLevelForValue(value); // please just cache the level & use that instead of the value
    }
}
