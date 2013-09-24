package rapaio.distributions.empirical;

import rapaio.core.BaseMath;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelFunctionTriangular implements KernelFunction {

    @Override
    public double pdf(double x, double x0, double bandwidth) {
        double value = BaseMath.abs(x - x0) / bandwidth;
        return value <= 1 ? 1 - value : 0;
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
