/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.math.optimization;

import rapaio.math.linear.RV;
import rapaio.util.Pair;

/**
 * Compute gradient and loss for a Least-squared loss function, as used in linear regression.
 * This is correct for the averaged least squares loss function (mean squared error)
 * L = 1/2n ||A weights-y||^2
 * See also the documentation for the precise formulation.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/15.
 */
@Deprecated
public class LeastSquareGradient implements Gradient {

    @Override
    public Pair<RV, Double> compute(RV data, double label, RV weights) {
        double diff = data.dotProd(weights) - label;
        double loss = diff * diff / 2.0;
        RV gradient = data.copy();
        gradient.dot(diff);
        return Pair.valueOf(gradient, loss);
    }

    @Override
    public Double compute(RV data, double label, RV weights, RV cumGradient) {
        double diff = data.dotProd(weights) - label;
        cumGradient.plus(data.copy().dot(diff));
        return diff * diff / 2.0;
    }
}

