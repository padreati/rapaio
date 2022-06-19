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

import static rapaio.printer.Format.floatFlex;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.math.linear.DVector;

/**
 * The GaussianPdf kernel is an example of radial basis function kernel.
 * <p>
 * k(x, y) = \exp\left(-\frac{ \lVert x-y \rVert ^2}{2\sigma^2}\right)
 * <p>
 * Alternatively, it could also be implemented using
 * <p>
 * k(x, y) = \exp\left(- \gamma \lVert x-y \rVert ^2 )
 * <p>
 * The adjustable parameter sigma plays a major role in the performance of
 * the kernel, and should be carefully tuned to the problem at hand. If
 * overestimated, the exponential will behave almost linearly and the
 * higher-dimensional projection will start to lose its non-linear power.
 * In the other hand, if underestimated, the function will lack regularization
 * and the decision boundary will be highly sensitive to noise in training data.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public class RBFKernel extends AbstractKernel {

    public static RBFKernel fromGamma(double gamma) {
        return new RBFKernel(gamma);
    }

    public static RBFKernel fromSigma(double sigma) {
        double gamma = 1 / (2.0 * sigma * sigma);
        return new RBFKernel(gamma);
    }

    @Serial
    private static final long serialVersionUID = -2105174939802643460L;

    private final double gamma;

    public RBFKernel(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double value = deltaSumSquares(df1, row1, df2, row2);
        return Math.exp(-gamma * value);
    }

    @Override
    public double compute(DVector v, DVector u) {
        double value = deltaSumSquares(v, u);
        return Math.exp(-gamma * value);
    }

    @Override
    public Kernel newInstance() {
        return new RBFKernel(gamma);
    }

    @Override
    public String name() {
        return "RBF(gamma=" + floatFlex(gamma) + ")";
    }
}
