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
public class HingeGradient implements Gradient {

    @Override
    public Pair<RV, Double> compute(RV data, double label, RV weights) {
        double dotProduct = data.dotProd(weights);

        // Our loss function with {0, 1} labels is max(0, 1 - (2y - 1) (f_w(x)))
        // Therefore the gradient is -(2y - 1)*x
        double labelScaled = 2 * label - 1.0;
        if (1.0 > labelScaled * dotProduct) {
            RV gradient = data.copy();
            gradient.dot(-labelScaled);
            return Pair.valueOf(gradient, 1.0 - labelScaled * dotProduct);
        } else {
            return Pair.valueOf(Linear.newRVEmpty(weights.rowCount()), 0.0);
        }
    }

    @Override
    public Double compute(RV data, double label, RV weights, RV cumGradient) {
        double dotProduct = data.dotProd(weights);
        // Our loss function with {0, 1} labels is max(0, 1 - (2y - 1) (f_w(x)))
        // Therefore the gradient is -(2y - 1)*x

        double labelScaled = 2 * label - 1.0;
        if (1.0 > labelScaled * dotProduct) {
            cumGradient.plus(data.copy().dot(-labelScaled));
            return 1.0 - labelScaled * dotProduct;
        }
        return 0.0;
    }
}
