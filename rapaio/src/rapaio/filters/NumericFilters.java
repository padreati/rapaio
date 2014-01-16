/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */
package rapaio.filters;

import rapaio.core.UnivariateFunction;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.distributions.Normal;

/**
 * Provides filter operations on numeric vectors.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class NumericFilters {

    private NumericFilters() {
    }

    /**
     * Compute the result of a numerical univariate function using numerical
     * values from a {@link Vector} as input parameter.
     *
     * @param vector  input value vector
     * @param f       univariate function
     * @param f}      applied over the values from {
     * @param vector}
     * @return function {
     */
    public static Vector applyFunction(Vector vector, UnivariateFunction f) {
        Vector ret = new Numeric(vector.rowCount());
        for (int i = 0; i < vector.rowCount(); i++) {
            if (vector.isMissing(i)) {
                continue;
            }
            ret.setValue(i, f.eval(vector.value(i)));
        }
        return ret;
    }

    /**
     * Alter valid numeric values with normally distributed noise.
     * <p/>
     * Noise comes from a normal distribution with mean 0 and standard deviation
     * 0.1
     *
     * @param vector input values
     * @return altered values
     */
    public static Vector jitter(Vector vector) {
        return jitter(vector, 0.1);
    }

    /**
     * Alter valid numeric values with normally distributed noise.
     * <p/>
     * Noise comes from a normal distribution with mean 0 and standard deviation
     * specified by {
     *
     * @param sd}
     * @param vector input values
     * @param sd     standard deviation of the normally distributed noise
     * @return altered values
     */
    public static Vector jitter(Vector vector, double sd) {
        Normal d = new Normal(0, sd);
        Vector result = new Numeric(vector.rowCount());
        Vector jitter = d.sample(result.rowCount());
        for (int i = 0; i < result.rowCount(); i++) {
            if (vector.isMissing(i)) {
                continue;
            }
            result.setValue(i, vector.value(i) + jitter.value(i));
        }
        return result;
    }
}
