package gg.sunken.sdk.curves;

public class ExponentialCurve extends AbstractCurve {
    private final double growthRate;
    
    public ExponentialCurve(double baseValue, double growthRate) {
        super(baseValue);
        if (growthRate <= 1) throw new IllegalArgumentException("Growth rate must be > 1");
        this.growthRate = growthRate;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue * (Math.pow(growthRate, level) - 1) / (growthRate - 1);
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        if (value < baseValue) return 0;
        return (long) (Math.log(value * (growthRate - 1) / baseValue + 1) / Math.log(growthRate));
    }
}