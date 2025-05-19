package gg.sunken.sdk.curves;

public class LogarithmicCurve extends AbstractCurve {
    private final double scale;
    
    public LogarithmicCurve(double baseValue, double scale) {
        super(baseValue);
        this.scale = scale;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue * Math.log(scale * level);
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        if (value < baseValue) return 0;
        return (long) ((long) Math.exp(value / baseValue) / scale);
    }
}