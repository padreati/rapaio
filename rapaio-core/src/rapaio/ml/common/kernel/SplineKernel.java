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

import rapaio.math.narray.NArray;

/**
 * The Spline kernel is given as a piece-wise cubic polynomial, as derived in the works by Gunn (1998).
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class SplineKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -4985948375658836441L;

    @Override
    public double compute(NArray<Double> v, NArray<Double> u) {
        double value = 1;
        for (int i = 0; i < u.size(); i++) {
            double xi = v.getDouble(i);
            double yi = u.getDouble(i);
            double min = Math.min(xi, yi);
            value *= 1 + xi * yi + xi * yi * min - (xi + yi) * min * min / 2.0 + min * min * min / 3;
        }
        return value;
    }

    @Override
    public Kernel newInstance() {
        return new SplineKernel();
    }

    @Override
    public String name() {
        return "Spline";
    }
}
