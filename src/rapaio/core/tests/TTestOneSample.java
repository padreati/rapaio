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

import rapaio.core.CoreTools;
import rapaio.core.distributions.StudentT;
import rapaio.data.Var;

import static rapaio.sys.WS.formatFlex;

/**
 * one sample z test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/24/16.
 */
public class TTestOneSample implements HTest {

    /**
     * One sample t test
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param sampleSd   sample standard deviation
     * @param mean       mean of X
     * @param sl         significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt        alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample test(double sampleMean, int sampleSize, double sampleSd, double mean, double sl, HTest.Alternative alt) {
        return new TTestOneSample(sampleMean, sampleSize, sampleSd, mean, sl, alt);
    }

    /**
     * One sample t test
     *
     * @param x    given sample
     * @param mean mean of X
     * @param sl   significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt  alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample test(Var x, double mean, double sl, HTest.Alternative alt) {
        return new TTestOneSample(x, mean, sl, alt);
    }


    /**
     * One sample t test with default significance level 0.05 and two tails alternative
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param sampleSd   sample standard deviation of X
     * @param mean       mean of X
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample test(double sampleMean, int sampleSize, double sampleSd, double mean) {
        return new TTestOneSample(sampleMean, sampleSize, sampleSd, mean, 0.05, HTest.Alternative.TWO_TAILS);
    }

    /**
     * One sample t test with default significance level of 0.05 and two tails alternative
     *
     * @param x    given sample
     * @param mean mean of X
     * @return an object containing hypothesis testing analysis
     */
    public static TTestOneSample test(Var x, double mean) {
        return new TTestOneSample(x, mean, 0.05, HTest.Alternative.TWO_TAILS);
    }

    private final double mu;
    private final double sl;
    private final HTest.Alternative alt;

    // computed
    private final int sampleSize;
    private final double sampleMean;
    private final double sampleSd;
    private double t;
    private double pValue;
    private double ciLow;
    private double ciHigh;

    public double mu() {
        return mu;
    }

    public double sl() {
        return sl;
    }

    public Alternative alt() {
        return alt;
    }

    public int sampleSize() {
        return sampleSize;
    }

    public int df() {
        return sampleSize-1;
    }

    public double sampleMean() {
        return sampleMean;
    }

    public double sampleSd() {
        return sampleSd;
    }

    public double t() {
        return t;
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

    private TTestOneSample(Var sample, double mu, double sl, HTest.Alternative alt) {
        this.mu = mu;
        this.sl = sl;
        this.alt = alt;

        Var clean = sample.stream().complete().toMappedVar();
        sampleSize = clean.getRowCount();
        if (sampleSize < 1) {
            sampleMean = Double.NaN;
            sampleSd = Double.NaN;
            t = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;
            return;
        }
        sampleMean = CoreTools.mean(clean).getValue();
        sampleSd = CoreTools.variance(clean).sdValue();
        compute();
    }

    private TTestOneSample(double sampleMean, int sampleSize, double sampleSd, double mu, double sl, HTest.Alternative alt) {
        this.mu = mu;
        this.sl = sl;
        this.alt = alt;
        this.sampleSize = sampleSize;
        this.sampleMean = sampleMean;
        this.sampleSd = sampleSd;
        compute();
    }

    private void compute() {
        t = (sampleMean - mu) * Math.sqrt(sampleSize) / sampleSd;

        StudentT dist = new StudentT(sampleSize - 1);

        switch (alt) {
            case GREATER_THAN:
                pValue = 1 - dist.cdf(this.t);
                break;
            case LESS_THAN:
                pValue = dist.cdf(this.t);
                break;
            default:
                pValue = dist.cdf(-Math.abs(this.t)) * 2;
        }

        ciLow = new StudentT(sampleSize - 1, sampleMean, sampleSd / Math.sqrt(sampleSize)).quantile(sl / 2);
        ciHigh = new StudentT(sampleSize - 1, sampleMean, sampleSd / Math.sqrt(sampleSize)).quantile(1 - sl / 2);
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> TTestOneSample\n");
        sb.append("\n");
        sb.append(" One Sample t-test\n");
        sb.append("\n");
        sb.append("mean: ").append(formatFlex(mu)).append("\n");
        sb.append("significance level: ").append(formatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ").append(alt == HTest.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample size: ").append(sampleSize).append("\n");
        sb.append("sample mean: ").append(formatFlex(sampleMean)).append("\n");
        sb.append("sample sd: ").append(formatFlex(sampleSd)).append("\n");
        sb.append("df: ").append(sampleSize - 1).append("\n");
        sb.append("t: ").append(formatFlex(t)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(formatFlex(ciLow)).append(",").append(formatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }
}
