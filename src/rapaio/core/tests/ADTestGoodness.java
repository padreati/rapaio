/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.var.VRefSort;

import static java.lang.Math.*;
import static rapaio.printer.format.Format.*;

/**
 * Hypothesis test which assess if a given samples belongs to a normal distribution.
 * Andreson-Darling test is based on A^2 distance statistic.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/17.
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
        this.x = xx.fapply(new VRefSort(xx.refComparator())).copy();

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
                // variance unknown, mean is known
                sigmaHat = 0.0;
                for (int i = 0; i < x.rowCount(); i++) {
                    sigmaHat += Math.pow(x.getDouble(i) - mu, 2);
                }
                sigmaHat = Math.sqrt(sigmaHat);
            } else {
                // both variance and mean are unknown
                sigmaHat = Variance.of(x).sdValue();
            }
        }

        Var y = VarDouble.from(x, value -> (value - muHat) / sigmaHat);
        Normal normal = Normal.std();

        a2 = 0.0;
        int n = y.rowCount();
        for (int i = 1; i <= n; i++) {
            double phi = normal.cdf(y.getDouble(i - 1));
            a2 += (2 * i - 1) * Math.log(phi) + (2 * (n - i) + 1) * Math.log(1 - phi);
        }
        a2 = -n - a2 / n;
        a2star = (Double.isNaN(mu) && Double.isNaN(sigma)) ? a2 * (1.0 + 4.0 / n - 25.0 / (n * n)) : a2;

        pValue = 1 - pvalue(a2, n);
        pValueStar = 1 - pvalue(a2star, n);
    }

    /**
     * Computes p-value for an AD GoF test
     * From: ad.test.pvalue.r of ADGofTest
     */
    private double pvalue(double x, int n) {
        if (x < 2) {
            x = exp(-1.2337141 / x) / sqrt(x) * (2.00012 + (.247105 - (.0649821 - (.0347962 - (.011672 - .00168691 * x) * x) * x) * x) * x);
        }
        else {
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
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("> ADTestGoodness\n");
        sb.append("\n");
        sb.append("Anderson-Darling GoF Test\n");
        sb.append("\n");

        sb.append("Null hypothesis:\n");
        sb.append("  sample is normally distributed\n");
        sb.append("\n");

        sb.append("sample size: ").append(x.rowCount()).append("\n");
        sb.append("given mean: ").append(floatFlex(mu)).append(", used mean : ").append(floatFlex(muHat)).append("\n");
        sb.append("given sd  : ").append(floatFlex(sigma)).append(", used sd   : ").append(floatFlex(sigmaHat)).append("\n");
        sb.append("\n");

        sb.append("A^2  statistic: ").append(floatFlex(a2)).append(", p-value: ").append(floatFlex(pValue)).append(" ").append(pValueStars(pValue)).append("\n");
        sb.append("A*^2 statistic: ").append(floatFlex(a2star)).append(", p-value: ").append(floatFlex(pValueStar)).append(" ").append(pValueStars(pValueStar)).append("\n");
        sb.append("\n");
        return sb.toString();
    }
}
