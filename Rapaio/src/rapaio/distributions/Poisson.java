package rapaio.distributions;

import static rapaio.core.BaseMath.pow;

/**
 * @author Aurelian Tutuianu
 */
@Deprecated
public class Poisson extends Distribution {

    private final double lambda;

    public Poisson(double lambda) {
        if (lambda <= 0.) {
            throw new IllegalArgumentException("Lambda parameter for Poisson distribution must have positive getValue.");
        }
        this.lambda = lambda;
    }

    @Override
    public String getName() {
        return "Poisson Distribution";
    }

    @Override
    public double pdf(double x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double cdf(double x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double quantile(double p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double min() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double max() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double mean() {
        return lambda;
    }

    @Override
    public double mode() {
        return lambda;
    }

    @Override
    public double variance() {
        return lambda;
    }

    @Override
    public double skewness() {
        return pow(lambda, -0.5);
    }

    @Override
    public double kurtosis() {
        return pow(lambda, -1);
    }
}
