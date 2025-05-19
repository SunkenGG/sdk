package gg.sunken.sdk.curves;

public class LinearCurve extends AbstractCurve {
    private final double increment;
    
    public LinearCurve(double baseValue, double increment) {
        super(baseValue);
        this.increment = increment;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue + (level - 1) * increment;
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        if (value < baseValue) return 0;
        return (long) ((value - baseValue) / increment) + 1;
    }
}