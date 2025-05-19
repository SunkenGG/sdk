package gg.sunken.sdk.curves;

public class LogPolynomialCurve extends AbstractCurve {
    private final double exponent;
    private final double logOffset;

    public LogPolynomialCurve(double baseValue, double exponent, double logOffset) {
        super(baseValue);
        this.exponent = exponent;
        this.logOffset = logOffset;
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue * Math.pow(level, exponent) * Math.log(level + logOffset);
    }

    @Override
    public long calculateLevelForValue(double value) {
        // Approximate using binary search
        long low = 1, high = 1_000_000;
        while (low < high) {
            long mid = (low + high) / 2;
            double v = calculateValueForLevel(mid);
            if (v < value) low = mid + 1;
            else high = mid;
        }
        return low;
    }
}
