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

package rapaio.ml.common.kernel;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.math.linear.DVector;
import rapaio.printer.Format;

/**
 * Spherical Kernel
 * <p>
 * The spherical kernel is similar to the circular kernel, but is positive definite in R3.
 * <p>
 * k(x, y) = 1 - \frac{3}{2} \frac{\lVert x-y \rVert}{\sigma} + \frac{1}{2} \left( \frac{ \lVert x-y \rVert}{\sigma} \right)^3
 * <p>
 * \mbox{if}~ \lVert x-y \rVert < \sigma \mbox{, zero otherwise}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class SphericalKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -7447828392149152605L;

    private final double sigma;

    public SphericalKernel(double sigma) {
        this.sigma = sigma;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double dot = deltaSumSquares(df1, row1, df2, row2);
        if (dot < sigma)
            return 0;
        double f = dot / sigma;
        return 1 - 3 * f / 2 + f * f * f / 2;
    }

    @Override
    public double compute(DVector v, DVector u) {
        double dot = deltaSumSquares(u, v);
        if (dot < sigma)
            return 0;
        double f = dot / sigma;
        return 1 - 3 * f / 2 + f * f * f / 2;
    }

    @Override
    public Kernel newInstance() {
        return new SphericalKernel(sigma);
    }

    @Override
    public String name() {
        return "Spherical(sigma=" + Format.floatFlex(sigma) + ")";
    }
}
