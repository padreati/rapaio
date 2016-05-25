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
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.printer.Printable;

import static rapaio.sys.WS.formatFlex;

/**
 * z test implementation in various scenarios.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/24/16.
 */
public abstract class ZTest implements Printable {

    public static ZTest oneSample(Var sample, double mean, double sd) {
        return new OneSample(sample, mean, sd, 0.05, Alternative.TWO_TAILS);
    }

    public static ZTest oneSample(Var sample, double mean, double sd, double sl, Alternative type) {
        return new OneSample(sample, mean, sd, sl, type);
    }

    private static final class OneSample extends ZTest {
        private final Var sample;
        private final double mu;
        private final double sd;
        private final double sl;
        private final Alternative alt;

        // computed
        Var clean;
        int totalRows;
        int completeRows;
        double computedMean;
        double zScore;
        double pValue;
        double ciLow;
        double ciHigh;
        String altString;

        public OneSample(Var sample, double mu, double sd, double sl, Alternative alt) {
            this.sample = sample;
            this.mu = mu;
            this.sd = sd;
            this.sl = sl;
            this.alt = alt;

            compute();
        }

        private void compute() {
            clean = sample.stream().complete().toMappedVar();
            totalRows = sample.rowCount();
            completeRows = clean.rowCount();
            if (completeRows < 1) {
                return;
            }

            computedMean = CoreTools.mean(clean).value();
            zScore = (computedMean - mu) * Math.sqrt(completeRows) / sd;

            Normal normal = new Normal(0, 1);

            switch (alt) {
                case GREATER_THAN:
                    pValue = 1 - normal.cdf(zScore);
                    altString = "P > z";
                    break;
                case LESS_THAN:
                    pValue = normal.cdf(zScore);
                    altString = "P < z";
                    break;
                default:
                    pValue = normal.cdf(-Math.abs(zScore)) * 2;
                    altString = "P > |z|";
            }

            ciLow = new Normal(computedMean, sd / Math.sqrt(completeRows)).quantile(sl / 2);
            ciHigh = new Normal(computedMean, sd / Math.sqrt(completeRows)).quantile(1 - sl / 2);
        }

        @Override
        public String summary() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n> ZTest.oneSample\n");
            sb.append("\n One Sample z-test\n");
            sb.append("\n");
            sb.append("complete rows: ").append(completeRows).append("/").append(totalRows).append("\n");
            sb.append("mean: ").append(formatFlex(mu)).append("\n");
            sb.append("sd: ").append(formatFlex(sd)).append("\n");
            sb.append("significance level: ").append(formatFlex(sl)).append("\n");
            sb.append("alternative hypothesis: ").append(alt == Alternative.TWO_TAILS ? "two tails " : "one tail ").append(altString).append("\n");
            sb.append("\n");
            sb.append("sample mean: ").append(formatFlex(computedMean)).append("\n");
            sb.append("z score: ").append(formatFlex(zScore)).append("\n");
            sb.append("p-value: ").append(pValue).append("\n");

            sb.append("conf int: [").append(formatFlex(ciLow)).append(",").append(formatFlex(ciHigh)).append("]\n");

            return sb.toString();
        }
    }

    public enum Alternative {
        TWO_TAILS,
        LESS_THAN,
        GREATER_THAN
    }
}
