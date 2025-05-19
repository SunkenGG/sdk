package gg.sunken.sdk.curves;

public class StepCurve extends AbstractCurve {
    private final double[] thresholds;
    private final double[] values;
    
    public StepCurve(double[] thresholds, double[] values) {
        super(0); // Base value not used here
        if (thresholds.length != values.length) {
            throw new IllegalArgumentException("Thresholds and values must match in length");
        }
        this.thresholds = thresholds;
        this.values = values;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        for (int i = thresholds.length - 1; i >= 0; i--) {
            if (level >= thresholds[i]) {
                return values[i];
            }
        }
        return values[0];
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        for (int i = 0; i < values.length; i++) {
            if (value <= values[i]) {
                return (long) thresholds[i];
            }
        }
        return Long.MAX_VALUE;
    }
}