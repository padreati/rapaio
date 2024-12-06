/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.common.kernel;

import java.io.Serial;

import rapaio.darray.DArray;
import rapaio.printer.Format;

/**
 * The Generalized Histogram Intersection kernel is built based on
 * the Histogram Intersection Kernel for image classification but
 * applies in a much larger variety of contexts (Boughorbel, 2005).
 * It is given by:
 * <p>
 * k(x,y) = \sum_{i=1}^m \min(|x_i|^\alpha,|y_i|^\beta)
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class GeneralizedMinKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -5905853828762141455L;

    private final double alpha;
    private final double beta;

    public GeneralizedMinKernel(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public double compute(DArray<Double> v, DArray<Double> u) {
        double sum = 0;
        for (int i = 0; i < u.size(); i++) {
            sum += Math.min(
                    Math.pow(Math.abs(v.getDouble(i)), alpha),
                    Math.pow(Math.abs(u.getDouble(i)), beta)
            );
        }
        return sum;
    }

    @Override
    public Kernel newInstance() {
        return new GeneralizedMinKernel(alpha, beta);
    }

    @Override
    public String name() {
        return "GeneralizedMean(alpha=" + Format.floatFlex(alpha) + ",beta=" + Format.floatFlex(beta) + ")";
    }
}
