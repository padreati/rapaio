package rapaio.distributions.empirical;

import rapaio.core.BaseMath;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelFunctionTricube implements KernelFunction {

    @Override
    public double pdf(double x, double x0, double bandwidth) {
        double value = BaseMath.abs(x - x0) / bandwidth;
        if (value <= 1) {
            double weight = 1 - value * value * value;
            return 70. * weight * weight * weight / 81.;
        }
        return 0;
    }

    @Override
    public double getMinValue(double x0, double bandwidth) {
        return x0 - bandwidth;
    }

    @Override
    public double getMaxValue(double x0, double bandwidth) {
        return x0 + bandwidth;
    }
}
