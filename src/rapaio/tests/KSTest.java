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

package rapaio.tests;

import rapaio.core.Printable;
import rapaio.core.distributions.Distribution;
import rapaio.data.Vector;
import rapaio.data.filters.BaseFilters;

/**
 * Creates a new statistical Kolmogorov-Smirnoff test. The 1 sample test, with <tt>v</tt>
 * being the 1 sample. The 1 sample test compare the data to a given distribution,
 * and see if it does not belong to the given distribution. The 2 sample test is
 * designed to tell if the data is not from the same population.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class KSTest implements Printable {

    private final Distribution cdf;
    private final Vector v1;
    private final Vector v2;
    private double D; // maximum distance between ECDF1 and F, or ECDF1 and ECFD2
    private final double pValue;

    /**
     * One-sample K-S test.
     * <p>
     * D is the maximum distance between ECDF(v) and given cdf.
     * pValue is the computed p-value for the KS test against the given distribution
     * <p>
     * The null hypothesis of this test is that the given data set belongs to the given distribution.
     * The alternative hypothesis is that the data set does not belong to the given distribution.
     *
     * @param cdf the distribution to compare against
     */
    public KSTest(Vector sample, Distribution cdf) {
        this.v1 = BaseFilters.sort(sample);
        this.cdf = cdf;
        this.v2 = null;

        D = 0;
        double n = v1.rowCount();
        double fo = 0.0;

        for (int i = 0; i < v1.rowCount(); i++) {
            //ECDF(x) - F(x)
            double ff = cdf.cdf(v1.getValue(i));
            double fn = (i + 1) / n;
            D = Math.max(D, Math.abs(fo - ff));
            D = Math.max(D, Math.abs(fn - ff));
            fo = fn;
        }
        n = Math.sqrt(n);
        pValue = probks((n + 0.12 + 0.11 / n) * D);
    }

    /**
     * Two-samples K-S test
     * <p>
     * D is the maximum distance between ECDF(v1) and ECDF(v2)
     * pValue is the p-value for the 2 sample KS test
     * The null hypothesis of this test is that both data sets comes from the same distribution,
     * The alternative hypothesis is that the two samples comes from different distributions.
     *
     * @param sample1 first sample
     * @param sample2 second sample
     */
    public KSTest(Vector sample1, Vector sample2) {
        this.v1 = BaseFilters.sort(sample1);
        this.v2 = BaseFilters.sort(sample2);
        this.cdf = null;

        D = 0;
        double fn1 = 0.0;
        double fn2 = 0.0;
        int i1 = 0;
        int i2 = 0;
        double n1 = v1.rowCount();
        double n2 = v2.rowCount();
        while (i1 < n1 && i2 < n2) {
            double d1 = v1.getValue(i1);
            double d2 = v2.getValue(i2);
            if (d1 <= d2) fn1 = i1++ / n1;
            if (d2 <= d1) fn2 = i2++ / n2;
            D = Math.max(D, Math.abs(fn1 - fn2));
        }
        double n = (n1 * n2) / (n1 + n2);
        n = Math.sqrt(n);
        pValue = probks((n + 0.12 + 0.11 / n) * D);
    }

//    private double pValue(double n) {
//        return 1 - cdf((Math.sqrt(n) + 0.12 + 0.11 / Math.sqrt(n)) * D);
//        return cdf((Math.sqrt(n) + 0.12 + 0.11 / Math.sqrt(n)) * D);
//    }

    public double probks(double x) {
//        if (x < 0)
//            throw new ArithmeticException("Invalid value of x, x must be > 0, not " + x);
//        else if (x == 0)
//            return 0;
//        else if (x >= 5)
//            return 1;
//
//        /*
//         * Uses 2 formulas, see http://en.wikipedia.org/wiki/Kolmogorov%E2%80%93Smirnov_test#Kolmogorov_distribution
//         * Each formula converges very rapidly, interface 3 terms to full
//         * IEEE precision for one or the other - crossover point is 1.18
//         * according to Numerical Recipes, 3rd Edition p(334-335)
//         */
//        double tmp = 0;
//        double x2 = x * x;
//        if (x < 1.18) {
//            for (int j = 1; j <= 3; j++)
//                tmp += Math.exp(-Math.pow(2 * j - 1, 2) * Math.PI * Math.PI / (8 * x2));
//
//            return Math.sqrt(2 * Math.PI) / x * tmp;
//        } else {
//            return 1 - 2 * (Math.exp(-2 * x2) + Math.exp(-18 * x2) - Math.exp(-8 * x2));
//        }
        final double EPS1 = 0.001;
        final double EPS2 = 1.0e-8;

        double a2 = -2.0 * x * x;
        double fac = 2.0;
        double sum = 0.0, term, bf = 0.0;
        for (int i = 1; i <= 100; i++) {
            term = fac * Math.exp(a2 * i * i);
            sum += term;
            if (Math.abs(term) <= EPS1 * bf || Math.abs(term) <= EPS2 * sum) return sum;
            fac = -fac;
            bf = Math.abs(term);
        }
        return 1.0;
    }

    public Distribution getCdf() {
        return cdf;
    }

    public Vector getSample1() {
        return v1;
    }

    public Vector getSample2() {
        return v2;
    }

    public double getD() {
        return D;
    }

    public double getPValue() {
        return pValue;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        if (cdf != null) oneSampleSummary(sb);
        else twoSamplesSummary(sb);
    }

    protected String getPValueStars() {
        if (pValue > 0.1) return "";
        if (pValue > 0.05) return ".";
        if (pValue > 0.01) return "*";
        if (pValue > 0.001) return "**";
        return "***";
    }

    private void oneSampleSummary(StringBuilder sb) {
        sb.append("> Kolmogorov-Smirnoff one-sample test\n");

        sb.append(String.format("distribution: %s\n", cdf.getName()));
        sb.append(String.format("D statistic: %.6f\n", D));
        sb.append(String.format("p-value: %.12f %s\n", pValue, getPValueStars()));
        sb.append("\n");
    }

    private void twoSamplesSummary(StringBuilder sb) {
        sb.append("> Kolmogorov-Smirnoff two-sample test\n");

        sb.append(String.format("D statistic: %.6f\n", D));
        sb.append(String.format("p-value: %.16f %s\n", pValue, getPValueStars()));
        sb.append("\n");
    }
}
