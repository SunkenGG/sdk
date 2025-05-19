package gg.sunken.sdk.curves;

public class PeriodicCurve extends AbstractCurve {
    private final double amplitude, frequency;

    public PeriodicCurve(double baseValue, double amplitude, double frequency) {
        super(baseValue);
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        return baseValue + amplitude * Math.sin(frequency * level);
    }

    @Override
    public long calculateLevelForValue(double value) {
        throw new UnsupportedOperationException("Inverse not well-defined for periodic curves");
    }
}
