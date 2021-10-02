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
 * The Cauchy kernel comes from the Cauchy distribution (Basak, 2008).
 * It is a long-tailed kernel and can be used to give long-range influence
 * and sensitivity over the high dimension space.
 * <p>
 * k(x, y) = \frac{1}{1 + \frac{\lVert x-y \rVert^2}{\sigma^2} }
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class CauchyKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -5631098319904454645L;

    private final double sigma;

    /**
     * The Cauchy kernel comes from the Cauchy distribution (Basak, 2008).
     * It is a long-tailed kernel and can be used to give long-range influence
     * and sensitivity over the high dimension space.
     * <p>
     * k(x, y) = \frac{1}{1 + \frac{\lVert x-y \rVert^2}{\sigma^2} }
     *
     * @param sigma sigma value
     */
    public CauchyKernel(double sigma) {
        this.sigma = sigma;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double value = deltaSumSquares(df1, row1, df2, row2) / sigma;
        return 1.0 / (1.0 + value * value);
    }

    @Override
    public double compute(DVector v, DVector u) {
        double value = deltaSumSquares(u, v) / sigma;
        return 1.0 / (1.0 + value * value);
    }

    @Override
    public Kernel newInstance() {
        return new CauchyKernel(sigma);
    }

    @Override
    public String name() {
        return "Cauchy(" + Format.floatFlex(sigma) + ")";
    }
}
