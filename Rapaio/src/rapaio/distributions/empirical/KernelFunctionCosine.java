package rapaio.distributions.empirical;


import static rapaio.core.BaseMath.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelFunctionCosine implements KernelFunction {

    @Override
    public double pdf(double x, double x0, double bandwidth) {

        double value = abs(x - x0) / bandwidth;
        if (value <= 1) {
            return PI * Math.cos(PI * value / 2) / 4.;
        }
        return 0;
    }
}
