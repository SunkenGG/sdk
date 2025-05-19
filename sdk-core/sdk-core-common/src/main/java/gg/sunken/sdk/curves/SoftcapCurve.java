package gg.sunken.sdk.curves;

public class SoftcapCurve extends AbstractCurve {
    private final double softcapStart;
    private final double postCapMultiplier;
    
    public SoftcapCurve(double baseValue, double softcapStart, double postCapMultiplier) {
        super(baseValue);
        this.softcapStart = softcapStart;
        this.postCapMultiplier = postCapMultiplier;
    }
    
    @Override
    public double calculateValueForLevel(long level) {
        validateLevel(level);
        if (level <= softcapStart) {
            return baseValue * level;
        }
        return baseValue * softcapStart + 
               baseValue * postCapMultiplier * (level - softcapStart);
    }
    
    @Override
    public long calculateLevelForValue(double value) {
        double maxPreCapValue = baseValue * softcapStart;
        if (value <= maxPreCapValue) {
            return (long) (value / baseValue);
        }
        return (long) (softcapStart + (value - maxPreCapValue) / (baseValue * postCapMultiplier));
    }
}