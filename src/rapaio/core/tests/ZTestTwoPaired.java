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

import static rapaio.printer.Format.floatFlex;

import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Two paired sample z test for testing mean of differences
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/27/16.
 */
public class ZTestTwoPaired implements HTest {

    /**
     * Two paired samples z test for mean of the difference with default values
     * for significance level (0.05) and alternative (two tails)
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param sd   standard deviation of the first sample
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoPaired test(Var x, Var y, double mean, double sd) {
        return new ZTestTwoPaired(x, y, mean, sd, 0.05, HTest.Alternative.TWO_TAILS);
    }

    /**
     * Two paired samples z test for mean of the differences
     *
     * @param x    first given sample
     * @param y    second given sample
     * @param mean null hypothesis mean
     * @param sd   standard deviation of the first sample
     * @param sl   significance level (usual value 0.05)
     * @param alt  alternative hypothesis (usual value two tails)
     * @return an object containing hypothesis testing analysis
     */
    public static ZTestTwoPaired test(Var x, Var y, double mean, double sd, double sl, HTest.Alternative alt) {
        return new ZTestTwoPaired(x, y, mean, sd, sl, alt);
    }


    // parameters

    private final Var x;
    private final Var y;
    private final double mu;
    private final double sd;
    private final double sl;
    private final HTest.Alternative alt;

    // computed

    private final Var xComplete;
    private final Var yComplete;

    private final double sampleMean;
    private final double zScore;
    private final double pValue;
    private final double ciLow;
    private final double ciHigh;

    private ZTestTwoPaired(Var x, Var y, double mu, double sd, double sl, HTest.Alternative alt) {
        this.x = x;
        this.y = y;
        this.mu = mu;
        this.sd = sd;
        this.sl = sl;
        this.alt = alt;

        xComplete = x.stream().complete().toMappedVar();
        yComplete = y.stream().complete().toMappedVar();

        if (xComplete.size() < 1 || yComplete.size() < 1) {
            // nothing to do
            sampleMean = Double.NaN;

            zScore = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;

            return;
        }

        Var complete = VarDouble.empty();
        for (int i = 0; i < Math.min(x.size(), y.size()); i++) {
            if (!(x.isMissing(i) || y.isMissing(i)))
                complete.addDouble(x.getDouble(i) - y.getDouble(i));
        }
        sampleMean = Mean.of(complete).value();

        double sv = sd / Math.sqrt(complete.size());

        zScore = (sampleMean - mu) / sv;

        Normal normal = Normal.std();
        switch (alt) {
            case GREATER_THAN -> pValue = 1 - normal.cdf(zScore);
            case LESS_THAN -> pValue = normal.cdf(zScore);
            default -> pValue = normal.cdf(-Math.abs(zScore)) * 2;
        }

        ciLow = Normal.of(sampleMean, sv).quantile(sl / 2);
        ciHigh = Normal.of(sampleMean, sv).quantile(1 - sl / 2);
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

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> ZTestTwoPaired\n");
        sb.append("\n");
        sb.append(" Two Paired z-test\n");
        sb.append("\n");
        sb.append("x complete rows: ").append(xComplete.size()).append("/").append(x.size()).append("\n");
        sb.append("y complete rows: ").append(yComplete.size()).append("/").append(y.size()).append("\n");
        sb.append("mean: ").append(floatFlex(mu)).append("\n");
        sb.append("x sd: ").append(floatFlex(sd)).append("\n");
        sb.append("significance level: ").append(floatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ").append(alt == HTest.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample mean: ").append(floatFlex(sampleMean)).append("\n");
        sb.append("z score: ").append(floatFlex(zScore)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(floatFlex(ciLow)).append(",").append(floatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }

}
