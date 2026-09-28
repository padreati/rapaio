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

package rapaio.core.tests;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

import static rapaio.printer.Format.floatFlex;
import static rapaio.printer.Format.pValueStars;

import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.transform.VarRefSort;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Hypothesis test which assess if a given samples belongs to a normal distribution.
 * Andreson-Darling test is based on A^2 distance statistic.
 * <p>
 * Two p-values are reported:
 * <ul>
 * <li>{@link #pValue()} treats the normal distribution as fully specified with the mean and
 * standard deviation actually used (given, or estimated when not given). It is exact only when
 * both parameters are given (Stephens' case 0).</li>
 * <li>{@link #getPValueStar()} accounts for parameter estimation. When both mean and standard
 * deviation are estimated (case 3) it uses the adjusted statistic
 * {@code A*^2 = A^2 (1 + 0.75/n + 2.25/n^2)} and the p-value formula of D'Agostino and Stephens
 * (1986), the same as R {@code nortest::ad.test}. When exactly one parameter is estimated
 * (cases 1 and 2) only critical value tables exist and {@code NaN} is returned. When none is
 * estimated it equals {@link #pValue()}.</li>
 * </ul>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/17.
 */
public class ADTestGoodness implements HTest {

    public static ADTestGoodness from(Var x) {
        return new ADTestGoodness(x, Double.NaN, Double.NaN);
    }

    public static ADTestGoodness from(Var x, double mu, double sigma) {
        return new ADTestGoodness(x, mu, sigma);
    }

    private final Var x;
    private final double mu;
    private final double sigma;

    private double muHat;
    private double sigmaHat;

    // statistics
    private double a2;
    private double a2star;

    private double pValue;
    private double pValueStar;

    private ADTestGoodness(Var x, double mu, double sigma) {
        Var xx = x.stream().complete().toMappedVar().copy();
        this.x = xx.fapply(VarRefSort.from(xx.refComparator())).copy();

        this.mu = mu;
        this.sigma = sigma;

        compute();
    }

    private void compute() {

        muHat = Double.isNaN(mu) ? Mean.of(x).value() : mu;

        if (!Double.isNaN(sigma)) {
            // variance is known
            sigmaHat = sigma;
        } else {
            if (!Double.isNaN(mu)) {
                // variance unknown, mean is known: maximum likelihood estimate around the known mean
                sigmaHat = 0.0;
                for (int i = 0; i < x.size(); i++) {
                    sigmaHat += Math.pow(x.getDouble(i) - mu, 2);
                }
                sigmaHat = Math.sqrt(sigmaHat / x.size());
            } else {
                // both variance and mean are unknown
                sigmaHat = Variance.of(x).sdValue();
            }
        }

        Var y = VarDouble.from(x, value -> (value - muHat) / sigmaHat);
        Normal normal = Normal.std();

        a2 = 0.0;
        int n = y.size();
        for (int i = 1; i <= n; i++) {
            double phi = normal.cdf(y.getDouble(i - 1));
            a2 += (2 * i - 1) * Math.log(phi) + (2 * (n - i) + 1) * Math.log(1 - phi);
        }
        a2 = -n - a2 / n;

        // p-value for the fully specified distribution (Stephens' case 0), Marsaglia & Marsaglia (2004)
        pValue = 1 - pvalue(a2, n);

        boolean bothEstimated = Double.isNaN(mu) && Double.isNaN(sigma);
        if (bothEstimated) {
            // Stephens' case 3: mean and variance estimated from the sample; adjusted statistic and
            // p-value from D'Agostino & Stephens (1986), Table 4.9 (as in R nortest::ad.test)
            a2star = a2 * (1.0 + 0.75 / n + 2.25 / (n * n));
            pValueStar = pvalueCase3(a2star);
        } else if (Double.isNaN(mu) || Double.isNaN(sigma)) {
            // Stephens' cases 1 and 2: only critical value tables exist, no p-value formula
            a2star = a2;
            pValueStar = Double.NaN;
        } else {
            a2star = a2;
            pValueStar = pValue;
        }
    }

    /**
     * p-value of the adjusted statistic when both mean and variance are estimated
     * (D'Agostino &amp; Stephens, Goodness-of-Fit Techniques, 1986, Table 4.9).
     */
    private static double pvalueCase3(double aa) {
        if (aa < 0.2) {
            return 1 - exp(-13.436 + 101.14 * aa - 223.73 * aa * aa);
        }
        if (aa < 0.34) {
            return 1 - exp(-8.318 + 42.796 * aa - 59.938 * aa * aa);
        }
        if (aa < 0.6) {
            return exp(0.9177 - 4.279 * aa - 1.38 * aa * aa);
        }
        if (aa < 10) {
            return exp(1.2937 - 5.709 * aa + 0.0186 * aa * aa);
        }
        return 3.7e-24;
    }

    /**
     * Computes p-value for an AD GoF test
     * From: ad.test.pvalue.r of ADGofTest
     */
    private double pvalue(double x, int n) {
        if (x < 2) {
            x = exp(-1.2337141 / x) / sqrt(x) * (2.00012 + (.247105 - (.0649821 - (.0347962 - (.011672 - .00168691 * x) * x) * x) * x) * x);
        } else {
            x = exp(-exp(1.0776 - (2.30695 - (.43424 - (.082433 - (.008056 - .0003146 * x) * x) * x) * x) * x));
        }

        if (x > 0.8) {
            return (x + (-130.2137 + (745.2337 - (1705.091 - (1950.646 - (1116.360 - 255.7844 * x) * x) * x) * x) * x) / n);
        }

        double z = -0.01265 + 0.1757 / n;

        if (x < z) {
            double v = x / z;
            v = sqrt(v) * (1. - v) * (49 * v - 102);
            return (x + v * (.0037 / (n * n) + .00078 / n + .00006) / n);
        }

        double v = (x - z) / (0.8 - z);
        v = -0.00022633 + (6.54034 - (14.6538 - (14.458 - (8.259 - 1.91864 * v) * v) * v) * v) * v;
        return x + v * (.04213 + .01365 / n) / n;
    }

    @Override
    public double pValue() {
        return pValue;
    }

    /**
     * @return the Anderson-Darling statistic {@code A^2} computed with the mean and standard
     * deviation actually used
     */
    public double a2() {
        return a2;
    }

    /**
     * @return the statistic adjusted for parameter estimation, {@code A*^2}; equals {@link #a2()}
     * unless both parameters were estimated
     */
    public double a2star() {
        return a2star;
    }

    public double getPValueStar() {
        return pValueStar;
    }

    @Override
    public double ciHigh() {
        return Double.NaN;
    }

    @Override
    public double ciLow() {
        return Double.NaN;
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {

        return "> ADTestGoodness\n"
                + "\n"
                + "Anderson-Darling GoF Test\n"
                + "\n"
                + "Null hypothesis:\n"
                + "  sample is normally distributed\n"
                + "\n"
                + "sample size: " + x.size() + "\n"
                + "given mean: " + floatFlex(mu) + ", used mean : " + floatFlex(muHat) + "\n"
                + "given sd  : " + floatFlex(sigma) + ", used sd   : " + floatFlex(sigmaHat) + "\n"
                + "\n"
                + "A^2  statistic: "
                + floatFlex(a2)
                + ", p-value: "
                + floatFlex(pValue)
                + " "
                + pValueStars(pValue)
                + "\n"
                + "A*^2 statistic: "
                + floatFlex(a2star)
                + ", p-value: "
                + floatFlex(pValueStar)
                + " "
                + pValueStars(pValueStar)
                + "\n"
                + "\n";
    }
}
