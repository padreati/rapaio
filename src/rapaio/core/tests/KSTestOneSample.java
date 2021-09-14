/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.tests;

import rapaio.core.distributions.Distribution;
import rapaio.data.Var;
import rapaio.data.filter.VSort;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import static rapaio.printer.Format.floatFlex;

/**
 * One-sample KS (Kolmogorov-Smirnoff) test.
 * <p>
 * The null hypothesis of this test is that the given data set belongs to the given densities.
 * The altString hypothesis is that the data set does not belong to the given densities.
 * <p>
 * This hypothesis test allows one to test if one have
 * strong evidence that a sample does not come from a given distribution.
 * <p>
 * We can't prove that a sample comes from a given distribution,
 * we can support a case if a sample does not come from a distribution.
 * <p>
 * D is the maximum distance between ECDF(v) and given cdf.
 * pValue is the computed p-value for the KS test against the given densities
 * <p>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/28/17.
 */
public class KSTestOneSample implements HTest {

    private final Distribution cdf;
    private final Var v;
    private double D; // maximum distance between ECDF1 and F
    private final double pValue;
    private final double ciHigh = Double.NaN;
    private final double ciLow = Double.NaN;

    public static KSTestOneSample from(Var sample, Distribution distribution) {
        return new KSTestOneSample(sample, distribution);
    }

    private KSTestOneSample(Var sample, Distribution cdf) {
        this.v = VSort.ascending().fapply(sample);
        this.cdf = cdf;

        D = 0;
        double n = v.size();
        double fo = 0.0;

        for (int i = 0; i < v.size(); i++) {
            //ECDF(x) - F(x)
            double ff = cdf.cdf(v.getDouble(i));
            double fn = (i + 1) / n;
            D = Math.max(D, Math.abs(fo - ff));
            D = Math.max(D, Math.abs(fn - ff));
            fo = fn;
        }
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

    @Override
    public double pValue() {
        return pValue;
    }

    @Override
    public double ciHigh() {
        return ciHigh;
    }

    @Override
    public double ciLow() {
        return ciLow;
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > Kolmogorov-Smirnoff 1-sample test\n");

        int ties = (int) (v.size() - v.stream().mapToDouble().distinct().count());
        sb.append(String.format("sample size: %d, ties: %d\n", v.size(), ties));
        if (ties > 0)
            sb.append(" (warning: p-values will not be exact because of ties)\n");

        sb.append(String.format("densities: %s\n", cdf.name()));
        sb.append("D statistic: ").append(floatFlex(D)).append("\n");
        sb.append("p-value: ").append(floatFlex(pValue)).append(" ").append(Format.pValueStars(pValue)).append("\n");
        sb.append("\n");
        return sb.toString();
    }
}
