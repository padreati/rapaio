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

import rapaio.narray.NArray;
import rapaio.printer.Format;

/**
 * The Generalized T-Student Kernel has been proven to be a
 * Mercer Kernel, thus having a positive semi-definite Kernel
 * matrix (Boughorbel, 2004).
 * It is given by:
 * <p>
 * k(x,y) = \frac{1}{1 + \lVert x-y \rVert ^d}
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class GeneralizedStudentTKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -1302773223473974349L;

    private final double degree;

    public GeneralizedStudentTKernel(double degree) {
        this.degree = degree;
    }

    @Override
    public double compute(NArray<Double> v, NArray<Double> u) {
        double dot = deltaSumSquares(u, v);
        return 1.0 / (1.0 + Math.pow(dot, degree));
    }

    @Override
    public Kernel newInstance() {
        return new GeneralizedStudentTKernel(degree);
    }

    @Override
    public String name() {
        return "GeneralizedStudent(degree=" + Format.floatFlex(degree) + ")";
    }
}
