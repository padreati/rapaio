package rapaio.distributions.empirical;

import rapaio.core.BaseMath;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelFunctionUniform implements KernelFunction {

    @Override
    public double pdf(double x, double x0, double bandwidth) {
        double value = BaseMath.abs(x - x0) / bandwidth;
        if (value <= 1) return 0.5;
        return 0;
    }
}
