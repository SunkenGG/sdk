package gg.sunken.sdk.curves;

public class SpikeCurve extends AbstractCurve {
    private final int spikeEvery;
    private final double spikeMultiplier;

    public SpikeCurve(double baseValue, int spikeEvery, double spikeMultiplier) {
        super(baseValue);
        this.spikeEvery = spikeEvery;
        this.spikeMultiplier = spikeMultiplier;
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        double value = baseValue * level * level;
        if (level % spikeEvery == 0) {
            value *= spikeMultiplier;
        }
        return value;
    }

    @Override
    public long calculateLevelForValue(double value) {
        long level = 0;
        double currentValue = baseValue * level * level;
        while (currentValue < value) {
            level++;
            currentValue = baseValue * level * level;
            if (level % spikeEvery == 0) {
                currentValue *= spikeMultiplier;
            }
        }

        return level - 1;
    }
}
