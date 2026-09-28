/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import java.util.function.DoubleUnaryOperator;

/**
 * Quantile computation by inverting a cdf numerically, for distributions
 * without a closed form or a dedicated inverse routine.
 * <p>
 * Both searches return {@code inf { x : cdf(x) >= p }} for a non-decreasing
 * cdf over a support starting at {@code 0}: the upper bound is first found
 * by doubling, then the bracket is bisected until it cannot shrink any
 * further (adjacent doubles, or adjacent integers for the discrete case).
 * The doubling stops as soon as the bound is no longer finite, so a cdf that
 * never reaches {@code p} yields {@code +Inf} rather than a hang; the
 * bisection stops on the bracket width, not on the value of the cdf, so it
 * terminates for any cdf, including one that is flat or coarse near the
 * solution.
 */
final class QuantileSearch {

    private QuantileSearch() {
    }

    /**
     * Validates a probability given to a quantile function.
     *
     * @param p probability
     * @throws IllegalArgumentException if {@code p} is not in {@code [0, 1]} (NaN included)
     */
    static void checkProbability(double p) {
        if (!(p >= 0 && p <= 1)) {
            throw new IllegalArgumentException("Probability value should lie in [0,1] interval, not " + p);
        }
    }

    /**
     * Smallest {@code x >= 0} with {@code cdf(x) >= p} for a continuous cdf,
     * up to the resolution of doubles.
     *
     * @param cdf non-decreasing function with {@code cdf(0) <= p} and support {@code [0, +Inf)}
     * @param p   probability in {@code (0, 1)}
     * @return quantile, {@code +Inf} if the cdf never reaches {@code p}
     */
    static double continuous(DoubleUnaryOperator cdf, double p) {
        double low = 0;
        double up = 1;
        while (cdf.applyAsDouble(up) < p) {
            low = up;
            up *= 2;
            if (!Double.isFinite(up)) {
                return Double.POSITIVE_INFINITY;
            }
        }
        while (true) {
            double mid = low + (up - low) / 2;
            if (mid <= low || mid >= up) {
                return up;
            }
            if (cdf.applyAsDouble(mid) < p) {
                low = mid;
            } else {
                up = mid;
            }
        }
    }

    /**
     * Smallest non-negative integer {@code k} with {@code cdf(k) >= p} for a
     * discrete cdf on the non-negative integers.
     *
     * @param cdf non-decreasing function evaluated at integers, with {@code cdf(0) < p}
     * @param p   probability in {@code (0, 1)}
     * @return quantile as an integer valued double, {@code +Inf} if the cdf never reaches {@code p}
     */
    static double discrete(DoubleUnaryOperator cdf, double p) {
        double low = 0;
        double up = 1;
        while (cdf.applyAsDouble(up) < p) {
            low = up;
            up *= 2;
            if (!Double.isFinite(up)) {
                return Double.POSITIVE_INFINITY;
            }
        }
        while (up - low > 1) {
            double mid = Math.floor(low + (up - low) / 2);
            if (mid <= low || mid >= up) {
                // beyond 2^53 consecutive integers are not representable; up is the best answer
                return up;
            }
            if (cdf.applyAsDouble(mid) < p) {
                low = mid;
            } else {
                up = mid;
            }
        }
        return up;
    }
}
