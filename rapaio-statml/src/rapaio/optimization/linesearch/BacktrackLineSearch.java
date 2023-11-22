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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.optimization.linesearch;

import java.io.Serial;

import rapaio.optimization.functions.RDerivative;
import rapaio.optimization.functions.RFunction;
import rapaio.linear.DVector;
import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;

/**
 * Backtracking strategy for line search.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public class BacktrackLineSearch extends ParamSet<BacktrackLineSearch> implements LineSearch {

    @Serial
    private static final long serialVersionUID = -831087752500069658L;

    public static BacktrackLineSearch newSearch() {
        return new BacktrackLineSearch();
    }

    // typically between 0.01 and 0.3
    public static final double DEFAULT_ALPHA = 0.1;
    // 0.1 (which corresponds to a very crude search) and 0.8 (which corresponds to a less crude search)
    public static final double DEFAULT_BETA = 0.7;

    /**
     * Alpha parameter which corresponds to how much gain is is enough for a good fit.
     */
    public final ValueParam<Double, BacktrackLineSearch> alpha = new ValueParam<>(this,
            DEFAULT_ALPHA, "alpha", v -> Double.isFinite(v) && v > 0 && v < 0.5);
    /**
     * Beta parameter which corresponds with the backtrack shrinking factor for each search iteration.
     */
    public final ValueParam<Double, BacktrackLineSearch> beta = new ValueParam<>(this,
            DEFAULT_BETA, "beta", value -> Double.isFinite(value) && value > 0 && value < 1);

    private BacktrackLineSearch() {
    }

    @Override
    public double search(RFunction f, RDerivative g, DVector x, DVector p, double t0) {
        double fx = f.apply(x);
        double gxp = g.apply(x).dot(p);

        double xalpha = alpha.get();
        double xbeta = beta.get();

        double t = t0;
        while (f.apply(x.fmaNew(t, p)) > fx + xalpha * t * gxp) {
            t *= xbeta;
        }
        return t;
    }
}
