/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.distributions.du;

import jdistlib.Normal;
import rapaio.core.MathBase;
import rapaio.core.distributions.cu.Norm;

import static java.lang.Math.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static jdistlib.math.Constants.DBL_EPSILON;
import static jdistlib.math.MathFunctions.isInfinite;
import static rapaio.core.Constants.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class Binom implements DUDistribution {


    private final double p;
    private final double n;

    public Binom(double p, double n) {
        this.p = p;
        this.n = n;
    }

    @Override
    public String getName() {
        return "Bin(p=" + ((int) p) + ",n=" + ((int) n) + ")";
    }

    @Override
    public double pmf(double x) {
        if (x < min() || x > max()) return 0;
        return Math.exp(MathBase.logBinomial(x, n, p));
    }

    @Override
    public double cdf(double x) {
        if (x == n) return 1.0;
        return MathBase.betaIncReg(1 - p, n - x, x + 1);
    }

    @Override
    public double quantile(double p) {
        double pr = this.p;
        double q, mu, sigma, gamma, z, y;

        if (Double.isNaN(p) || Double.isNaN(n) || Double.isNaN(pr)) return p + n + pr;
        if (Double.isInfinite(n) || Double.isInfinite(pr)) return Double.NaN;
        /* if log_p is true, p = -Inf is a legitimate value */
        if (Double.isInfinite(p)) return Double.NaN;

        //if(n != floor(n + 0.5)) return Double.NaN;
        if (n != rint(n)) return Double.NaN;
        if (pr < 0 || pr > 1 || n < 0) return Double.NaN;

        // R_Q_P01_boundaries(p, 0, n);
        /* !log_p */
        if (p < 0 || p > 1)
            return Double.NaN;
        if (p == 0)
            return 0;
        if (p == 1)
            return n;

        if (pr == 0. || n == 0) return 0.;

        q = 1 - pr;
        if (q == 0.) return n; /* covers the full range of the distribution */
        mu = n * pr;
        sigma = sqrt(n * pr * q);
        gamma = (q - pr) / sigma;

		/* Note : "same" code in qpois.c, qbinom.c, qnbinom.c --
         * FIXME: This is far from optimal [cancellation for p ~= 1, etc]: */
        /* temporary hack --- FIXME --- */
        if (p + 1.01 * DBL_EPSILON >= 1.) return n;

		/* y := approx.value (Cornish-Fisher expansion) :  */
        z = new Norm(0, 1).quantile(p);
        //y = floor(mu + sigma * (z + gamma * (z*z - 1) / 6) + 0.5);
        y = rint(mu + sigma * (z + gamma * (z * z - 1) / 6));

        if (y > n) /* way off */ y = n;

        z = new Binom(pr, n).cdf(y);

		/* fuzz to ensure left continuity: */
        p *= 1 - 64 * DBL_EPSILON;

        double[] zp = new double[]{z};
        if (n < 1e5) return do_search(y, zp, p, n, pr, 1);
        /* Otherwise be a bit cleverer in the search */
        {
            double incr = floor(n * 0.001), oldincr;
            do {
                oldincr = incr;
                y = do_search(y, zp, p, n, pr, incr);
                incr = Math.max(1, floor(incr / 100));
            } while (oldincr > 1 && incr > n * 1e-15);
            return y;
        }
    }

    private static double do_search(double y, double[] z, double p, double n, double pr, double incr) {
        if (z[0] >= p) {
            /* search to the left */
            while (true) {
                double newz = new Binom(pr, n).cdf(y - incr);
                if (y == 0 || newz < p)
                    return y;
                y = Math.max(0, y - incr);
                z[0] = newz;
            }
        } else {		/* search to the right */
            while (true) {
                y = Math.min(y + incr, n);
                if (y == n || (z[0] = new Binom(pr, n).cdf(y)) >= p)
                    return y;
            }
        }
    }


    @Override
    public double min() {
        return 0;
    }

    @Override
    public double max() {
        return n;
    }

    @Override
    public double mean() {
        return n * p;
    }

    @Override
    public double mode() {
        double low = Math.floor((n + 1) * p);
        double p1 = pmf(low - 1);
        double p2 = pmf(low);
        return (p1 > p2) ? low - 1 : low;
    }

    @Override
    public double variance() {
        return n * p * (1 - p);
    }

    @Override
    public double skewness() {
        return (1 - 2 * p) / Math.sqrt(n * p * (1 - p));
    }

    @Override
    public double kurtosis() {
        return (1 - 6 * p * (1 - p)) / (n * p * (1 - p));
    }
}
