package gg.sunken.sdk.curves;

public class CurveFactory {
    public static Curve createLinear(double baseValue, double increment) {
        return new LinearCurve(baseValue, increment);
    }
    
    public static Curve createExponential(double baseValue, double growthRate) {
        return new ExponentialCurve(baseValue, growthRate);
    }
    
    public static Curve createPiecewise(double baseValue, double[] thresholds, double[] multipliers) {
        return new PiecewiseCurve(baseValue, thresholds, multipliers);
    }
    
    public static Curve createSoftcap(double baseValue, double softcapStart, double postMultiplier) {
        return new SoftcapCurve(baseValue, softcapStart, postMultiplier);
    }

    public static Curve createLogarithmic(double baseValue, double scale) {
        return new LogarithmicCurve(baseValue, scale);
    }

    public static Curve createPolynomial(double baseValue, double exponent) {
        return new PolynomialCurve(baseValue, exponent);
    }

    public static Curve createStep(double[] thresholds, double[] values) {
        return new StepCurve(thresholds, values);
    }

    public static Curve createSigmoid(double baseValue, double steepness, double midpoint) {
        return new SigmoidCurve(baseValue, steepness, midpoint);
    }

    public static Curve createFibonacci(double baseValue) {
        return new FibonacciCurve(baseValue);
    }

    public static Curve createCustom(double baseValue, double[] coefficients) {
        return new CustomCurve(baseValue, coefficients);
    }

    public static Curve createTriangular(double baseValue) {
        return new TriangularCurve(baseValue);
    }
}