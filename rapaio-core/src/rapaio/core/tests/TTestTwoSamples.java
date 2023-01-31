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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import static rapaio.printer.Format.floatFlex;

import rapaio.core.distributions.StudentT;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * t test for checking if two samples have the same mean
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/14/16.
 */
public class TTestTwoSamples implements HTest {

    /**
     * Two unpaired samples t test for difference of the means with default values
     * for significance level (0.05) and alternative (two tails)
     *
     * @param xSampleMean sample mean for the first sample
     * @param xSampleSize sample size for the first sample
     * @param ySampleMean sample mean for the second sample
     * @param ySampleSize sample size for the second sample
     * @param mean        null hypothesis mean
     * @param xSd         standard deviation of the first sample
     * @param ySd         standard deviation of the second sample
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples test(double xSampleMean, int xSampleSize, double ySampleMean,
                                       int ySampleSize, double mean, double xSd, double ySd) {
        return new TTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd,
                ySd, 0.05, true, HTest.Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples t test for difference of the means
     *
     * @param xSampleMean sample mean for the first sample
     * @param xSampleSize sample size for the first sample
     * @param ySampleMean sample mean for the second sample
     * @param ySampleSize sample size for the second sample
     * @param mean        null hypothesis mean
     * @param xSd         standard deviation of the first sample
     * @param ySd         standard deviation of the second sample
     * @param sl          significance level (usual value 0.05)
     * @param alt         alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples test(double xSampleMean, int xSampleSize, double ySampleMean,
                                       int ySampleSize, double mean, double xSd, double ySd,
                                       double sl, HTest.Alternative alt) {
        return new TTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd,
                ySd, sl, true, alt);
    }

    /**
     * Two unpaired samples with equal variances t test for difference of the means with default
     * values for significance level (0.05) and alternative (two tails)
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples test(Var x, Var y, double mean) {
        return new TTestTwoSamples(x, y, mean, true, 0.05, HTest.Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples with equal variances t test for difference of the means
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param sl   significance level (usual value 0.05)
     * @param alt  alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples test(Var x, Var y, double mean, double sl,
                                       HTest.Alternative alt) {
        return new TTestTwoSamples(x, y, mean, true, sl, alt);
    }

    /**
     * Two unpaired samples with unequal variances Welch t test for difference of the means
     * default values for significance level (0.05) and alternative (two tails)
     *
     * @param xSampleMean sample mean for the first sample
     * @param xSampleSize sample size for the first sample
     * @param ySampleMean sample mean for the second sample
     * @param ySampleSize sample size for the second sample
     * @param mean        null hypothesis mean
     * @param xSd         standard deviation of the first sample
     * @param ySd         standard deviation of the second sample
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples welchTest(double xSampleMean, int xSampleSize, double ySampleMean,
                                            int ySampleSize, double mean, double xSd, double ySd) {
        return new TTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd,
                ySd, 0.05, false, HTest.Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples with unequal variances Welch t test for difference of the means
     *
     * @param xSampleMean sample mean for the first sample
     * @param xSampleSize sample size for the first sample
     * @param ySampleMean sample mean for the second sample
     * @param ySampleSize sample size for the second sample
     * @param mean        null hypothesis mean
     * @param xSd         standard deviation of the first sample
     * @param ySd         standard deviation of the second sample
     * @param sl          significance level (usual value 0.05)
     * @param alt         alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples welchTest(double xSampleMean, int xSampleSize, double ySampleMean,
                                            int ySampleSize, double mean, double xSd, double ySd,
                                            double sl, HTest.Alternative alt) {
        return new TTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd,
                ySd, sl, false, alt);
    }

    /**
     * Two unpaired samples with unequal variances Welch t test for difference of the means with
     * default values for significance level (0.05) and alternative (two tails)
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples welchTest(Var x, Var y, double mean) {
        return new TTestTwoSamples(x, y, mean, false, 0.05, HTest.Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples with unequal variances Welch t test for difference of the means
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param sl   significance level (usual value 0.05)
     * @param alt  alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static TTestTwoSamples welchTest(Var x, Var y, double mean, double sl,
                                            HTest.Alternative alt) {
        return new TTestTwoSamples(x, y, mean, false, sl, alt);
    }


    private final double sampleMean;
    private final double xSampleMean;
    private final int xSampleSize;
    private final double xSampleSd;

    // parameters
    private final double ySampleMean;
    private final int ySampleSize;
    private final double ySampleSd;
    private final boolean equalVars;
    private final double mu;
    private final double sl;
    private final HTest.Alternative alt;
    private double df;
    private double t;
    private double pValue;
    private double ciLow;
    private double ciHigh;

    private TTestTwoSamples(Var x, Var y, double mu, boolean equalVars, double sl,
                            HTest.Alternative alt) {
        this.mu = mu;
        this.sl = sl;
        this.alt = alt;
        this.equalVars = equalVars;

        Var xComplete = x.stream().complete().toMappedVar();
        Var yComplete = y.stream().complete().toMappedVar();

        if (xComplete.size() < 1 || yComplete.size() < 1) {
            // nothing to do
            sampleMean = Double.NaN;
            xSampleMean = Double.NaN;
            ySampleMean = Double.NaN;
            xSampleSize = -1;
            ySampleSize = -1;
            xSampleSd = Double.NaN;
            ySampleSd = Double.NaN;
            df = -1;

            t = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;

            return;
        }

        this.xSampleMean = Mean.of(xComplete).value();
        this.xSampleSize = xComplete.size();
        this.xSampleSd = Variance.of(xComplete).sdValue();
        this.ySampleMean = Mean.of(yComplete).value();
        this.ySampleSize = yComplete.size();
        this.ySampleSd = Variance.of(yComplete).sdValue();

        this.sampleMean = xSampleMean - ySampleMean;

        compute();
    }

    private TTestTwoSamples(double xSampleMean, int xSampleSize, double ySampleMean,
                            int ySampleSize, double mu, double xSd, double ySd, double sl,
                            boolean equalVars, HTest.Alternative alt) {
        this.mu = mu;
        this.sl = sl;
        this.alt = alt;
        this.equalVars = equalVars;

        this.xSampleMean = xSampleMean;
        this.xSampleSize = xSampleSize;
        this.xSampleSd = xSd;
        this.ySampleMean = ySampleMean;
        this.ySampleSize = ySampleSize;
        this.ySampleSd = ySd;

        this.sampleMean = xSampleMean - ySampleMean;

        compute();
    }

    public boolean hasEqualVars() {
        return equalVars;
    }

    public double getSampleMean() {
        return sampleMean;
    }

    public double getXSampleMean() {
        return xSampleMean;
    }

    public int getXSampleSize() {
        return xSampleSize;
    }

    public double getYSampleMean() {
        return ySampleMean;
    }

    public int getYSampleSize() {
        return ySampleSize;
    }

    public double getXSampleSd() {
        return xSampleSd;
    }

    public double getYSampleSd() {
        return ySampleSd;
    }

    public double getMu() {
        return mu;
    }

    public double getSl() {
        return sl;
    }

    public HTest.Alternative getAlt() {
        return alt;
    }

    public double getT() {
        return t;
    }

    public double getDf() {
        return df;
    }

    public double pValue() {
        return pValue;
    }

    public double ciLow() {
        return ciLow;
    }

    public double ciHigh() {
        return ciHigh;
    }

    private void compute() {

        double pv;

        if (equalVars) {
            df = xSampleSize + ySampleSize - 2;
            double xv = xSampleSd * xSampleSd * (xSampleSize - 1);
            double yv = ySampleSd * ySampleSd * (ySampleSize - 1);
            pv = Math.sqrt((xv + yv) / df) * Math.sqrt(1.0 / xSampleSize + 1.0 / ySampleSize);
            t = (xSampleMean - ySampleMean - mu) / pv;
        } else {
            double xv = xSampleSd * xSampleSd / xSampleSize;
            double yv = ySampleSd * ySampleSd / ySampleSize;
            t = (xSampleMean - ySampleMean - mu) / Math.sqrt(xv + yv);
            df = Math.pow(xv + yv, 2) / (xv * xv / (xSampleSize - 1) + yv * yv / (ySampleSize - 1));
            pv = Math.sqrt(xv + yv);
        }

        StudentT st = StudentT.of(df);
        switch (alt) {
            case GREATER_THAN -> pValue = 1 - st.cdf(t);
            case LESS_THAN -> pValue = st.cdf(t);
            default -> pValue = st.cdf(-Math.abs(t)) * 2;
        }

        ciLow = StudentT.of(df, xSampleMean - ySampleMean, pv).quantile(sl / 2);
        ciHigh = StudentT.of(df, xSampleMean - ySampleMean, pv).quantile(1 - sl / 2);
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> HTTools.tTestTwoSamples\n");
        sb.append("\n");
        if (equalVars) {
            sb.append(" Two Samples t-test\n");
            sb.append(" (equal variances)\n");
            sb.append("\n");
        } else {
            sb.append(" Welch's Two Samples t-test\n");
            sb.append(" (unequal variances)\n");
            sb.append("\n");
        }
        sb.append("mean: ").append(floatFlex(mu)).append("\n");

        sb.append("\nsample estimates:\n");
        sb.append("x mean: ").append(floatFlex(xSampleMean)).append("\n");
        sb.append("x size: ").append(xSampleSize).append("\n");
        sb.append("x sd: ").append(floatFlex(xSampleSd)).append("\n");
        sb.append("y mean: ").append(floatFlex(ySampleMean)).append("\n");
        sb.append("y size: ").append(ySampleSize).append("\n");
        sb.append("y sd: ").append(floatFlex(ySampleSd)).append("\n");

        sb.append("\ntest results:\n");
        sb.append("df: ").append(df).append("\n");
        sb.append("significance level: ").append(floatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ")
                .append(alt == HTest.Alternative.TWO_TAILS ? "two tails " : "one tail ")
                .append(alt.pCondition()).append("\n");
        sb.append("t: ").append(floatFlex(t)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(floatFlex(ciLow)).append(",").append(floatFlex(ciHigh))
                .append("]\n");

        return sb.toString();
    }


}