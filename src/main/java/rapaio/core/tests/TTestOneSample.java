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
import rapaio.core.distributions.StudentT;
import rapaio.data.Var;
import rapaio.printer.Printable;

import static rapaio.sys.WS.formatFlex;

/**
 * one sample z test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/24/16.
 */
public class TTestOneSample implements Printable {

    public final double mu;
    public final double sl;
    public final HTTools.Alternative alt;

    // computed
    public final int sampleSize;
    public final double sampleMean;
    public final double sampleSd;
    public double t;
    public double pValue;
    public double ciLow;
    public double ciHigh;

    public TTestOneSample(Var sample, double mu, double sl, HTTools.Alternative alt) {
        this.mu = mu;
        this.sl = sl;
        this.alt = alt;

        Var clean = sample.stream().complete().toMappedVar();
        sampleSize = clean.rowCount();
        if (sampleSize < 1) {
            sampleMean = Double.NaN;
            sampleSd = Double.NaN;
            t = Double.NaN;
            pValue = Double.NaN;
            ciLow = Double.NaN;
            ciHigh = Double.NaN;
            return;
        }
        sampleMean = CoreTools.mean(clean).value();
        sampleSd = CoreTools.var(clean).sdValue();
        compute();
    }

    public TTestOneSample(double sampleMean, int sampleSize, double sampleSd, double mu, double sl, HTTools.Alternative alt) {
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

        StudentT dist = new StudentT(sampleSize-1);

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

        ciLow = new StudentT(sampleSize -1, sampleMean, sampleSd / Math.sqrt(sampleSize)).quantile(sl / 2);
        ciHigh = new StudentT(sampleSize-1, sampleMean, sampleSd / Math.sqrt(sampleSize)).quantile(1 - sl / 2);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("> HTTools.tTestOneSample\n");
        sb.append("\n");
        sb.append(" One Sample t-test\n");
        sb.append("\n");
        sb.append("mean: ").append(formatFlex(mu)).append("\n");
        sb.append("significance level: ").append(formatFlex(sl)).append("\n");
        sb.append("alternative hypothesis: ").append(alt == HTTools.Alternative.TWO_TAILS ? "two tails " : "one tail ").append(alt.pCondition()).append("\n");
        sb.append("\n");
        sb.append("sample size: ").append(sampleSize).append("\n");
        sb.append("sample mean: ").append(formatFlex(sampleMean)).append("\n");
        sb.append("sample sd: ").append(formatFlex(sampleSd)).append("\n");
        sb.append("df: ").append(sampleSize-1).append("\n");
        sb.append("t: ").append(formatFlex(t)).append("\n");
        sb.append("p-value: ").append(pValue).append("\n");

        sb.append("conf int: [").append(formatFlex(ciLow)).append(",").append(formatFlex(ciHigh)).append("]\n");

        return sb.toString();
    }
}
