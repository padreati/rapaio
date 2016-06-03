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
import rapaio.core.HTTools;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.printer.Printable;

import static rapaio.sys.WS.formatFlex;

/**
 * one sample z test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/24/16.
 */
public class ZTestOneSample implements Printable {

    public final double mu;
    public final double sd;
    public final double sl;
    public final HTTools.Alternative alt;

    // computed
    public final int sampleSize;
    public final double sampleMean;
    public double zScore;
    public double pValue;
    public double ciLow;
    public double ciHigh;

    public ZTestOneSample(Var sample, double mu, double sd, double sl, HTTools.Alternative alt) {
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
        sampleMean = CoreTools.mean(clean).value();
        compute();
    }

    public ZTestOneSample(double sampleMean, int sampleSize, double mu, double sd, double sl, HTTools.Alternative alt) {
        this.mu = mu;
        this.sd = sd;
        this.sl = sl;
        this.alt = alt;
        this.sampleSize = sampleSize;
        this.sampleMean = sampleMean;
        compute();
    }

    private void compute() {
        zScore = (sampleMean - mu) * Math.sqrt(sampleSize) / sd;

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

        ciLow = new Normal(sampleMean, sd / Math.sqrt(sampleSize)).quantile(sl / 2);
        ciHigh = new Normal(sampleMean, sd / Math.sqrt(sampleSize)).quantile(1 - sl / 2);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> HTTools.zTestOneSample\n");
        sb.append("\n");
        sb.append(" One Sample z-test\n");
        sb.append("\n");
        sb.append("mean: ").append(formatFlex(mu)).append("\n");
        sb.append("sd: ").append(formatFlex(sd)).append("\n");
        sb.append("significance level: ").append(formatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ").append(alt == HTTools.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample size: ").append(sampleSize).append("\n");
        sb.append("sample mean: ").append(formatFlex(sampleMean)).append("\n");
        sb.append("z score: ").append(formatFlex(zScore)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(formatFlex(ciLow)).append(",").append(formatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }
}
