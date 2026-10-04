/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.core.distributions.empirical;

import java.io.Serial;
import java.io.Serializable;

import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.transform.VarSort;

/**
 * Kernel density estimator.
 * Given a sample of values, based on a given kernel and bandwidth it creates
 * an estimation of a density function.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KDE implements Serializable {

    /**
     * Builds a Gaussian kernel density estimator with bandwidth found by Silverman technique.
     *
     * @param values sample values
     * @return kernel density estimator instance
     */
    public static KDE of(Var values) {
        return new KDE(values, new KFuncGaussian(), silvermanBandwidth(values));
    }

    /**
     * Builds a Gaussian kernel density estimator with given bandwidth.
     *
     * @param values    sample values
     * @param bandwidth desired bandwidth
     * @return kernel density estimator instance
     */
    public static KDE of(Var values, double bandwidth) {
        return new KDE(values, new KFuncGaussian(), bandwidth);
    }

    /**
     * Builds a kernel density estimator with bandwidth found by Silverman technique.
     *
     * @param values sample values
     * @param kernel kernel function
     * @return kernel density estimator instance
     */
    public static KDE of(Var values, KFunc kernel) {
        return new KDE(values, kernel, silvermanBandwidth(values));
    }

    /**
     * Builds a kernel density estimator with given bandwidth.
     *
     * @param values    sample values
     * @param kernel    kernel function
     * @param bandwidth desired bandwidth
     * @return kernel density estimator instance
     */
    public static KDE of(Var values, KFunc kernel, double bandwidth) {
        return new KDE(values, kernel, bandwidth);
    }

    @Serial
    private static final long serialVersionUID = -9221394390068126299L;
    private final double[] values;
    private final KFunc kernel;
    private final double bandwidth;

    private KDE(Var values, KFunc kernel, double bandwidth) {
        // NaN is not rejected: the Silverman bandwidth of an empty sample is NaN, and the estimator of such a
        // sample has no density to give anyway
        if (bandwidth <= 0) {
            throw new IllegalArgumentException("Bandwidth of a kernel density estimator must be strictly positive, got: " + bandwidth + ".");
        }
        this.values = VarSort.ascending().fapply(values).stream().filter(s -> !s.isMissing()).mapToDouble().toArray();
        this.kernel = kernel;
        this.bandwidth = bandwidth;
    }

    /**
     * Density estimated at a point as {@code 1/(n h) sum_i K((x - x_i)/h)}.
     * <p>
     * The kernel {@link KFuncGaussian Gaussian} is not zero beyond four bandwidths from the point, but it
     * is not evaluated there: the sample values farther than {@code 4h} are ignored, which is a loss of about
     * {@code 6e-5} of the total mass, and spares the evaluation of the kernel for the whole sample.
     *
     * @param x point where the density is estimated
     * @return estimated density
     */
    public double pdf(double x) {
        // to optimize the computation, we find all sample values which have positive weights, the range
        // of values in [min, max], both ends included
        int from = lowerBound(kernel.minValue(x, bandwidth));
        int to = upperBound(kernel.maxValue(x, bandwidth));

        // compute the pdf kernel estimator as \frac{1}{nh} sum_{i=1}^{n} k(\frac{x-x_i}{h})
        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += kernel.pdf(x, values[i], bandwidth);
        }
        return sum / (values.length * bandwidth);
    }

    /** @return the first position holding a value not smaller than the given one */
    private int lowerBound(double value) {
        int lo = 0;
        int hi = values.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (values[mid] < value) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /** @return the first position holding a value greater than the given one */
    private int upperBound(double value) {
        int lo = 0;
        int hi = values.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (values[mid] <= value) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    public KFunc kernel() {
        return kernel;
    }

    public double bandwidth() {
        return bandwidth;
    }

    /**
     * Computes the approximation for bandwidth provided by Silverman,
     * known also as Silverman's rule of thumb.
     * <p>
     * Is used when the approximated is gaussian for approximating
     * univariate data.
     * <p>
     * For further reference check:
     * http://en.wikipedia.org/wiki/Kernel_density_estimation
     *
     * @param vector sample of values
     * @return the value of the approximation for bandwidth: for a sample of one value, or of equal values,
     * which have no deviation, the deviation is taken as 1; for an empty sample it is {@code NaN}
     */
    public static double silvermanBandwidth(Var vector) {
        double count = vector.stream().complete().count();
        if (count == 0) {
            return Double.NaN;
        }
        Variance var = Variance.of(vector);
        double sd = Math.sqrt(var.value());
        // the deviation is 0 for equal values and undefined (NaN) for a single one: both would give a bandwidth
        // which is 0 or NaN, and so a density which is NaN everywhere
        if (!(sd > 0)) {
            sd = 1;
        }
        return 1.06 * sd * Math.pow(count, -1. / 5.);
    }
}
