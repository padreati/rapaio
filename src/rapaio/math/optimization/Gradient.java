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

import rapaio.math.linear.Linear;
import rapaio.math.linear.RV;
import rapaio.util.Pair;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/15.
 */
@Deprecated
public interface Gradient {

    /**
     * Compute the gradient and loss given the features of a single data point.
     *
     * @param data    features for one data point
     * @param label   label for this data point
     * @param weights weights/coefficients corresponding to features
     * @return Pair(Vector gradient, Double loss)
     */
    default Pair<RV, Double> compute(RV data, double label, RV weights) {
        RV gradient = Linear.newRVEmpty(weights.rowCount());
        Double loss = compute(data, label, weights, gradient);
        return Pair.valueOf(gradient, loss);
    }

    /**
     * Compute the gradient and loss given the features of a single data point,
     * add the gradient to a provided vector to avoid creating new objects, and return loss.
     *
     * @param data        features for one data point
     * @param label       label for this data point
     * @param weights     weights/coefficients corresponding to features
     * @param cumGradient the computed gradient will be added to this vector
     * @return loss
     */
    Double compute(RV data, double label, RV weights, RV cumGradient);

}
