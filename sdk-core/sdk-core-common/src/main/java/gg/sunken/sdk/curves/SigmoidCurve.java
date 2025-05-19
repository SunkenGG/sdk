package gg.sunken.sdk.curves;

public class SigmoidCurve extends AbstractCurve {
    private final double steepness;
    private final double midpoint;
    
    public SigmoidCurve(double baseValue, double steepness, double midpoint) {
        super(baseValue);
        this.steepness = steepness;
        this.midpoint = midpoint;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue / (1 + Math.exp(-steepness * (level - midpoint)));
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        if (value <= 0) return 0;
        if (value >= baseValue) return Long.MAX_VALUE;
        return (long) (midpoint - Math.log(baseValue/value - 1)/steepness);
    }
}