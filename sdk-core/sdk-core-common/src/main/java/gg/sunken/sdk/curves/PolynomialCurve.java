package gg.sunken.sdk.curves;

public class PolynomialCurve extends AbstractCurve {
    private final double exponent;
    
    public PolynomialCurve(double baseValue, double exponent) {
        super(baseValue);
        this.exponent = exponent;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue * Math.pow(level, exponent);
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        if (value < baseValue) return 0;
        return (long) Math.pow(value / baseValue, 1.0/exponent);
    }
}