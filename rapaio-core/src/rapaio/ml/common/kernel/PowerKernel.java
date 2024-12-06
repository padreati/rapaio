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
 * The Power kernel is also known as the (unrectified) triangular kernel.
 * It is an example of scale-invariant kernel (Sahbi and Fleuret, 2004)
 * and is also only conditionally positive definite.
 * <p>
 * k(x,y) = - \lVert x-y \rVert ^d
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class PowerKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -974630838457936489L;

    private final double degree;

    public PowerKernel(double degree) {
        this.degree = degree;
    }

    @Override
    public double compute(DArray<Double> v, DArray<Double> u) {
        return -Math.pow(deltaSumSquares(u, v), degree);
    }

    @Override
    public Kernel newInstance() {
        return new PowerKernel(degree);
    }

    @Override
    public String name() {
        return "Power(degree=" + Format.floatFlex(degree) + ")";
    }
}
