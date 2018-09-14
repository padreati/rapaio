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

package rapaio.math.optim.linesearch;

import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.linear.RV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public class BacktrackLineSearch implements LineSearch {

    public static BacktrackLineSearch from() {
        return new BacktrackLineSearch();
    }

    private final double rho = 0.1; // in 0-0.5
    private final double c = 0.9;

    @Override
    public double find(RFunction f, RDerivative d1f, RV x, RV delta_f) {
        double fx = f.apply(x);
        double m = d1f.apply(x).dotProd(delta_f);

        double alpha = 1;
        while (true) {
            double f_x_plus_alpha_delta = f.apply(x.solidCopy().plus(delta_f.solidCopy().dot(alpha)));
            if (f_x_plus_alpha_delta > fx + c * alpha * m) {
                alpha *= rho;
                continue;
            }
            break;
        }
        return alpha;
    }
}
