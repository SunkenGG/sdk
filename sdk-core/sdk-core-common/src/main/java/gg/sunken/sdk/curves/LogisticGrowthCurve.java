package gg.sunken.sdk.curves;

public class LogisticGrowthCurve extends AbstractCurve {
    private final double L, k, x0;

    public LogisticGrowthCurve(double baseValue, double L, double k, double x0) {
        super(baseValue);
        this.L = L;
        this.k = k;
        this.x0 = x0;
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue + L / (1 + Math.exp(-k * (level - x0)));
    }

    @Override
    public long calculateLevelForValue(double value) {
        double v = value - baseValue;
        return (long) Math.max(1, Math.floor(x0 - Math.log((L / v) - 1) / k));
    }
}
