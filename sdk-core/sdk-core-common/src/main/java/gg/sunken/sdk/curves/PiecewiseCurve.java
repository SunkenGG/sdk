package gg.sunken.sdk.curves;

public class PiecewiseCurve extends AbstractCurve {
    private final double[] thresholds;
    private final double[] multipliers;
    
    public PiecewiseCurve(double baseValue, double[] thresholds, double[] multipliers) {
        super(baseValue);
        if (thresholds.length != multipliers.length) {
            throw new IllegalArgumentException("Thresholds and multipliers arrays must match in length");
        }
        this.thresholds = thresholds;
        this.multipliers = multipliers;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        double total = 0;
        double prevThreshold = 0;
        double currentMultiplier = 1.0;
        
        for (int i = 0; i <= thresholds.length; i++) {
            double nextThreshold = (i < thresholds.length) ? thresholds[i] : Long.MAX_VALUE;
            currentMultiplier = (i > 0) ? multipliers[i-1] : currentMultiplier;
            
            if (level > prevThreshold) {
                long levelsInSegment = Math.min(level, (long) nextThreshold) - (long) prevThreshold;
                total += baseValue * currentMultiplier * levelsInSegment;
            }
            
            prevThreshold = nextThreshold;
            if (level <= nextThreshold) break;
        }
        return total;
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }

        double total = 0;
        long level = 0;
        double prevThreshold = 0;
        double currentMultiplier = 1.0;

        for (int i = 0; i <= thresholds.length; i++) {
            double nextThreshold = (i < thresholds.length) ? thresholds[i] : Long.MAX_VALUE;
            currentMultiplier = (i > 0) ? multipliers[i-1] : currentMultiplier;

            if (total >= value) {
                break;
            }

            long levelsInSegment = (long) (Math.min(nextThreshold, Long.MAX_VALUE) - (long) prevThreshold);
            double segmentValue = baseValue * currentMultiplier * levelsInSegment;

            if (total + segmentValue >= value) {
                level += (long) ((value - total) / (baseValue * currentMultiplier));
                break;
            }

            total += segmentValue;
            level += levelsInSegment;
            prevThreshold = nextThreshold;
        }

        return level;
    }
}