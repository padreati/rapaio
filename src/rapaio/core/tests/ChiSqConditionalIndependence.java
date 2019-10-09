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

import rapaio.core.distributions.ChiSquare;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.printer.format.Format;

import java.util.List;

/**
 * Chi-square test for conditional independence of categorical variables.
 * Scenario:
 * Let's suppose that we have three categorical variables: Z, X and X.
 * We noticed that there is a correlation between X and Y, but we want to
 * understand in X and Y are independent given Z. What we want to achieve
 * is to test if P(XY|Z) = P(X|Z)P(Y|Z).
 * <p/>
 * This test statistic build the partial table X x Y fore each level of Z.
 * Then we compute the test statistic for each partial level.
 * We use the fact that the sum of those partial tables' statistics is
 * distributed as a chi-square with n_Z(n_X-1)(n_Y-1) degrees
 * of freedom.
 * <p/>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/2/17.
 *
 * @see <a href="https://onlinecourses.science.psu.edu/stat504/node/112">https://onlinecourses.science.psu.edu/stat504/node/112</a>
 */
public class ChiSqConditionalIndependence implements HTest {

    public static ChiSqConditionalIndependence from(Var x, Var y, Var z) {
        return new ChiSqConditionalIndependence(x, y, z);
    }

    private final Var x;
    private final Var y;
    private final Var z;

    private final String[] zlevels;
    private final ChiSqIndependence[] ztests;

    private static int degrees;
    private static double statistic;
    private static double pValue;

    private ChiSqConditionalIndependence(Var x, Var y, Var z) {
        this.x = x;
        this.y = y;
        this.z = z;

        Frame df = BoundFrame.byVars(x, y, z);
        List<String> levels = z.levels();

        zlevels = new String[levels.size() - 1];
        ztests = new ChiSqIndependence[levels.size() - 1];

        for (int i = 1; i < levels.size(); i++) {
            String level = levels.get(i);
            zlevels[i - 1] = levels.get(i);
            Frame map = df.stream().filter(s -> s.getLabel(2).equals(level)).toMappedFrame();
            ztests[i - 1] = ChiSqIndependence.from(map.rvar(0), map.rvar(1), false);
        }

        degrees = (z.levels().size() - 1) * (x.levels().size() - 2) * (y.levels().size() - 2);
        for (ChiSqIndependence ztest : ztests) {
            statistic += ztest.getChiValue();
        }
        pValue = 1.0 - ChiSquare.of(degrees).cdf(statistic);
    }

    public double getDegrees() {
        return degrees;
    }

    public double getStatistic() {
        return statistic;
    }

    @Override
    public double pValue() {
        return pValue;
    }

    @Override
    public double ciHigh() {
        return Double.NaN;
    }

    @Override
    public double ciLow() {
        return Double.NaN;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("> ChiSqConditionalIndependence\n");
        sb.append("\n");

        sb.append("Chi-squared conditional independence test\n");
        sb.append("\n");

        sb.append("Null hypothesis:\n");
        sb.append("\n");
        sb.append("  P(").append(x.name()).append(",").append(y.name()).append("|");
        sb.append(z.name()).append(") = ").append("P(").append(x.name()).append("|").append(z.name()).append(")");
        sb.append(" + P(").append(y.name()).append("|").append(z.name()).append(")\n");
        sb.append("\n");

        sb.append("X-squared = ").append(Format.floatFlex(statistic))
                .append(", df = ").append(degrees)
                .append(", p-value = ").append(Format.pValue(pValue))
                .append("\n");
        sb.append("\n");

        sb.append("Observed conditional data:\n");
        sb.append("\n");

        for (int i = 0; i < zlevels.length; i++) {
            String zlevel = zlevels[i];

            sb.append("> ").append(z.name()).append(" = '").append(zlevel).append("'\n");
            sb.append(ztests[i].summary()).append("\n");
        }

        return sb.toString();
    }
}
