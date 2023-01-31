/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import static rapaio.printer.Format.floatFlex;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.math.MathTools;
import rapaio.math.linear.DVector;

/**
 * The Polynomial kernel is a non-stationary kernel. Polynomial kernels
 * are well suited for problems where all the training data is normalized.
 * <p>
 * k(x, y) = (slope * x^T y + bias)^exponent
 * <p>
 * Adjustable parameters are the slope alpha, the constant term c and the
 * polynomial degree d.
 * <p>
 * A special case is the linear kernel (exponent=1, slope=1,bias=0).
 * <p>
 * The Linear kernel is the simplest kernel function. It is given by the
 * inner product <x,y> plus an optional constant c. Kernel algorithms
 * using a linear kernel are often equivalent to their non-kernel
 * counterparts, i.e. KPCA with linear kernel is the same as standard PCA.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public class PolyKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = 7520286921201342580L;

    private final double exponent;
    private final double bias;
    private final double slope;

    @Override
    public boolean isLinear() {
        return MathTools.eq(exponent, 1.0);
    }

    @Override
    public Kernel newInstance() {
        return new PolyKernel(exponent, bias, slope);
    }

    public PolyKernel(double exponent) {
        this(exponent, 1.0, 1.0);
    }

    public PolyKernel(double exponent, double bias) {
        this(exponent, bias, 1.0);
    }

    @Override
    public String name() {
        return "PolyKernel(" +
                "exp=" + floatFlex(exponent) + "," +
                "bias=" + floatFlex(bias) + "," +
                "slope=" + floatFlex(slope) +
                ")";
    }

    public PolyKernel(double exponent, double bias, double slope) {
        this.exponent = exponent;
        this.slope = slope;
        this.bias = bias;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {

        if (varNames == null) {
            throw new IllegalArgumentException("This kernel is not build with var names");
        }
        if (isLinear()) {
            return slope * dotProd(df1, row1, df2, row2) + bias;
        }
        return Math.pow(slope * dotProd(df1, row1, df2, row2) + bias, exponent);
    }

    @Override
    public double compute(DVector v, DVector u) {
        if (isLinear()) {
            return slope * v.dot(u) + bias;
        }
        return Math.pow(slope * v.dot(u) + bias, exponent);
    }
}