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
import rapaio.printer.Format;

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
        if (!Double.isFinite(p) || p < 0 || p > 1) {
            throw new IllegalArgumentException("Probability must have a finite value in range [0,1].");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("Number of samples must be a positive integer value.");
        }
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
        if (x >= n) {
            return 1.0;
        }
        x = MTools.floor(x);
        return betaIncReg(1 - p, n - x, x + 1);
    }

    @Override
    public double quantile(double probability) {
        /* if log_p is true, p = -Inf is a legitimate value */
        if (!Double.isFinite(probability)) {
            return Double.NaN;
        }

        // R_Q_P01_boundaries(p, 0, n);
        /* !log_p */
        if (probability < 0 || probability > 1)
            return Double.NaN;
        if (probability == 0)
            return 0;
        if (probability == 1)
            return n;

        if (p == 0. || n == 0) return 0.;

        double q = 1 - p;
        if (q == 0.) return n; /* covers the full range of the densities */
        double mu = n * p;
        double sigma = sqrt(n * p * q);
        double gamma = (q - p) / sigma;

        /* Note : "same" code in qpois.c, qbinom.c, qnbinom.c --
         * FIXME: This is far from optimal [cancellation for p ~= 1, etc]: */
        /* temporary hack --- FIXME --- */
        if (probability + 1.01 * DBL_EPSILON >= 1.) return n;

        /* y := approx.value (Cornish-Fisher expansion) :  */
        double z = Normal.std().quantile(probability);
        //y = floor(mu + sigma * (z + gamma * (z*z - 1) / 6) + 0.5);
        double y = rint(mu + sigma * (z + gamma * (z * z - 1) / 6));

        if (y > n) /* way off */ y = n;

        z = Binomial.of(p, n).cdf(y);

        /* fuzz to ensure left continuity: */
        probability *= 1 - 64 * DBL_EPSILON;

        double[] zp = new double[]{z};
        if (n < 1e5) return doSearch(y, zp, probability, 1);
        /* Otherwise be a bit cleverer in the search */
        double incr = floor(n * 0.001), oldincr;
        do {
            oldincr = incr;
            y = doSearch(y, zp, probability, incr);
            incr = Math.max(1, floor(incr / 100));
        } while (oldincr > 1 && incr > n * 1e-20);
        return y;
    }

    private double doSearch(double y, double[] z, double probability, double incr) {
        if (z[0] >= probability) {
            /* search to the left */
            while (true) {
                double newz = cdf(y - incr);
                if (y == 0 || newz < probability)
                    return y;
                y = Math.max(0, y - incr);
                z[0] = newz;
            }
        } else {        /* search to the right */
            while (true) {
                y = Math.min(y + incr, n);
                if (y == n || (z[0] = cdf(y)) >= probability)
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
