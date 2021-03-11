/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.linear.DVector;

/**
 * Backtracking strategy for line search.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public class BacktrackLineSearch implements LineSearch {

    public static BacktrackLineSearch fromDefaults() {
        return new BacktrackLineSearch(DEFAULT_ALPHA_0, DEFAULT_SHRINK_FACTOR, DEFAULT_C);
    }

    public static BacktrackLineSearch from(double alpha0) {
        return new BacktrackLineSearch(alpha0, DEFAULT_SHRINK_FACTOR, DEFAULT_C);
    }

    public static BacktrackLineSearch from(double alpha0, double shrink, double c) {
        return new BacktrackLineSearch(alpha0, shrink, c);
    }

    public static final double DEFAULT_ALPHA_0 = 1.0;
    public static final double DEFAULT_SHRINK_FACTOR = 0.5;
    public static final double DEFAULT_C = 0.9;

    private final double alpha0;
    private final double shrink; // in (0,0.5]
    private final double c; // in (0,1]

    private BacktrackLineSearch(double alpha0, double shrink, double c) {

        validateParameters(alpha0, shrink, c);
        this.alpha0 = alpha0;
        this.shrink = shrink;
        this.c = c;
    }

    private void validateParameters(double alpha0, double shrink, double c) {
        if (alpha0 <= 0) {
            throw new IllegalArgumentException();
        }
        if (shrink <= 0 || shrink >= 1) {
            throw new IllegalArgumentException();
        }
        if (c <= 0 || c >= 1) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public double search(RFunction f, RDerivative df, DVector x, DVector p) {
        double fx = f.apply(x);
        double t = c * df.apply(x).dot(p);

        double alpha = alpha0;
        while (true) {
            DVector palpha = p.caxpy(alpha, x);
            double fdelta = f.apply(palpha);
            if (fdelta > fx + alpha * t) {
                alpha *= shrink;
                continue;
            }
            break;
        }
        return alpha;
    }
}
