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

    public double getMu() {
        return mu;
    }

    public double getSl() {
        return sl;
    }

    public Alternative getAlt() {
        return alt;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public int getDegrees() {
        return sampleSize - 1;
    }

    public double getSampleMean() {
        return sampleMean;
    }

    public double getSampleSd() {
        return sampleSd;
    }

    public double getT() {
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
        sampleSize = clean.size();
        if (sampleSize < 1) {
            sampleMean = Double.NaN;
            sampleSd = Double.NaN;
            t = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;
            return;
        }
        sampleMean = Mean.of(clean).value();
        sampleSd = Variance.of(clean).sdValue();
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

        StudentT dist = StudentT.of(sampleSize - 1);

        var t = StudentT.of(sampleSize - 1, sampleMean, sampleSd / Math.sqrt(sampleSize));

        switch (alt) {
            case GREATER_THAN -> {
                pValue = 1 - dist.cdf(this.t);
                ciLow = t.quantile(sl);
                ciHigh = Double.POSITIVE_INFINITY;
            }
            case LESS_THAN -> {
                pValue = dist.cdf(this.t);
                ciLow = Double.NEGATIVE_INFINITY;
                ciHigh = t.quantile(1 - sl);
            }
            default -> {
                pValue = dist.cdf(-Math.abs(this.t)) * 2;
                ciLow = t.quantile(sl / 2);
                ciHigh = t.quantile(1 - sl / 2);
            }
        }
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> TTestOneSample\n");
        sb.append("\n");
        sb.append(" One Sample t-test\n");
        sb.append("\n");
        sb.append("mean: ").append(floatFlex(mu)).append("\n");
        sb.append("significance level: ").append(floatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ").append(alt == HTest.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample size: ").append(sampleSize).append("\n");
        sb.append("sample mean: ").append(floatFlex(sampleMean)).append("\n");
        sb.append("sample sd: ").append(floatFlex(sampleSd)).append("\n");
        sb.append("df: ").append(sampleSize - 1).append("\n");
        sb.append("t: ").append(floatFlex(t)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(floatFlex(ciLow)).append(",").append(floatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }
}
