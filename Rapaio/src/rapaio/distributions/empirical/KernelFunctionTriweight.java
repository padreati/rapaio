package rapaio.distributions.empirical;

import rapaio.core.BaseMath;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelFunctionTriweight implements KernelFunction {

    @Override
    public double pdf(double x, double x0, double bandwidth) {
        double value = BaseMath.abs(x - x0) / bandwidth;
        if (value <= 1) {
            double weight = 1 - value * value;
            return 35. * weight * weight * weight / 32.;
        }
        return 0;
    }
}
