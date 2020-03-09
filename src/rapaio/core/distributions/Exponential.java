package rapaio.core.distributions;

import rapaio.printer.Format;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/19/20.
 */
public class Exponential implements Distribution {

    public static Exponential of(double lambda) {
        return new Exponential(lambda);
    }

    private static final long serialVersionUID = 5064238118800143270L;

    private final double lambda;

    private Exponential(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public String name() {
        return "Exponential(lambda=" + Format.floatFlex(lambda) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        if (x < 0) {
            return 0;
        }
        return lambda * Math.exp(-lambda * x);
    }

    @Override
    public double cdf(double x) {
        if (x < 0) {
            return 0;
        }
        return 1 - Math.exp(-lambda * x);
    }

    @Override
    public double quantile(double p) {
        if (p < 0) {
            return 0;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return -Math.log(1 - p) / lambda;
    }

    @Override
    public double min() {
        return 0;
    }

    @Override
    public double max() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double mean() {
        return 1 / lambda;
    }

    @Override
    public double mode() {
        return 0;
    }

    @Override
    public double median() {
        return Math.log(2) / lambda;
    }

    @Override
    public double var() {
        return 1 / (lambda * lambda);
    }

    @Override
    public double skewness() {
        return 2;
    }

    @Override
    public double kurtosis() {
        return 6;
    }

    @Override
    public double entropy() {
        return 1 - Math.log(lambda);
    }
}
