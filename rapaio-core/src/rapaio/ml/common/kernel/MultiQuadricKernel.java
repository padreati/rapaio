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

import java.io.Serial;

import rapaio.math.tensor.Tensor;
import rapaio.printer.Format;

/**
 * The Multiquadric kernel can be used in the same situations as the Rational Quadratic kernel.
 * As is the case with the Sigmoid kernel, it is also an example of an
 * non-positive definite kernel.
 * <p>
 * k(x, y) = \sqrt{\lVert x-y \rVert^2 + c^2}
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
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
    public double compute(Tensor<Double> v, Tensor<Double> u) {
        double dot = deltaSumSquares(u, v);
        return Math.sqrt(dot * dot + c_square);
    }

    @Override
    public Kernel newInstance() {
        return new MultiQuadricKernel(c);
    }

    @Override
    public String name() {
        return STR."MultiQuadratic(c=\{Format.floatFlex(c)})";
    }
}
