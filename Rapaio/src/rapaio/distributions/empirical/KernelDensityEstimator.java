/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.distributions.empirical;

import rapaio.core.UnivariateFunction;
import rapaio.data.Vector;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KernelDensityEstimator {

    private final Vector values;
    private final KernelFunction kernel;
    private final double bandwidth;

    public KernelDensityEstimator(Vector values, double bandwidth) {
        this(values, new KernelFunctionGaussian(), bandwidth);
    }

    public KernelDensityEstimator(Vector values, KernelFunction kernel, double bandwidth) {
        this.values = values;
        this.kernel = kernel;
        this.bandwidth = bandwidth;
    }

    public double pdf(double x) {
        double sum = 0;
        double count = 0;
        for (int i = 0; i < values.getRowCount(); i++) {
            if (values.isMissing(i)) {
                continue;
            }
            count++;
            sum += kernel.pdf(x, values.getValue(i), bandwidth);
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
}
