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

package rapaio.math.optimization.scalar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rapaio.ml.common.ValueParam;
import rapaio.util.function.Double2DoubleFunction;

/**
 * This is an implementation of Illinois algorithm, a better variant of Regula Falsi algorithm
 * for root finding.
 * <p>
 * For reference see: <a href="https://en.wikipedia.org/wiki/Regula_falsi">Wikipedia Regula Falsi</a>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/26/21.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegulaFalsiScalarRootFind extends ScalarRootFind<RegulaFalsiScalarRootFind> {

    public static RegulaFalsiScalarRootFind newMethod() {
        return new RegulaFalsiScalarRootFind();
    }

    private static final long serialVersionUID = -1252533729600687889L;

    public final ValueParam<Double, RegulaFalsiScalarRootFind> a = new ValueParam<>(this,
            Double.NaN, "a", "bracket first point");
    public final ValueParam<Double, RegulaFalsiScalarRootFind> b = new ValueParam<>(this,
            Double.NaN, "b", "bracket second point");
    public final ValueParam<Double, RegulaFalsiScalarRootFind> eps = new ValueParam<>(this,
            1e-20, "eps", "tolerance for approximation");
    public final ValueParam<Integer, RegulaFalsiScalarRootFind> maxIter = new ValueParam<>(this,
            100_000, "maxIter", "maximum number of iterations");

    @Getter
    private double x;
    @Getter
    private boolean converged;
    @Getter
    private int iterations;

    @Override
    double optimize(Double2DoubleFunction f) {

        double r = Double.NaN;
        int side = 0;
        /* starting values at endpoints of interval */
        double xa = a.get();
        double xb = b.get();
        double fa = f.apply(xa);
        double fb = f.apply(xb);

        for (int it = 0; it < maxIter.get(); it++) {

            r = (fa * xb - fb * xa) / (fa - fb);
            if (Math.abs(xb - xa) < eps.get() * Math.abs(xb + xa)) {
                x = r;
                converged = true;
                iterations = it + 1;
                return x;
            }
            double fr = f.apply(r);

            if (fr * fb > 0) {
                /* fr and ft have same sign, copy r to t */
                xb = r;
                fb = fr;
                if (side == -1) {
                    fa /= 2;
                }
                side = -1;
            } else if (fa * fr > 0) {
                /* fr and fs have same sign, copy r to s */
                xa = r;
                fa = fr;
                if (side == +1) {
                    fb /= 2;
                }
                side = +1;
            } else {
                /* fr * f_ very small (looks like zero) */
                x = r;
                converged = true;
                iterations = it;
                return x;
            }
        }
        x = r;
        converged = false;
        iterations = maxIter.get();
        return r;
    }
}
