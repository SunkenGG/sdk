package gg.sunken.sdk.curves;

public class CustomCurve extends AbstractCurve {

    private final double[] coefficients;

    public CustomCurve(double baseValue, double[] coefficients) {
        super(baseValue);
        if (coefficients == null || coefficients.length == 0) {
            throw new IllegalArgumentException("Coefficients must not be null or empty");
        }
        if (baseValue < 0) {
            throw new IllegalArgumentException("Base value must be non-negative");
        }
        this.coefficients = coefficients;
    }


    @Override
    public double calculateValueForLevel(long level) {
        if (level < 0) {
            throw new IllegalArgumentException("Level must be non-negative");
        }

        double value = baseValue;
        for (int i = 0; i < coefficients.length; i++) {
            value += coefficients[i] * Math.pow(level, i);
        }

        return value;
    }

    @Override
    public long calculateLevelForValue(double value) {
        if (value < baseValue) {
            return 0;
        }

        long level = 0;

        while (calculateValueForLevel(level) < value) {
            level++;
        }

        return level;
    }
}
