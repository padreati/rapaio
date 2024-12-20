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
 * The exponential kernel is closely related to the GaussianPdf kernel, with only the square of the norm left out.
 * It is also a radial basis function kernel.
 * <p>
 * k(x, y) = \exp\left(-\frac{ \lVert x-y \rVert }{2\sigma^2}\right)
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class ExponentialKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = 7590795016650597990L;

    private final double sigma;
    private final double factor;

    public ExponentialKernel() {
        this(7);
    }

    public ExponentialKernel(double sigma) {
        this.sigma = sigma;
        this.factor = -1.0 / (2.0 * sigma * sigma);
    }

    @Override
    public double compute(DArray<Double> v, DArray<Double> u) {
        double value = deltaSumSquares(v, u);
        return Math.exp(factor * value);
    }

    @Override
    public Kernel newInstance() {
        return new ExponentialKernel(sigma);
    }

    @Override
    public String name() {
        return "Exponential(sigma=" + Format.floatFlex(sigma) + ",factor=" + Format.floatFlex(factor) + ")";
    }
}
