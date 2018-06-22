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
import rapaio.core.tools.DTable;
import rapaio.data.Var;
import rapaio.math.linear.RM;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pearson Chi Square independence test.
 * <p>
 * Allows one to test whether unpaired observations on two variables,
 * expressed in a contingency table, are independent of each other.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/31/17.
 */
public class ChiSqIndependence implements HTest {

    /**
     * Tests the independence of unpaired observations from two variables.
     * Variables should be both nominal, a contingency table is created from them.
     *
     * @param x first random variable
     * @param y second random variable
     * @return result object
     */
    public static ChiSqIndependence from(Var x, Var y, boolean yates) {
        return new ChiSqIndependence(DTable.fromCounts(x, y, false), yates);
    }

    public static ChiSqIndependence from(RM m, boolean yates) {
        List<String> rowLevels = new ArrayList<>();
        List<String> colLevels = new ArrayList<>();
        for (int i = 0; i < m.rowCount(); i++) {
            rowLevels.add("R" + (i + 1));
        }
        for (int i = 0; i < m.colCount(); i++) {
            colLevels.add("C" + (i + 1));
        }
        return from(m, rowLevels, colLevels, yates);
    }

    public static ChiSqIndependence from(RM m, List<String> rowLevels, List<String> colLevels, boolean yates) {
        if (m.rowCount() != rowLevels.size()) {
            throw new IllegalArgumentException("Row levels length is different than matrix rows.");
        }
        if (m.colCount() != colLevels.size()) {
            throw new IllegalArgumentException("Col levels length is different than matrix cols.");
        }
        DTable dt = DTable.empty(rowLevels, colLevels, true);
        for (int i = 0; i < m.rowCount(); i++) {
            for (int j = 0; j < m.colCount(); j++) {
                dt.update(i, j, m.get(i, j));
            }
        }
        return from(dt, yates);
    }

    public static ChiSqIndependence from(DTable dt, boolean yates) {
        return new ChiSqIndependence(dt, yates);
    }

    private final DTable dt;
    private final DTable expected;
    private final boolean yates;
    private final int df; // degrees of freedom
    private final double chiValue; // chi-square statistic's value
    private final double pValue;

    private ChiSqIndependence(DTable dt, boolean yates) {
        this.yates = yates;
        this.dt = dt;
        int off = dt.useFirst() ? 0 : 1;
        df = (dt.rowCount() - 1 - off) * (dt.colCount() - 1 - off);

        expected = DTable.empty(dt.rowLevels(), dt.colLevels(), dt.useFirst());

        double[] rowTotals = dt.rowTotals();
        double[] colTotals = dt.colTotals();
        double total = Arrays.stream(rowTotals).sum();

        double sum = 0.0;
        for (int i = dt.start(); i < dt.rowCount(); i++) {
            for (int j = dt.start(); j < dt.colCount(); j++) {
                double exp = rowTotals[i] * colTotals[j] / total;
                expected.update(i, j, exp);
                sum += Math.pow(Math.abs(dt.get(i, j) - exp) - (yates ? 0.5 : 0.0), 2) / exp;
            }
        }
        chiValue = sum;
        pValue = 1.0 - new ChiSquare(df).cdf(sum);
    }

    public int getDegrees() {
        return df;
    }

    public double getChiValue() {
        return chiValue;
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
        sb.append("> ChiSqIndependence\n");
        sb.append("\n");

        sb.append("Pearson's Chi-squared test").append(yates ? " with Yates' continuity correction" : "").append("\n");
        sb.append("\n");
        sb.append("X-squared = ").append(WS.formatFlex(chiValue))
                .append(", df = ").append(df)
                .append(", p-value = ").append(WS.formatPValue(pValue))
                .append("\n");
        sb.append("\n");

        sb.append("Observed data:\n");
        sb.append(dt.summary()).append("\n");

        sb.append("Expected data:\n");
        sb.append(expected.summary()).append("\n");
        return sb.toString();

    }
}
