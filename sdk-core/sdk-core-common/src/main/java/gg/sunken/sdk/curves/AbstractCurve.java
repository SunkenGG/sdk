package gg.sunken.sdk.curves;

public abstract class AbstractCurve implements Curve {
    protected final double baseValue;
    
    protected AbstractCurve(double baseValue) {
        this.baseValue = baseValue;
        if (baseValue < 0) throw new IllegalArgumentException("Base value must be ≥ 0");
    }
    
    protected void validateLevel(long level) {
        if (level < 1) throw new IllegalArgumentException("Level must be ≥ 1");
    }

    protected double incomingValue(double value) {
        if (value < 0) throw new IllegalArgumentException("Value must be ≥ 0");
        return 1;
    }
}