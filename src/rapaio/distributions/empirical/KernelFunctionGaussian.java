package rapaio.distributions.empirical;

import rapaio.distributions.Distribution;
import rapaio.distributions.Normal;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelFunctionGaussian implements KernelFunction {

    private final Distribution normal = new Normal();

    @Override
    public double pdf(double x, double x0, double bandwidth) {
        return normal.pdf((x - x0) / bandwidth);
    }

    @Override
    public double getMinValue(double x0, double bandwidth) {
        return x0 - 4 * bandwidth;
    }

    @Override
    public double getMaxValue(double x0, double bandwidth) {
        return x0 + 4 * bandwidth;
    }
}
