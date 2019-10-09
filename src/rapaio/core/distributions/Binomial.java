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

package rapaio.core.distributions;

import rapaio.math.MTools;
import rapaio.printer.format.Format;

import static rapaio.math.MTools.*;

/**
 * Binomial distribution.
 * It models the number of successes from n trials, where all trials
 * are independent Bernoulli random variables with parameter p.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class Binomial implements Distribution {

    public static Binomial of(double p, int n) {
        return new Binomial(p, n);
    }

    private static final long serialVersionUID = 8813621560796556828L;
    private final double p;
    private final int n;

    private Binomial(double p, int n) {
        this.p = p;
        this.n = n;
    }

    @Override
    public boolean discrete() {
        return true;
    }

    @Override
    public String name() {
        return "Binomial(p=" + Format.floatFlex(p) + ",n=" + n + ")";
    }

    @Override
    public double pdf(double x) {
        if (x < min() || x > max()) return 0;
        if (Math.abs(Math.rint(x) - x) < 1e-12)
            return Math.exp(logBinomial(x, n, p));
        return 0.0;
    }

    @Override
    public double cdf(double x) {
        if (x >= n)
            return 1.0;
        x = MTools.floor(x);
        return betaIncReg(1 - p, n - x, x + 1);
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
        if (q == 0.) return n; /* covers the full range of the densities */
        mu = n * pr;
        sigma = sqrt(n * pr * q);
        gamma = (q - pr) / sigma;

        /* Note : "same" code in qpois.c, qbinom.c, qnbinom.c --
         * FIXME: This is far from optimal [cancellation for p ~= 1, etc]: */
        /* temporary hack --- FIXME --- */
        if (p + 1.01 * DBL_EPSILON >= 1.) return n;

        /* y := approx.value (Cornish-Fisher expansion) :  */
        z = Normal.std().quantile(p);
        //y = floor(mu + sigma * (z + gamma * (z*z - 1) / 6) + 0.5);
        y = rint(mu + sigma * (z + gamma * (z * z - 1) / 6));

        if (y > n) /* way off */ y = n;

        z = Binomial.of(pr, n).cdf(y);

        /* fuzz to ensure left continuity: */
        p *= 1 - 64 * DBL_EPSILON;

        double[] zp = new double[]{z};
        if (n < 1e5) return doSearch(y, zp, p, n, pr, 1);
        /* Otherwise be a bit cleverer in the search */
        double incr = floor(n * 0.001), oldincr;
        do {
            oldincr = incr;
            y = doSearch(y, zp, p, n, pr, incr);
            incr = Math.max(1, floor(incr / 100));
        } while (oldincr > 1 && incr > n * 1e-20);
        return y;
    }

    private static double doSearch(double y, double[] z, double p, int n, double pr, double incr) {
        if (z[0] >= p) {
            /* search to the left */
            while (true) {
                double newz = Binomial.of(pr, n).cdf(y - incr);
                if (y == 0 || newz < p)
                    return y;
                y = Math.max(0, y - incr);
                z[0] = newz;
            }
        } else {        /* search to the right */
            while (true) {
                y = Math.min(y + incr, n);
                if (y == n || (z[0] = Binomial.of(pr, n).cdf(y)) >= p)
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
        double low = floor((n + 1) * p);
        double p1 = pdf(low - 1);
        double p2 = pdf(low);
        return (p1 > p2) ? low - 1 : low;
    }

    @Override
    public double var() {
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

    /**
     * The wikipedia dedicated page (http://en.wikipedia.org/wiki/Binomial_distribution)
     * states that entropy for binomial is:
     * $$\frac1 2 \log_2 \big( 2\pi e\, np(1-p) \big) + O \left( \frac{1}{n} \right)$$
     * <p>
     * According to this page is lighter to use an approximation. The following page
     * http://math.stackexchange.com/questions/244455/entropy-of-a-binomial-distribution
     * documents how this entropy is approximated.
     *
     * @return entropy value
     */
    @Override
    public double entropy() {
        return log(2 * Math.PI * Math.E * n * p * (1 - p)) / (2.0 * Math.log(2));
    }
}
