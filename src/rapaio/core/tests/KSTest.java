/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.printer.Printable;
import rapaio.core.distributions.Distribution;
import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;

/**
 * Creates a new statistical Kolmogorov-Smirnoff test. The 1 sample test, with <tt>v</tt>
 * being the 1 sample. The 1 sample test compare the data to a given densities,
 * and see if it does not belong to the given densities. The 2 sample test is
 * designed to tell if the data is not from the same population.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class KSTest implements Printable {

    private final Distribution cdf;
    private final Var v1;
    private final Var v2;
    private double D; // maximum distance between ECDF1 and F, or ECDF1 and ECFD2
    private final double pValue;
    private final String testName;

    /**
     * One-sample K-S test.
     * <p>
     * D is the maximum distance between ECDF(v) and given cdf.
     * pValue is the computed p-value for the KS test against the given densities
     * <p>
     * The null hypothesis of this test is that the given data set belongs to the given densities.
     * The alternative hypothesis is that the data set does not belong to the given densities.
     *
     * @param cdf the densities to compare against
     */
    public static KSTest newOneSampleTest(String testName, Var sample, Distribution cdf) {
        return new KSTest(testName, sample, cdf);
    }

    /**
     * Two-samples K-S test
     * <p>
     * D is the maximum distance between ECDF(v1) and ECDF(v2)
     * pValue is the p-value for the 2 sample KS test
     * The null hypothesis of this test is that both data sets comes from the same densities,
     * The alternative hypothesis is that the two samples comes from different densities.
     *
     * @param sample1 first sample
     * @param sample2 second sample
     */
    public static KSTest twoSamplesTest(String testName, Var sample1, Var sample2) {
        return new KSTest(testName, sample1, sample2);
    }

    private KSTest(String testName, Var sample, Distribution cdf) {
        this.testName = testName;
        this.v1 = new VFSort().fitApply(sample);
        this.cdf = cdf;
        this.v2 = null;

        D = 0;
        double n = v1.rowCount();
        double fo = 0.0;

        for (int i = 0; i < v1.rowCount(); i++) {
            //ECDF(x) - F(x)
            double ff = cdf.cdf(v1.value(i));
            double fn = (i + 1) / n;
            D = Math.max(D, Math.abs(fo - ff));
            D = Math.max(D, Math.abs(fn - ff));
            fo = fn;
        }
        n = Math.sqrt(n);
        pValue = probks((n + 0.12 + 0.11 / n) * D);
    }

    private KSTest(String testName, Var sample1, Var sample2) {
        this.testName = testName;
        this.v1 = new VFSort().fitApply(sample1);
        this.v2 = new VFSort().fitApply(sample2);
        this.cdf = null;

        D = 0;
        double fn1 = 0.0;
        double fn2 = 0.0;
        int i1 = 0;
        int i2 = 0;
        double n1 = v1.rowCount();
        double n2 = v2.rowCount();
        while (i1 < n1 && i2 < n2) {
            double d1 = v1.value(i1);
            double d2 = v2.value(i2);
            if (d1 <= d2) fn1 = i1++ / n1;
            if (d2 <= d1) fn2 = i2++ / n2;
            D = Math.max(D, Math.abs(fn1 - fn2));
        }
        double n = (n1 * n2) / (n1 + n2);
        n = Math.sqrt(n);
        pValue = probks((n + 0.12 + 0.11 / n) * D);
    }

    public double probks(double x) {
        final double EPS1 = 0.001;
        final double EPS2 = 1.0e-10;

        double a2 = -2.0 * x * x;
        double fac = 2.0;
        double sum = 0.0, term, bf = 0.0;
        for (int i = 1; i <= 100; i++) {
            term = fac * Math.exp(a2 * i * i);
            sum += term;
            if (Math.abs(term) <= EPS1 * bf || Math.abs(term) <= EPS2 * sum) return Math.min(1.0, sum);
            fac = -fac;
            bf = Math.abs(term);
        }
        return 1.0;
    }

    /**
     * Returns maximum distance between ECDF and given cdf, for 1-sample test,
     * and returns maximum distance between the two given ECDFs for 2-sample test
     *
     * @return maximum distance between densities
     */
    public double d() {
        return D;
    }

    /**
     * Gets pValue for the given test
     *
     * @return p-value
     */
    public double pValue() {
        return pValue;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
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
        sb.append("> Kolmogorov-Smirnoff 1-sample test: ").append(testName).append("\n");

        int ties = (int) (v1.rowCount() - v1.stream().mapToDouble().distinct().count());
        sb.append(String.format("sample size: %d, ties: %d\n",
                v1.rowCount(), ties));
        if (ties > 0)
            sb.append(" (warning: p-values will not be exact because of ties)\n");

        sb.append(String.format("densities: %s\n", cdf.name()));
        sb.append(String.format("D statistic: %.6f\n", D));
        sb.append(String.format("p-value: %.12f %s\n", pValue, getPValueStars()));
        sb.append("\n");
    }

    private void twoSamplesSummary(StringBuilder sb) {
        sb.append("> Kolmogorov-Smirnoff 2-sample test: ").append(testName).append("\n");

        int ties1 = (int) (v1.rowCount() - v1.stream().mapToDouble().distinct().count());
        int ties2 = (int) (v2.rowCount() - v2.stream().mapToDouble().distinct().count());
        sb.append(String.format("first sample size: %d, ties: %d\n",
                v1.rowCount(), ties1));
        sb.append(String.format("second sample size: %d, ties: %d\n",
                v2.rowCount(), ties2));
        if (ties1 + ties2 > 0)
            sb.append(" (warning: p-values will not be exact because of ties)\n");

        sb.append(String.format("D statistic: %.6f\n", D));
        sb.append(String.format("p-value: %.16f %s\n", pValue, getPValueStars()));
        sb.append("\n");
    }
}
