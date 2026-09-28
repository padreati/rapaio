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
import static java.lang.StrictMath.floor;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.rint;
import static java.lang.StrictMath.sqrt;

import java.io.Serial;
import java.util.Arrays;

/**
 * Hypergeometric distribution
 * <p>
 * Created by andrei on 13.11.2015.
 * Improved by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Hypergeometric implements Distribution {

    /**
     * Builds a hypergeometric distribution.
     *
     * @param m number of white balls
     * @param n number of black balls
     * @param k number of draws
     * @return instance of hypergeometric distribution
     */
    public static Hypergeometric of(int m, int n, int k) {
        return new Hypergeometric(m, n, k);
    }

    @Serial
    private static final long serialVersionUID = 5557359074330033049L;

    private final int m; // the number of white balls from the urn
    private final int n; // the number of black balls from the urn
    private final int k; // the number of balls drawn from the urn

    private final double[] pdfCache;

    /**
     * Instantiates a hypergeometric distribution
     *
     * @param m the number of white / success balls from the urn
     * @param n the number of black / not success balls from the urn
     * @param k the number of draws
     */
    private Hypergeometric(int m, int n, int k) {
        if (m < 0) {
            throw new IllegalArgumentException("m parameter should not be negative.");
        }
        if (n < 0) {
            throw new IllegalArgumentException("n parameter should not be negative.");
        }
        if (n + m < 1) {
            throw new IllegalArgumentException("m + n should be at least 1.");
        }
        if (k < 0) {
            throw new IllegalArgumentException("k parameter should not be negative.");
        }
        if (k > m + n) {
            throw new IllegalArgumentException("Size of sample k should be at most m + n.");
        }
        this.m = m;
        this.n = n;
        this.k = k;

        pdfCache = new double[k + 1];
        Arrays.fill(pdfCache, Double.NaN);
    }


    @Override
    public String name() {
        return "Hypergeometric(m=" + m + ",n=" + n + ",k=" + k + ")";
    }

    @Override
    public boolean discrete() {
        return true;
    }

    /**
     * This algorithm is not found on any literature, it is simply a development
     * of combined multiplication of combinations.
     *
     * @param x value for which it calculates the probability density function
     * @return computed value
     */
    @Override
    public double pdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (Double.isInfinite(x)) {
            return 0.0;
        }
        int xx = (int) rint(x);
        if (abs(xx - x) > 1e-30)
            return 0.0;
        if ((xx < 0) || (xx > m) || (xx > k) || (xx < k - n))
            return 0.0;
        if (!Double.isNaN(pdfCache[xx])) {
            return pdfCache[xx];
        }

        int[] up = new int[m + n + 1];
        int[] down = new int[m + n + 1];
        for (int i = 1; i <= m + n; i++) {
            int j = 0;
            if (i <= m)
                j++;
            if (i <= m - xx)
                j--;
            if (i <= xx)
                j--;
            if (i <= n)
                j++;
            if (i <= n - k + xx)
                j--;
            if (i <= k - xx)
                j--;
            if (i <= m + n - k)
                j++;
            if (i <= k)
                j++;
            if (i <= m + n)
                j--;
            if (j == 0)
                continue;
            if (j > 0) {
                up[i] += j;
            } else {
                down[i] -= j;
            }
        }
        for (int i = 0; i < m + n + 1; i++) {
            int min = min(up[i], down[i]);
            if (min > 0) {
                up[i] -= min;
                down[i] -= min;
            }
        }

        double prod = 1.0;
        int posUp = 0;
        int posDown = 0;
        while (posUp < m + n + 1) {
            if (up[posUp] > 0) break;
            posUp++;
        }
        while (posDown < m + n + 1) {
            if (down[posDown] > 0) break;
            posDown++;
        }

        while (true) {
            if (posUp > m + n) {
                while (posDown < m + n + 1) {
                    if (down[posDown] > 0)
                        prod /= pow(posDown, down[posDown]);
                    posDown++;
                }
                break;
            }
            if (posDown > m + n) {
                while (posUp < m + n + 1) {
                    if (up[posUp] > 0)
                        prod *= pow(posUp, up[posUp]);
                    posUp++;
                }
                break;
            }
            if (prod >= 10) {
                prod /= posDown;
                down[posDown]--;
                if (down[posDown] == 0) {
                    do {
                        posDown++;
                    }
                    while (posDown < m + n + 1 && down[posDown] == 0);
                }
            } else {
                prod *= posUp;
                up[posUp]--;
                if (up[posUp] == 0) {
                    do {
                        posUp++;
                    }
                    while (posUp < m + n + 1 && up[posUp] == 0);
                }
            }
        }
        pdfCache[xx] = prod;
        return prod;
    }

    @Override
    public double cdf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x < minValue()) {
            return 0.0;
        }
        if (x >= maxValue()) {
            return 1.0;
        }
        double cdf = 0;
        int upper = (int) floor(x);
        for (int i = (int) minValue(); i <= upper; i++) {
            cdf += pdf(i);
        }
        return cdf;
    }

    @Override
    public double quantile(double p) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("Probability value should lie in [0,1] interval");
        }
        int upper = (int) maxValue();
        double cdf = 0;
        for (int i = (int) minValue(); i < upper; ++i) {
            cdf += pdf(i);
            if (cdf >= p) {
                return i;
            }
        }
        return upper;
    }

    /**
     * @return the smallest value with positive probability, {@code max(0, k - n)}
     */
    @Override
    public double minValue() {
        return max(0, k - n);
    }

    /**
     * @return the largest value with positive probability, {@code min(m, k)}
     */
    @Override
    public double maxValue() {
        return min(m, k);
    }

    @Override
    public double mean() {
        return (double) m * k / ((double) m + n);
    }

    @Override
    public double mode() {
        return floor((double) (k + 1) * (m + 1) / (n + m + 2));
    }

    /*
       With N = m + n, X ~ Hypergeometric(m, n, k) has
       var(X) = k m n (N - k) / (N^2 (N - 1))
       skewness(X) = (N - 2m) sqrt(N - 1) (N - 2k) / (sqrt(k m n (N - k)) (N - 2))
       (https://en.wikipedia.org/wiki/Hypergeometric_distribution). All products are
       evaluated in double to avoid int overflow for large urns.
     */
    @Override
    public double var() {
        double N = (double) m + n;
        if (N <= 1) {
            return 0;
        }
        return (double) k * m * n * (N - k) / (N * N * (N - 1));
    }

    @Override
    public double skewness() {
        double N = (double) m + n;
        double denom = sqrt((double) k * m * n * (N - k)) * (N - 2);
        if (denom == 0) {
            return Double.NaN;
        }
        return (N - 2 * m) * sqrt(N - 1) * (N - 2 * k) / denom;
    }

    /*
       Computing the kurtosis using the formula from this Wikipedia page:
       https://en.wikipedia.org/wiki/Hypergeometric_distribution
     */
    @Override
    public double kurtosis() {
        double total = (double) m + n;
        double firstTerm = (double) k * m * n * (total - k) * (total - 2) * (total - 3);
        double secondTerm = (total - 1) * total * total * (total * (total + 1)
                - 6.0 * m * n - 6.0 * k * (total - k)) + 6.0 * k * m * n * (total - k)
                * (5 * total - 6);
        return secondTerm / firstTerm;
    }

    /**
     * @return Shannon entropy in nats, {@code -sum p(x) log p(x)} over the support
     */
    @Override
    public double entropy() {
        double h = 0;
        for (int x = (int) minValue(); x <= (int) maxValue(); x++) {
            double p = pdf(x);
            if (p > 0) {
                h -= p * log(p);
            }
        }
        return h;
    }
}