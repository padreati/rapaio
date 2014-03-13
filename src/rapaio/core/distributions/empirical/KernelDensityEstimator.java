/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.distributions.empirical;

import rapaio.core.UnivariateFunction;
import rapaio.core.stat.Variance;
import rapaio.data.Vector;

import static rapaio.core.MathBase.pow;
import static rapaio.core.MathBase.sqrt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelDensityEstimator {

    private final Vector values;
    private final KernelFunction kernel;
    private final double bandwidth;

    public KernelDensityEstimator(Vector values) {
        this.values = values;
        this.kernel = new KernelFunctionGaussian();
        this.bandwidth = getSilvermanBandwidth(values);
    }

    public KernelDensityEstimator(Vector values, double bandwidth) {
        this.values = values;
        this.kernel = new KernelFunctionGaussian();
        this.bandwidth = bandwidth;
    }

    public KernelDensityEstimator(Vector values, KernelFunction kernel) {
        this.values = values;
        this.kernel = kernel;
        this.bandwidth = getSilvermanBandwidth(values);
    }

    public KernelDensityEstimator(Vector values, KernelFunction kernel, double bandwidth) {
        this.values = values;
        this.kernel = kernel;
        this.bandwidth = bandwidth;
    }

    public double pdf(double x) {
        double sum = 0;
        double count = 0;
        for (int i = 0; i < values.rowCount(); i++) {
            if (values.missing(i)) {
                continue;
            }
            count++;
            sum += kernel.pdf(x, values.value(i), bandwidth);
        }
        return sum / (count * bandwidth);
    }

    public UnivariateFunction getPdfFunction() {
        return new UnivariateFunction() {

            @Override
            public double eval(double value) {
                return pdf(value);
            }
        };
    }

    public KernelFunction getKernel() {
        return kernel;
    }

    public Vector getValues() {
        return values;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Computes the approximation for bandwidth provided by Silverman,
     * known also as Silverman's rule of thumb.
     * <p>
     * Is used when the approximated is normal for approximating
     * univariate data.
     * <p>
     * For further reference check:
     * http://en.wikipedia.org/wiki/Kernel_density_estimation
     *
     * @param vector sample of values
     * @return teh getValue of the approximation for bandwidth
     */
    public final double getSilvermanBandwidth(Vector vector) {
        Variance var = new Variance(vector);
        double sd = sqrt(var.getValue());
        if (sd == 0) {
            sd = 1;
        }
        double count = 0;
        for (int i = 0; i < vector.rowCount(); i++) if (!vector.missing(i)) count++;
        return 1.06 * sd * pow(count, -1. / 5.);
    }
}
