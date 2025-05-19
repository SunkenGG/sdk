package gg.sunken.sdk.curves;

public class HarmonicCurve extends AbstractCurve {
    public HarmonicCurve(double baseValue) {
        super(baseValue);
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        double sum = 0;
        for (int i = 1; i <= level; i++) {
            sum += 1.0 / i;
        }
        return baseValue * sum;
    }

    @Override
    public long calculateLevelForValue(double value) {
        double sum = 0;
        int level = 1;
        while (baseValue * sum < value) {
            sum += 1.0 / level;
            level++;
        }
        return Math.max(1, level - 1);
    }
}
