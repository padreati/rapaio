/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.distributions.empirical;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.filter.VSort;

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
        this.values = VSort.ascending().fapply(values).stream().filter(s -> !s.isMissing()).mapToDouble().toArray();
        this.kernel = kernel;
        this.bandwidth = bandwidth;
    }

    public double pdf(double x) {
        // to optimize the computation, we find all sample values which have positive weights
        int from = Arrays.binarySearch(values, kernel.minValue(x, bandwidth));
        if (from < 0) from = -from - 1;

        int to = Arrays.binarySearch(values, kernel.maxValue(x, bandwidth));
        if (to < 0) to = -to - 1;

        // compute the pdf kernel estimator as \frac{1}{nh} sum_{i=1}^{n} k(\frac{x-x_i}{h})
        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += kernel.pdf(x, values[i], bandwidth);
        }
        return sum / (values.length * bandwidth);
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
     * @return teh value of the approximation for bandwidth
     */
    public static double silvermanBandwidth(Var vector) {
        Variance var = Variance.of(vector);
        double sd = Math.sqrt(var.value());
        if (sd == 0) {
            sd = 1;
        }
        double count = vector.stream().complete().count();
        return 1.06 * sd * Math.pow(count, -1. / 5.);
    }
}
