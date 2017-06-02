/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.core.distributions.Distribution;
import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;

import static rapaio.sys.WS.formatFlex;

/**
 * Two-samples K-S test
 * <p>
 * D is the maximum distance between ECDF(v1) and ECDF(v2)
 * pValue is the p-value for the 2 sample KS test
 * The null hypothesis of this test is that both data sets comes from the same densities,
 * The altString hypothesis is that the two samples comes from different densities.
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/29/17.
 */
public class KSTestTwoSamples implements HTest {

    private final Var v1;
    private final Var v2;
    private double D; // maximum distance between ECDF1 and F, or ECDF1 and ECFD2
    private final double pValue;

    public static KSTestTwoSamples from(Var sample1, Var sample2) {
        return new KSTestTwoSamples(sample1, sample2);
    }

    private KSTestTwoSamples(Var sample1, Var sample2) {
        this.v1 = new VFSort().fitApply(sample1);
        this.v2 = new VFSort().fitApply(sample2);

        D = 0;
        double fn1 = 0.0;
        double fn2 = 0.0;
        int i1 = 0;
        int i2 = 0;
        double n1 = v1.getRowCount();
        double n2 = v2.getRowCount();
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

    private double probks(double x) {
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

    protected String getPValueStars() {
        if (pValue > 0.1) return "";
        if (pValue > 0.05) return ".";
        if (pValue > 0.01) return "*";
        if (pValue > 0.001) return "**";
        return "***";
    }

    @Override
    public double pValue() {
        return pValue;
    }

    @Override
    public double ciHigh() {
        return 0;
    }

    @Override
    public double ciLow() {
        return 0;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > Kolmogorov-Smirnoff 2-sample test\n");

        int ties1 = (int) (v1.getRowCount() - v1.stream().mapToDouble().distinct().count());
        int ties2 = (int) (v2.getRowCount() - v2.stream().mapToDouble().distinct().count());
        sb.append(String.format("first sample size: %d, ties: %d\n",
                v1.getRowCount(), ties1));
        sb.append(String.format("second sample size: %d, ties: %d\n",
                v2.getRowCount(), ties2));
        if (ties1 + ties2 > 0)
            sb.append(" (warning: p-values will not be exact because of ties)\n");

        sb.append(String.format("D statistic: %.6f\n", D));
        sb.append(String.format("p-value: %.16f %s\n", pValue, getPValueStars()));
        sb.append("\n");
        return sb.toString();
    }
}
