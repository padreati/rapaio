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
 * Bisection algorithm implementation for scalar root finding.
 * <p>
 * For more details on the algorithm see:
 * <a href="https://en.wikipedia.org/wiki/Bisection_method">Wikipedia Bisection method</a>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/26/21.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BisectionScalarRootFind extends ScalarRootFind<BisectionScalarRootFind> {

    public static BisectionScalarRootFind newMethod() {
        return new BisectionScalarRootFind();
    }

    private static final long serialVersionUID = -4697174722738465823L;

    public final ValueParam<Double, BisectionScalarRootFind> a = new ValueParam<>(this,
            Double.NaN, "a", "bracket first point");
    public final ValueParam<Double, BisectionScalarRootFind> b = new ValueParam<>(this,
            Double.NaN, "b", "bracket second point");
    public final ValueParam<Double, BisectionScalarRootFind> eps = new ValueParam<>(this,
            1e-30, "eps", "tolerance for approximation");
    public final ValueParam<Integer, BisectionScalarRootFind> maxIter = new ValueParam<>(this,
            100_000, "maxIter", "maximum number of iterations");

    @Getter
    private double x;

    @Getter
    private boolean converged;

    @Getter
    private int iterations;

    @Override
    double optimize(Double2DoubleFunction f) {
        if (!Double.isFinite(a.get()) || !Double.isFinite(b.get())) {
            throw new IllegalArgumentException("Bracket values must be finite.");
        }

        double xa = a.get();
        double xb = b.get();

        double fa = f.apply(xa);
        double fb = f.apply(xb);
        if (Math.signum(fa) == Math.signum(fb)) {
            throw new IllegalArgumentException("Bracket points does not have images of different sign.");
        }

        double xc = Double.NaN;
        for (int it = 0; it < maxIter.get(); it++) {
            xc = (xa + xb) / 2;
            double fc = f.apply(xc);

            if (Math.abs(fc) < eps.get()) {
                x = xc;
                iterations = it + 1;
                converged = true;
                return xc;
            }

            if (fa * fc < 0) {
                xb = xc;
            } else {
                fa = fc;
                xa = xc;
            }
        }
        x = xc;
        converged = false;
        iterations = maxIter.get();

        return x;
    }
}
