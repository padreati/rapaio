/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.data.Frame;
import rapaio.math.linear.DVector;
import rapaio.printer.Format;

import java.io.Serial;

/**
 * The Multiquadric kernel can be used in the same situations as the Rational Quadratic kernel.
 * As is the case with the Sigmoid kernel, it is also an example of an
 * non-positive definite kernel.
 * <p>
 * k(x, y) = \sqrt{\lVert x-y \rVert^2 + c^2}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class MultiQuadricKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -4215277675823113044L;

    private final double c;
    private final double c_square;

    public MultiQuadricKernel(double c) {
        this.c = c;
        this.c_square = c * c;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double dot = deltaSumSquares(df1, row1, df2, row2);
        return Math.sqrt(dot * dot + c_square);
    }

    @Override
    public double compute(DVector v, DVector u) {
        double dot = deltaSumSquares(u, v);
        return Math.sqrt(dot * dot + c_square);
    }

    @Override
    public Kernel newInstance() {
        return new MultiQuadricKernel(c);
    }

    @Override
    public String name() {
        return "MultiQuadric(c=" + Format.floatFlex(c) + ")";
    }
}
