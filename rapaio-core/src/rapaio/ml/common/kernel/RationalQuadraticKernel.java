/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
 * The Rational Quadratic kernel is less computationally intensive than the GaussianPdf kernel
 * and can be used as an alternative when using the GaussianPdf becomes too expensive.
 * <p>
 * k(x, y) = 1 - \frac{\lVert x-y \rVert^2}{\lVert x-y \rVert^2 + c}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class RationalQuadraticKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = 4637136575173648153L;

    private final double c;

    public RationalQuadraticKernel(double c) {
        this.c = c;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double dot = deltaSumSquares(df1, row1, df2, row2);
        double square = dot * dot;
        return 1.0 - square / (square + c);
    }

    @Override
    public double compute(DVector v, DVector u) {
        double dot = deltaSumSquares(u, v);
        double square = dot * dot;
        return 1.0 - square / (square + c);
    }

    @Override
    public Kernel newInstance() {
        return new RationalQuadraticKernel(c);
    }

    @Override
    public String name() {
        return "RationalQuadratic(c=" + Format.floatFlex(c) + ")";
    }
}
