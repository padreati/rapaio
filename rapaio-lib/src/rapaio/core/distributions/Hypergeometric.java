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

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.floor;
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
        if (Double.isInfinite(x)) {
            throw new IllegalArgumentException("x should be an integer since the hypergeometric" +
                    " repartition is a discrete repartion.");
        }
        int xx = (int) rint(x);
        if (abs(xx - x) > 1e-30)
            return 0.0;
        if ((xx > m) || (xx > k) || (xx < k - n))
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
        if (Double.isInfinite(x)) {
            return x > 0 ? 1.0 : 0.0;
        }
        if (x > m) return 1.0;
        if (x > k) return 1.0;
        if (x > n) return 1.0;

        double cdf = 0;
        for (int i = 0; i <= x; i++) {
            cdf += pdf(i);
        }
        return cdf;
    }

    @Override
    public double quantile(double p) {
        double cdf = 0;
        for (int i = 0; i <= m; ++i) {
            cdf += pdf(i);
            if (cdf > p) {
                return i;
            }
        }
        return m;
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return m;
    }

    @Override
    public double mean() {
        return (double) (m * k) / (m + n);
    }

    @Override
    public double mode() {
        return floor((double) (k + 1) * (m + 1) / (n + m + 2));
    }

    /*
       According to http://mathworld.wolfram.com/HypergeometricDistribution.html
       the variance of a random variable X ~ Hypergeometric(m, n, k) is
       var(X) = ( n * m *  k * ( n + m - k ) ) / (( (n + m) ^ 2) * (n + m - 1))
     */
    @Override
    public double var() {
        return (n * m * k * (n + m - k)) / (pow((n + m), 2) * (n + m - 1));
    }

    @Override
    public double skewness() {
        return sqrt((double) (n + m - 1) / (n * m * k * (n + m - k)));
    }

    /*
       Computing the kurtosis using the formula from this Wikipedia page:
       https://en.wikipedia.org/wiki/Hypergeometric_distribution
     */
    @Override
    public double kurtosis() {
        double total = m + n;
        double firstTerm = k * m * n * (total - k) * (total - 2) * (total - 3);
        double secondTerm = (total - 1) * Math.pow(total, 2) * (total * (total + 1)
                - 6 * m * n - 6 * k * (total - k)) + 6 * k * m * n * (total - k)
                * (5 * total - 6);
        return secondTerm / firstTerm;
    }

    @Override
    public double entropy() {
        return Double.NaN;
    }
}