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

import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.printer.Printable;

import static rapaio.core.CoreTools.mean;
import static rapaio.sys.WS.formatFlex;

/**
 * Two sample z test: tests the difference between two sample means
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/27/16.
 */
public class ZTestTwoSamples implements HTest {

    /**
     * Two unpaired samples z test for difference of the means with default values
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
    public static ZTestTwoSamples test(double xSampleMean, int xSampleSize, double ySampleMean, int ySampleSize, double mean, double xSd, double ySd) {
        return new ZTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd, ySd, 0.05, HTest.Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples z test for difference of the means
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
    public static ZTestTwoSamples test(double xSampleMean, int xSampleSize, double ySampleMean, int ySampleSize, double mean, double xSd, double ySd, double sl, HTest.Alternative alt) {
        return new ZTestTwoSamples(xSampleMean, xSampleSize, ySampleMean, ySampleSize, mean, xSd, ySd, sl, alt);
    }

    /**
     * Two unpaired samples z test for difference of the means with default values
     * for significance level (0.05) and alternative (two tails)
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param xSd  standard deviation of the first sample
     * @param ySd  standard deviation of the second sample
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoSamples test(Var x, Var y, double mean, double xSd, double ySd) {
        return new ZTestTwoSamples(x, y, mean, xSd, ySd, 0.05, HTest.Alternative.TWO_TAILS);
    }

    /**
     * Two unpaired samples z test for difference of the means
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param xSd  standard deviation of the first sample
     * @param ySd  standard deviation of the second sample
     * @param sl   significance level (usual value 0.05)
     * @param alt  alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoSamples test(Var x, Var y, double mean, double xSd, double ySd, double sl, HTest.Alternative alt) {
        return new ZTestTwoSamples(x, y, mean, xSd, ySd, sl, alt);
    }

    // parameters

    private final double sampleMean;
    private final double xSampleMean;
    private final int xSampleSize;
    private final double ySampleMean;
    private final int ySampleSize;
    private final double mu;
    private final double xSd;
    private final double ySd;
    private final double sl;
    private final HTest.Alternative alt;


    // computed

    private double zScore;
    private double pValue;
    private double ciLow;
    private double ciHigh;

    private ZTestTwoSamples(Var x, Var y, double mu, double xSd, double ySd, double sl, HTest.Alternative alt) {
        this.mu = mu;
        this.xSd = xSd;
        this.ySd = ySd;
        this.sl = sl;
        this.alt = alt;

        Var xComplete = x.stream().complete().toMappedVar();
        Var yComplete = y.stream().complete().toMappedVar();

        if (xComplete.getRowCount() < 1 || yComplete.getRowCount() < 1) {
            // nothing to do
            sampleMean = Double.NaN;
            xSampleMean = Double.NaN;
            ySampleMean = Double.NaN;
            xSampleSize = -1;
            ySampleSize = -1;

            zScore = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;

            return;
        }

        xSampleMean = mean(xComplete).getValue();
        xSampleSize = xComplete.getRowCount();
        ySampleMean = mean(yComplete).getValue();
        ySampleSize = yComplete.getRowCount();
        sampleMean = xSampleMean - ySampleMean;

        compute();
    }

    private ZTestTwoSamples(double xSampleMean, int xSampleSize, double ySampleMean, int ySampleSize, double mu, double xSd, double ySd, double sl, HTest.Alternative alt) {
        this.xSampleMean = xSampleMean;
        this.xSampleSize = xSampleSize;
        this.ySampleMean = ySampleMean;
        this.ySampleSize = ySampleSize;
        this.sampleMean = xSampleMean - ySampleMean;

        this.mu = mu;
        this.xSd = xSd;
        this.ySd = ySd;
        this.sl = sl;
        this.alt = alt;

        if (xSampleSize < 1 || ySampleSize < 1) {
            // nothing to do
            zScore = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;
            return;
        }

        compute();
    }

    public double sampleMean() {
        return sampleMean;
    }

    public double xSampleMean() {
        return xSampleMean;
    }

    public int xSampleSize() {
        return xSampleSize;
    }

    public double ySampleMean() {
        return ySampleMean;
    }

    public int ySampleSize() {
        return ySampleSize;
    }

    public double mu() {
        return mu;
    }

    public double xSd() {
        return xSd;
    }

    public double ySd() {
        return ySd;
    }

    public double sl() {
        return sl;
    }

    public HTest.Alternative alt() {
        return alt;
    }

    public double zScore() {
        return zScore;
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
        double xv = xSd * xSd / xSampleSize;
        double yv = ySd * ySd / ySampleSize;

        double sv = Math.sqrt(xv + yv);

        zScore = (xSampleMean - ySampleMean - mu) / sv;

        Normal normal = new Normal(0, 1);
        switch (alt) {
            case GREATER_THAN:
                pValue = 1 - normal.cdf(zScore);
                break;
            case LESS_THAN:
                pValue = normal.cdf(zScore);
                break;
            default:
                pValue = normal.cdf(-Math.abs(zScore)) * 2;
        }

        ciLow = new Normal(xSampleMean - ySampleMean, sv).quantile(sl / 2);
        ciHigh = new Normal(xSampleMean - ySampleMean, sv).quantile(1 - sl / 2);
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> HTTools.zTestTwoSamples\n");
        sb.append("\n");
        sb.append(" Two Samples z-test\n");
        sb.append("\n");
        sb.append("x sample mean: ").append(formatFlex(xSampleMean)).append("\n");
        sb.append("x sample size: ").append(xSampleSize).append("\n");
        sb.append("y sample mean: ").append(formatFlex(ySampleMean)).append("\n");
        sb.append("y sample size: ").append(ySampleSize).append("\n");
        sb.append("mean: ").append(formatFlex(mu)).append("\n");
        sb.append("x sd: ").append(formatFlex(xSd)).append("\n");
        sb.append("y sd: ").append(formatFlex(ySd)).append("\n");
        sb.append("significance level: ").append(formatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ").append(alt == HTest.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample mean: ").append(formatFlex(sampleMean)).append("\n");
        sb.append("z score: ").append(formatFlex(zScore)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(formatFlex(ciLow)).append(",").append(formatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }
}
