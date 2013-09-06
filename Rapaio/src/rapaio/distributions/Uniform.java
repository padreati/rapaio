package rapaio.distributions;

import static rapaio.core.BaseMath.pow;

/**
 * @author tutuianu
 */
public class Uniform extends Distribution {

    private final double a;
    private final double b;

    public Uniform(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    @Override
    public String getName() {
        return "Continuous Uniform Distribution";
    }

    @Override
    public double pdf(double x) {
        if (x < a || x > b) {
            return 0;
        }
        if (a == b) {
            return 0;
        }
        return 1 / (b - a);
    }

    @Override
    public double cdf(double x) {
        if (x < a) {
            return 0;
        }
        if (x > b) {
            return 1;
        }
        return (x - a) / (b - a);
    }

    @Override
    public double quantile(double p) {
        if (p < 0 || p > 1) {
            throw new ArithmeticException("probability getValue should lie in [0,1] interval");
        }
        return a + p * (b - a);
    }

    @Override
    public double min() {
        return a;
    }

    @Override
    public double max() {
        return b;
    }

    @Override
    public double mean() {
        return a + (b - a) / 2.;
    }

    @Override
    public double mode() {
        return mean();
    }

    @Override
    public double variance() {
        return pow(b - a, 2) / 12.;
    }

    @Override
    public double skewness() {
        return 0;
    }

    @Override
    public double kurtosis() {
        return -6. / 5.;
    }
}
