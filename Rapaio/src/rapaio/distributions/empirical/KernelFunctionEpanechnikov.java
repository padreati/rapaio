package rapaio.distributions.empirical;

import rapaio.core.BaseMath;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelFunctionEpanechnikov implements KernelFunction {

    @Override
    public double pdf(double x, double x0, double bandwidth) {
        double value = BaseMath.abs(x - x0) / bandwidth;
        return value <= 1 ? 3. * (1 - value * value) / 4. : 0;
    }
}
