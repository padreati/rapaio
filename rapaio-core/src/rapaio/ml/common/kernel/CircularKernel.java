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
 * Circular Kernel
 * <p>
 * The circular kernel is used in geostatic applications. It is an example of an isotropic stationary kernel and is positive definite in R2.
 * <p>
 * k(x, y) = \frac{2}{\pi} \arccos ( - \frac{ \lVert x-y \rVert}{\sigma}) - \frac{2}{\pi} \frac{ \lVert x-y \rVert}{\sigma} \sqrt{1 - \left(\frac{ \lVert x-y \rVert}{\sigma} \right)^2}
 * <p>
 * \mbox{if}~ \lVert x-y \rVert < \sigma \mbox{, zero otherwise}
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class CircularKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -3141672110292845302L;

    private final double sigma;

    public CircularKernel(double sigma) {
        this.sigma = sigma;
    }

    @Override
    public double compute(DArray<Double> v, DArray<Double> u) {
        double dot = deltaSumSquares(u, v);
        if (dot < sigma) {
            return 0;
        }
        double f = dot / sigma;
        return 2 * (Math.acos(-f) - f * Math.sqrt(1 - f * f)) / Math.PI;
    }

    @Override
    public Kernel newInstance() {
        return new CircularKernel(sigma);
    }

    @Override
    public String name() {
        return "Circular(sigma="+Format.floatFlex(sigma)+")";
    }
}
