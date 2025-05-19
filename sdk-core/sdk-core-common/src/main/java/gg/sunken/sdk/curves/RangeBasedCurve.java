package gg.sunken.sdk.curves;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RangeBasedCurve extends AbstractCurve {
    private record CurveRange(long minLevel, long maxLevel, Curve curve) {

        boolean contains(long level) {
                return level >= minLevel && level <= maxLevel;
            }
    }

    private final List<CurveRange> ranges = new ArrayList<>();

    public RangeBasedCurve(double baseValue) {
        super(baseValue);
    }

    public void addRange(long minLevel, long maxLevel, Curve curve) {
        if (minLevel > maxLevel) throw new IllegalArgumentException("minLevel > maxLevel");
        ranges.add(new CurveRange(minLevel, maxLevel, curve));
        ranges.sort(Comparator.comparingLong(r -> r.minLevel));
    }

    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        for (CurveRange range : ranges) {
            if (range.contains(level)) {
                return range.curve.calculateValueForLevel(level - range.minLevel + 1);
            }
        }
        throw new IllegalStateException("No curve defined for level " + level);
    }

    @Override
    public long calculateLevelForValue(double value) {
        long offset = 0;
        for (CurveRange range : ranges) {
            long maxRangeLevels = range.maxLevel - range.minLevel + 1;
            double maxValue = range.curve.calculateValueForLevel(maxRangeLevels);
            if (value <= maxValue) {
                long localLevel = range.curve.calculateLevelForValue(value);
                return offset + localLevel;
            } else {
                value -= maxValue;
                offset += maxRangeLevels;
            }
        }
        throw new IllegalStateException("Value exceeds all defined ranges");
    }
}
