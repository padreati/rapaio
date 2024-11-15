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

import rapaio.math.optimization.functions.RDerivative;
import rapaio.math.optimization.functions.RFunction;
import rapaio.math.narrays.NArray;

/**
 * Fixed rate line search strategy. This strategy actually does not compute a step size,
 * but used a fixed step size specified at construction time. This can be useful in setups
 * where it is known in advance an approximately good step size so any calculation can
 * be avoided, or for simulation purposes.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public class LearningRateLineSearch implements LineSearch {

    public static LearningRateLineSearch from(double r) {
        return new LearningRateLineSearch(r);
    }

    private final double r;

    private LearningRateLineSearch(double r) {
        if (r <= 0 || !Double.isFinite(r)) {
            throw new IllegalArgumentException("Learning rate must have a finite positive value");
        }
        this.r = r;
    }

    @Override
    public double search(RFunction f, RDerivative df, NArray<Double> x, NArray<Double> p, double t0) {
        return r;
    }
}
