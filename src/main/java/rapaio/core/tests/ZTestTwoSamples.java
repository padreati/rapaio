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

import rapaio.core.HTTools;
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
public class ZTestTwoSamples implements Printable {

    // parameters

    public final double sampleMean;
    public final double xSampleMean;
    public final int xSampleSize;
    public final double ySampleMean;
    public final int ySampleSize;
    public final double mu;
    public final double xSd;
    public final double ySd;
    public final double sl;
    public final HTTools.Alternative alt;


    // computed

    public double zScore;
    public double pValue;
    public double ciLow;
    public double ciHigh;

    public ZTestTwoSamples(Var x, Var y, double mu, double xSd, double ySd, double sl, HTTools.Alternative alt) {
        this.mu = mu;
        this.xSd = xSd;
        this.ySd = ySd;
        this.sl = sl;
        this.alt = alt;

        Var xComplete = x.stream().complete().toMappedVar();
        Var yComplete = y.stream().complete().toMappedVar();

        if (xComplete.rowCount() < 1 || yComplete.rowCount() < 1) {
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

        xSampleMean = mean(xComplete).value();
        xSampleSize = xComplete.rowCount();
        ySampleMean = mean(yComplete).value();
        ySampleSize = yComplete.rowCount();
        sampleMean = xSampleMean - ySampleMean;

        compute();
    }

    public ZTestTwoSamples(double xSampleMean, int xSampleSize, double ySampleMean, int ySampleSize, double mu, double xSd, double ySd, double sl, HTTools.Alternative alt) {
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
    public String summary() {
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
        sb.append("alternative hypothesis: ").append(alt == HTTools.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample mean: ").append(formatFlex(sampleMean)).append("\n");
        sb.append("z score: ").append(formatFlex(zScore)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(formatFlex(ciLow)).append(",").append(formatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }
}
