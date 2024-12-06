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

package rapaio.math.optimization.linesearch;

import rapaio.darray.DArray;
import rapaio.math.optimization.functions.RDerivative;
import rapaio.math.optimization.functions.RFunction;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public interface LineSearch {

    /**
     * Computes a positive step size that adequately reduces the objective function {@code f} in the direction {@code p}
     * starting from 1.0.
     * <p>
     * In other words, it computes a factor {@code alpha} such as {@code f(x + alpha*p) < f(x)}.
     *
     * @param f  objective function
     * @param df objective function derivative
     * @param x  starting position
     * @param p  search direction
     * @return computed step size {@code alpha}
     */
    default double search(RFunction f, RDerivative df, DArray<Double> x, DArray<Double> p) {
        return search(f, df, x, p, 1.0);
    }

    /**
     * Computes a positive step size that adequately reduces the objective function {@code f} in the direction {@code p}.
     * <p>
     * In other words, it computes a factor {@code alpha} such as {@code f(x + alpha*p) < f(x)}.
     *
     * @param f  objective function
     * @param df objective function derivative
     * @param x  starting position
     * @param p  search direction
     * @param t0 initial step size value
     * @return computed step size {@code alpha}
     */
    double search(RFunction f, RDerivative df, DArray<Double> x, DArray<Double> p, double t0);
}
