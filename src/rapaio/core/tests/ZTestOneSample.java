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
import rapaio.data.Var;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import static rapaio.printer.format.Format.floatFlex;

/**
 * one sample z test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/24/16.
 */
public class ZTestOneSample implements HTest {

    /**
     * One sample z test with default significance level 0.05 and to tails alternative
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param mean       mean of X
     * @param sd         standard deviation of X
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample test(double sampleMean, int sampleSize, double mean, double sd) {
        return new ZTestOneSample(sampleMean, sampleSize, mean, sd, 0.05, HTest.Alternative.TWO_TAILS);
    }

    /**
     * One sample z test with default significance level of 0.05 and two tails alternative
     *
     * @param x    given sample
     * @param mean mean of X
     * @param sd   standard deviation of X
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample test(Var x, double mean, double sd) {
        return new ZTestOneSample(x, mean, sd, 0.05, HTest.Alternative.TWO_TAILS);
    }


    /**
     * One sample z test
     *
     * @param sampleMean given sample mean
     * @param sampleSize given sample size
     * @param mean       mean of X
     * @param sd         standard deviation of X
     * @param sl         significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt        alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample test(double sampleMean, int sampleSize, double mean, double sd, double sl, HTest.Alternative alt) {
        return new ZTestOneSample(sampleMean, sampleSize, mean, sd, sl, alt);
    }

    /**
     * One sample z test
     *
     * @param x    given sample
     * @param mean mean of X
     * @param sd   standard deviation of X
     * @param sl   significance level, usual values are 0.1, 0.05, 0.01, 0.001
     * @param alt  alternative hypothesis alternative
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestOneSample test(Var x, double mean, double sd, double sl, HTest.Alternative alt) {
        return new ZTestOneSample(x, mean, sd, sl, alt);
    }

    // INTERNALS


    private final double mu;
    private final double sd;
    private final double sl;
    private final HTest.Alternative alt;

    // computed
    private final int sampleSize;
    private final double sampleMean;
    private double zScore;
    private double pValue;
    private double ciLow;
    private double ciHigh;

    private ZTestOneSample(Var sample, double mu, double sd, double sl, HTest.Alternative alt) {
        this.mu = mu;
        this.sd = sd;
        this.sl = sl;
        this.alt = alt;

        Var clean = sample.stream().complete().toMappedVar();
        sampleSize = clean.rowCount();
        if (sampleSize < 1) {
            sampleMean = Double.NaN;
            zScore = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;
            return;
        }
        sampleMean = Mean.of(clean).value();
        compute();
    }

    private ZTestOneSample(double sampleMean, int sampleSize, double mu, double sd, double sl, HTest.Alternative alt) {
        this.mu = mu;
        this.sd = sd;
        this.sl = sl;
        this.alt = alt;
        this.sampleSize = sampleSize;
        this.sampleMean = sampleMean;
        compute();
    }

    public double getMu() {
        return mu;
    }

    public double getSd() {
        return sd;
    }

    public double getSl() {
        return sl;
    }

    public HTest.Alternative getAlt() {
        return alt;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public double getSampleMean() {
        return sampleMean;
    }

    public double getZScore() {
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
        zScore = (sampleMean - mu) * Math.sqrt(sampleSize) / sd;

        Normal normal = Normal.std();

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

        ciLow = Normal.of(sampleMean, sd / Math.sqrt(sampleSize)).quantile(sl / 2);
        ciHigh = Normal.of(sampleMean, sd / Math.sqrt(sampleSize)).quantile(1 - sl / 2);
    }

    @Override
    public String toSummary(Printer printer, POption... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> HTTools.zTestOneSample\n");
        sb.append("\n");
        sb.append(" One Sample z-test\n");
        sb.append("\n");
        sb.append("mean: ").append(floatFlex(mu)).append("\n");
        sb.append("sd: ").append(floatFlex(sd)).append("\n");
        sb.append("significance level: ").append(floatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ").append(alt == HTest.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample size: ").append(sampleSize).append("\n");
        sb.append("sample mean: ").append(floatFlex(sampleMean)).append("\n");
        sb.append("z score: ").append(floatFlex(zScore)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(floatFlex(ciLow)).append(",").append(floatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }
}
