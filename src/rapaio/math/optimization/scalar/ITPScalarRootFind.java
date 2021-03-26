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
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/26/21.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ITPScalarRootFind extends ScalarRootFind<ITPScalarRootFind> {

    public static ITPScalarRootFind newMethod() {
        return new ITPScalarRootFind();
    }

    private static final long serialVersionUID = -7479795009665905847L;

    public final ValueParam<Double, ITPScalarRootFind> a = new ValueParam<>(this,
            Double.NaN, "a", "first bracket parameter");
    public final ValueParam<Double, ITPScalarRootFind> b = new ValueParam<>(this,
            Double.NaN, "b", "second bracket parameter");
    public final ValueParam<Double, ITPScalarRootFind> eps = new ValueParam<>(this,
            1e-20, "eps", "tolerance used for assessing convergence");
    public final ValueParam<Integer, ITPScalarRootFind> maxIter = new ValueParam<>(this,
            10_000, "maxIter", "maximum number of iterations");

    public final ValueParam<Double, ITPScalarRootFind> k1 = new ValueParam<>(this,
            0.1, "k1", "k1 parameter", value -> value > 0 && Double.isFinite(value));
    public final ValueParam<Double, ITPScalarRootFind> k2 = new ValueParam<>(this,
            2.0, "k2", "k2 parameter", value -> value >= 1.0 && value <= 1.5 + 0.5 * Math.sqrt(5));
    public final ValueParam<Double, ITPScalarRootFind> n0 = new ValueParam<>(this,
            1.0, "n0", "n0 parameter", value -> value >= 0 && Double.isFinite(value));

    @Getter
    private double x;
    @Getter
    private boolean converged;
    @Getter
    private int iterations;

    @Override
    double optimize(Double2DoubleFunction f) {

        double xa = a.get();
        double xb = b.get();

        double fa = f.apply(xa);
        double fb = f.apply(xb);

        if (fa * fb > 0) {
            throw new IllegalArgumentException("Images of bracket endings have the same signum.");
        }

        // switch if not fa < fb
        if (fa > fb) {
            double tmp = fa;
            fa = fb;
            fb = tmp;
            tmp = xa;
            xa = xb;
            xb = tmp;
        }

        // pre-computations
        int n_half = (int) Math.ceil(Math.log((xb - xa) / (2 * eps.get())));
        double n_max = n_half + n0.get();
        int j = 0;

        while (xb - xa > 2 * eps.get() && j < maxIter.get()) {

            // computing parameters
            double x_half = xa + (xb - xa) / 2.0;
            double r = eps.get() * Math.pow(2, n_max - j) - (xb - xa) / 2;
            double delta = k1.get() * Math.pow(xb - xa, k2.get());

            // interpolation step
            double xf = (fb * xa - fa * xb) / (fb - fa);

            // truncation
            double sigma = Math.signum(x_half - xf);
            double xt = (delta <= Math.abs(x_half - xf)) ? xf + sigma * delta : x_half;

            // projection
            double x_itp = (Math.abs(xt - x_half) <= r) ? xt : x_half - sigma * delta;

            // update interval
            double y_itp = f.apply(x_itp);
            if (y_itp > 0) {
                xb = x_itp;
                fb = y_itp;
            } else if (y_itp < 0) {
                xa = x_itp;
                fa = y_itp;
            } else {
                xa = x_itp;
                xb = x_itp;
            }
            j++;
        }

        x = xa + (xb - xa) / 2;
        converged = j != maxIter.get();
        iterations = j;
        return x;
    }
}
