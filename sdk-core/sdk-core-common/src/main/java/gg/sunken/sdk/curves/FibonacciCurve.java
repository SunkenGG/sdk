package gg.sunken.sdk.curves;

public class FibonacciCurve extends AbstractCurve {
    public FibonacciCurve(double baseValue) {
        super(baseValue);
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        if (level == 1) return baseValue;
        if (level == 2) return baseValue * 2;
        
        double a = baseValue;
        double b = baseValue * 2;
        for (int i = 3; i <= level; i++) {
            double next = a + b;
            a = b;
            b = next;
        }
        return b;
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        if (value < baseValue) return 0;
        if (value < baseValue * 2) return 1;
        
        long level = 2;
        double a = baseValue;
        double b = baseValue * 2;
        while (b < value) {
            double next = a + b;
            a = b;
            b = next;
            level++;
        }
        return level;
    }
}