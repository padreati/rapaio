/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.log1p;
import static java.lang.StrictMath.min;
import static java.lang.StrictMath.sqrt;

import static rapaio.math.MathTools.PI;
import static rapaio.math.MathTools.betaIncReg;
import static rapaio.math.MathTools.digamma;
import static rapaio.math.MathTools.invBetaIncRegLogit;
import static rapaio.math.MathTools.lnBeta;
import static rapaio.math.MathTools.lnBetaIncRegLogit;
import static rapaio.math.MathTools.lnGamma;

import java.io.Serial;

import rapaio.printer.Format;

/**
 * Student's T distribution, or T distribution.
 * This distribution arises when a gaussian distribution is approximated
 * for small sample size or when the standard deviation of the population is not known.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class StudentT implements Distribution {

    public static StudentT of(double df) {
        return new StudentT(df, 0, 1);
    }

    public static StudentT of(double df, double mu, double sigma) {
        return new StudentT(df, mu, sigma);
    }

    @Serial
    private static final long serialVersionUID = 2573925611489986427L;

    private final double df;
    private final double mu;
    private final double sigma;

    private StudentT(double df, double mu, double sigma) {
        if (!(df > 0)) {
            throw new IllegalArgumentException("degrees of freedom in student t distribution must have a value greater than 0.");
        }
        // a zero scale is the degenerate distribution, with all the mass on mu (a test of a constant sample
        // builds it); a negative scale has no meaning. NaN is not rejected, the tests derive the scale from
        // sample statistics which are NaN for a sample too small to have one
        if (sigma < 0) {
            throw new IllegalArgumentException("scale of the student t distribution must not be negative, got: " + Format.floatFlex(sigma) + ".");
        }
        this.df = df;
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public String name() {
        return "StudentT(df=" + Format.floatFlex(df) +
                ", mu=" + Format.floatFlex(mu) +
                ", sigma=" + Format.floatFlex(sigma) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double t) {
        if (sigma == 0) {
            // degenerate distribution, see Normal
            return Double.isNaN(t) ? Double.NaN : (t == mu ? Double.POSITIVE_INFINITY : 0.0);
        }
        double z = (t - mu) / sigma;
        double w = abs(z);
        // log(1 + z^2 / df); the square of a large z overflows long before the density does
        double logOnePlusSquare = w > 1e150 ? 2 * log(w) - log(df) : log1p(w * w / df);
        return exp(lnGamma((df + 1) / 2) - lnGamma(df / 2) - log(df * PI) / 2 - log(sigma)
                - (df + 1) / 2 * logOnePlusSquare);
    }

    @Override
    public double cdf(double t) {
        if (sigma == 0) {
            // degenerate distribution, a right continuous step at mu
            return Double.isNaN(t) ? Double.NaN : (t < mu ? 0.0 : 1.0);
        }
        double z = (t - mu) / sigma;
        if (Double.isNaN(z)) {
            return Double.NaN;
        }
        double w = abs(z);
        // the probability of the tail beyond |z|, in both directions, is half of I_x(df/2, 1/2) with
        // x = df / (df + z^2)
        double tail;
        if (Double.isInfinite(w)) {
            tail = 0;
        } else {
            double x = df / (df + w * w);
            if (x > 1e-250) {
                tail = betaIncReg(x, df / 2, 0.5) / 2;
            } else {
                // z^2 overflows or x is too small to be a usable double; the log odds of x are exactly
                // log(df) - 2 log|z|, which has neither problem
                tail = exp(lnBetaIncRegLogit(log(df) - 2 * log(w), df / 2, 0.5)) / 2;
            }
        }
        if (t > mu) {
            return 1 - tail;
        } else {
            return tail;
        }
    }

    @Override
    public double quantile(double p) {
        if (!(p >= 0 && p <= 1)) {
            throw new IllegalArgumentException("Probability must be in the range [0,1]");
        }
        // the support is unbounded, so the boundary probabilities map to the infinities; the root search
        // below has no bracket for them
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        if (p + 1e-20 >= 0.5 && p - 1e-20 <= 0.5) {
            return mu;
        }
        // the tail probability of |t| is I_x(df/2, 1/2) with x = df / (df + t^2), hence t^2 = df * (1 - x) / x.
        // Computing x loses the precision of extreme tails, since x is then tiny or not distinguishable from 1,
        // while the log odds u = log(x / (1 - x)) keep it: t^2 = df * exp(-u)
        double u = invBetaIncRegLogit(2 * min(p, 1 - p), df / 2, 0.5);
        double x = sigma * sqrt(df) * exp(-u / 2);
        if (p >= 0.5) {
            return mu + x;
        } else {
            return mu - x;
        }
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double mean() {
        return mu;
    }

    @Override
    public double mode() {
        return mu;
    }

    @Override
    public double skewness() {
        if (df <= 3) {
            return Double.NaN;
        }
        return 0;
    }

    @Override
    public double var() {
        if (df <= 1) {
            return Double.NaN;
        }
        if (df <= 2) {
            return Double.POSITIVE_INFINITY;
        }
        return df / (df - 2) * sigma * sigma;
    }

    @Override
    public double kurtosis() {
        if (df <= 2) {
            return Double.NaN;
        }
        if (df <= 4) {
            return Double.POSITIVE_INFINITY;
        }
        return 6 / (df - 4);
    }

    /**
     * @return differential entropy in nats:
     * {@code (df+1)/2 (psi((df+1)/2) - psi(df/2)) + log(sqrt(df) B(df/2, 1/2)) + log(sigma)}
     */
    @Override
    public double entropy() {
        return (df + 1) / 2 * (digamma((df + 1) / 2) - digamma(df / 2)) + log(df) / 2 + lnBeta(df / 2, 0.5) + log(sigma);
    }
}
