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

import static java.lang.StrictMath.floor;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.rint;
import static java.lang.StrictMath.sqrt;

import static rapaio.math.MathTools.incGammaC;
import static rapaio.math.MathTools.pdfPois;

import java.io.Serial;

import rapaio.printer.Format;

/**
 * Discrete probability distribution which expresses the probability of a
 * given number of events occuring in a fixed interval of time/space
 * if these events occur with a known average rate and independent
 * of the last occurrence of last events.
 */
public record Poisson(double lambda) implements Distribution {

    public static Poisson of(double lambda) {
        return new Poisson(lambda);
    }

    @Serial
    private static final long serialVersionUID = 2013039227493064895L;

    public Poisson {
        if (!(lambda > 0)) {
            throw new IllegalArgumentException("lambda parameter value must be a real positive value");
        }
    }

    @Override
    public String name() {
        return "Poisson(lambda=" + Format.floatFlex(lambda) + ")";
    }

    public double getLambda() {
        return lambda;
    }

    @Override
    public boolean discrete() {
        return true;
    }

    @Override
    public double pdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x < 0 || x == Double.POSITIVE_INFINITY)
            return 0.0;
        double xx = rint(x);
        if (xx != x)
            return 0;
        return pdfPois(x, lambda);
    }

    @Override
    public double cdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x < 0)
            return 0.0;
        if (x == Double.POSITIVE_INFINITY)
            return 1.0;
        return incGammaC(floor(x + 1), lambda);
    }

    /**
     * Quantile obtained by searching the cdf over the non-negative integers:
     * the smallest {@code k} with {@code cdf(k) >= p}.
     *
     * @throws IllegalArgumentException if {@code p} is not in {@code [0, 1]}
     */
    @Override
    public double quantile(double p) {
        QuantileSearch.checkProbability(p);
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        if (p <= cdf(0)) {
            return 0;
        }
        return QuantileSearch.discrete(this::cdf, p);
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double mean() {
        return lambda;
    }

    @Override
    public double mode() {
        return floor(lambda);
    }

    @Override
    public double var() {
        return lambda;
    }

    @Override
    public double skewness() {
        return 1.0 / sqrt(lambda);
    }

    @Override
    public double kurtosis() {
        return 1 / lambda;
    }

    /**
     * Above this rate the entropy is computed from its asymptotic expansion instead of by summation.
     * The first omitted term is of order {@code lambda^-4}, which is below {@code 1e-12} here, and
     * summing would need about {@code 80 sqrt(lambda)} terms whose count does not fit an {@code int}
     * for very large rates.
     */
    private static final double ENTROPY_ASYMPTOTIC_THRESHOLD = 1_000;

    /**
     * Shannon entropy in nats.
     * <p>
     * For small and moderate rates it is the direct sum {@code -sum_k p(k) log p(k)} over the range of
     * {@code k} where the pmf is not negligible, with the pmf taken from the saddle point evaluation
     * which is accurate to the last digits. The identity {@code lambda (1 - log lambda) + E[log k!]} is
     * not used, since both of its terms grow like {@code lambda log lambda} while their difference stays
     * of order {@code log lambda}, so rounding in {@code log(k!)} destroys the result for large rates.
     * <p>
     * For large rates the expansion
     * {@code 1/2 log(2 pi e lambda) - 1/(12 lambda) - 1/(24 lambda^2) - 19/(360 lambda^3)} is used.
     */
    @Override
    public double entropy() {
        if (lambda >= ENTROPY_ASYMPTOTIC_THRESHOLD) {
            return 0.5 * log(2 * Math.PI * Math.E * lambda)
                    - 1 / (12 * lambda)
                    - 1 / (24 * lambda * lambda)
                    - 19 / (360 * lambda * lambda * lambda);
        }
        int from = (int) max(0, floor(lambda - 40 * sqrt(lambda) - 40));
        int to = (int) (lambda + 40 * sqrt(lambda) + 40);
        double sum = 0;
        for (int k = from; k <= to; k++) {
            double pmf = pdfPois(k, lambda);
            if (pmf > 0) {
                sum -= pmf * log(pmf);
            }
        }
        return sum;
    }
}
