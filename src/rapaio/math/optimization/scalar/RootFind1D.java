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

import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.util.NotImplementedException;
import rapaio.util.function.Double2DoubleFunction;

import java.io.Serial;
import java.io.Serializable;

/**
 * Models a root finding algorithm for a single dimensional function
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/26/21.
 */
public class RootFind1D extends ParamSet<RootFind1D> implements Serializable {

    public static RootFind1D newModel() {
        return new RootFind1D();
    }

    @Serial
    private static final long serialVersionUID = -2933255484925187026L;

    public final ValueParam<Double, RootFind1D> x0 = new ValueParam<>(this,
            Double.NaN, "a", "bracket first point if the algorithm requires brackets or an initial estimation of the root");
    public final ValueParam<Double, RootFind1D> x1 = new ValueParam<>(this,
            Double.NaN, "b", "bracket second point if the agorithm requires brackets or a second estimator for the root");
    public final ValueParam<Double, RootFind1D> eps = new ValueParam<>(this,
            1e-15, "eps", "tolerance for convergence approximation of the solution");
    public final ValueParam<Integer, RootFind1D> maxIter = new ValueParam<>(this,
            100_000, "maxIter", "maximum number of iterations");
    public final ValueParam<Double, RootFind1D> k1 = new ValueParam<>(this,
            0.1, "k1", "k1 parameter for method brent", value -> value > 0 && Double.isFinite(value));
    public final ValueParam<Double, RootFind1D> k2 = new ValueParam<>(this,
            2.0, "k2", "k2 parameter for method brent", value -> value >= 1.0 && value <= 1.5 + 0.5 * Math.sqrt(5));
    public final ValueParam<Double, RootFind1D> n0 = new ValueParam<>(this,
            1.0, "n0", "n0 parameter for method brent only", value -> value >= 0 && Double.isFinite(value));

    public final ValueParam<Method, RootFind1D> method = new ValueParam<>(this,
            Method.ITP, "method", "method usd for scalar root finding");

    private double x;
    private boolean converged;
    private int iterations;

    public enum Method {
        Bisection,
        RegulaFalsi,
        ITP,
        Secant,
        Brent,
        Ridder
    }

    private RootFind1D() {
    }

    public double getX() {
        return x;
    }

    public boolean isConverged() {
        return converged;
    }

    public int getIterations() {
        return iterations;
    }

    public double optimize(Double2DoubleFunction f) {
        return switch (method.get()) {
            case Bisection -> bisection_optimize(f);
            case RegulaFalsi -> regula_falsi_optimize(f);
            case ITP -> itp_optimize(f);
            case Secant -> secant_optimization(f);
            case Brent -> brent_optimization(f);
            case Ridder -> ridder_optimization(f);
            default -> throw new NotImplementedException();
        };
    }

    private double bisection_optimize(Double2DoubleFunction f) {
        if (!Double.isFinite(x0.get()) || !Double.isFinite(x1.get())) {
            throw new IllegalArgumentException("Bracket values must be finite.");
        }

        double xa = x0.get();
        double xb = x1.get();

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

    private double regula_falsi_optimize(Double2DoubleFunction f) {

        double r = Double.NaN;
        int side = 0;
        /* starting values at endpoints of interval */
        double xa = x0.get();
        double xb = x1.get();
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

    private double itp_optimize(Double2DoubleFunction f) {

        double xa = x0.get();
        double xb = x1.get();

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

        while (Math.abs(xb - xa) > 2 * eps.get() && j < maxIter.get()) {

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

    private double secant_optimization(Double2DoubleFunction f) {

        if (!Double.isFinite(x0.get()) || !Double.isFinite(x1.get())) {
            throw new IllegalArgumentException("Provided initial values are not finite.");
        }

        double xa = x0.get();
        double xb = x1.get();
        double fa = f.apply(xa);
        double fb = f.apply(xb);

        if (fa * fb > 0) {
            throw new IllegalArgumentException("Images of the initial points have the same signum.");
        }

        for (int it = 0; it < maxIter.get(); it++) {
            if (Math.abs(xb - xa) < eps.get()) {
                x = (xa + xb) / 2;
                iterations = it;
                converged = true;
                return x;
            }
            double xc = (xa * fb - xb * fa) / (fb - fa);
            xa = xb;
            xb = xc;
        }
        x = (xa + xb) / 2;
        iterations = maxIter.get();
        converged = false;
        return x;
    }

    private double brent_optimization(Double2DoubleFunction f) {

        // another tolerance parameter
        double delta = 1e-100;

        double xa = x0.get();
        double xb = x1.get();

        double fa = f.apply(xa);
        double fb = f.apply(xb);

        if (fa == 0) {
            x = xa;
            iterations = 0;
            converged = true;
            return x;
        }

        if (fa * fb > 0) {
            throw new IllegalArgumentException("Imagines of the bracket points does not have opposite signs.");
        }

        if (Math.abs(fa) < Math.abs(fb)) {
            // swap a and b
            double tmp = xa;
            xa = xb;
            xb = tmp;
            tmp = fa;
            fa = fb;
            fb = tmp;
        }

        double xc = xa;
        boolean mflag = true;
        double d = Double.NaN; // just to make sure, it should not be called in the first iteration

        for (int it = 0; it < maxIter.get(); it++) {
            if (fb == 0 || Math.abs(xb - xa) < eps.get()) {
                x = xb;
                iterations = it;
                converged = true;
                return x;
            }

            double fc = f.apply(xc);
            double s;
            if ((fa != fc) && (fb != fc)) {
                // inverse quadratic
                s = xa * fb * fc / ((fa - fb) * (fa - fc))
                        + xb * fa * fc / ((fb - fa) * (fb - fc))
                        + xc * fa * fb / ((fc - fa) * (fc - fb));
            } else {
                // secant
                s = xb - fb * (xb - xa) / (fb - fa);
            }

            if ((s < (3 * xa + xb) / 4 || s > xb)
                    || (mflag && Math.abs(s - xb) >= Math.abs(xb - xc) / 2)
                    || (!mflag && Math.abs(s - xb) >= Math.abs(xc - d) / 2)
                    || (mflag && Math.abs(xb - xc) < Math.abs(delta))
                    || (!mflag && Math.abs(xc - d) < Math.abs(delta))
            ) {
                s = (xa + xb) / 2;
                mflag = true;
            } else {
                mflag = false;
            }

            double fs = f.apply(s);
            d = xc;
            xc = xb;
            if (fa * fs < 0) {
                xb = s;
                fb = fs;
            } else {
                xa = s;
                fa = fs;
            }
            if (Math.abs(fa) < Math.abs(fb)) {
                // swap a and b
                double tmp = xa;
                xa = xb;
                xb = tmp;
                tmp = fa;
                fa = fb;
                fb = tmp;
            }
        }

        x = xb;
        iterations = maxIter.get();
        converged = false;
        return x;
    }

    private double ridder_optimization(Double2DoubleFunction f) {

        double x1 = x0.get();
        double x2 = this.x1.get();

        double f1 = f.apply(x1);
        double f2 = f.apply(x2);

        if (f1 * f2 > 0) {
            throw new IllegalArgumentException("Imagines of the bracket ends does not have opposite signs.");
        }

        for (int it = 0; it < maxIter.get(); it++) {
            if (Math.abs(x1 - x2) < eps.get()) {
                // convergent solution
                x = (x1 + x2) / 2;
                converged = true;
                iterations = it;
                return x;
            }
            double x3 = 0.5 * (x1 + x2);
            double f3 = f.apply(x3);

            // this is one end of the new bracket interval
            double x4 = x3 + Math.signum(f1) * f3 * (x3 - x1) / Math.sqrt(f3 * f3 - f1 * f2);
            double f4 = f.apply(x4);

            double x5 = Double.NaN;
            double delta = Double.NaN;

            if (f4 == 0) {
                x = x4;
                iterations = it;
                converged = true;
                return x;
            }

            // for the second bracket point choose the closest alternative which is a proper bracket
            if (f4 * f3 < 0) {
                x5 = x3;
                delta = Math.abs(x4 - x3);
            }
            if (f4 * f1 < 0 && (Double.isNaN(delta) || delta > Math.abs(x4 - x1))) {
                x5 = x1;
                delta = Math.abs(x4 - x1);
            }
            if (f4 * f2 < 0 && (Double.isNaN(delta) || delta > Math.abs(x4 - x2))) {
                x5 = x2;
            }

            x1 = x4;
            f1 = f4;
            x2 = x5;
            f2 = f.apply(x5);
        }

        x = (x1 + x2) / 2;
        converged = false;
        iterations = maxIter.get();
        return x;
    }
}
