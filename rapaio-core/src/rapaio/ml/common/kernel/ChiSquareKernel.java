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

/**
 * The Chi-Square kernel comes from the Chi-Square distribution.
 * There are multiple version, this is the conditionally positive definite version of it
 * <p>
 * k(x,y) = 1 - \sum_{i=1}^n \frac{2(x_i-y_i)^2}{(x_i+y_i)}
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class ChiSquareKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -3301596992870913061L;

    @Override
    public double compute(DArray<Double> v, DArray<Double> u) {
        var sum = v.add(u);
        var delta = v.sub(u);
        return 1 - 2 * delta.sqr().div(sum).sum();
    }

    @Override
    public Kernel newInstance() {
        return new ChiSquareKernel();
    }

    @Override
    public String name() {
        return "ChiSquare";
    }
}
