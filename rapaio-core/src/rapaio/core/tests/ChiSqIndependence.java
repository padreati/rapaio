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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rapaio.core.distributions.ChiSquare;
import rapaio.core.tools.DensityTable;
import rapaio.data.Var;
import rapaio.math.linear.DMatrix;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Pearson Chi Square independence test.
 * <p>
 * Allows one to test whether unpaired observations on two variables,
 * expressed in a contingency table, are independent of each other.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/31/17.
 */
public final class ChiSqIndependence implements HTest {

    /**
     * Tests the independence of unpaired observations from two variables.
     * Variables should be both nominal, a contingency table is created from them.
     *
     * @param x first random variable
     * @param y second random variable
     * @return result object
     */
    public static ChiSqIndependence from(Var x, Var y, boolean yates) {
        return new ChiSqIndependence(DensityTable.fromLabels(false, x, y, null), yates);
    }

    public static ChiSqIndependence from(DMatrix m, boolean yates) {
        List<String> rowLevels = new ArrayList<>();
        List<String> colLevels = new ArrayList<>();
        for (int i = 0; i < m.rows(); i++) {
            rowLevels.add("R" + (i + 1));
        }
        for (int i = 0; i < m.cols(); i++) {
            colLevels.add("C" + (i + 1));
        }
        return from(m, rowLevels, colLevels, yates);
    }

    public static ChiSqIndependence from(DMatrix m, List<String> rowLevels, List<String> colLevels, boolean yates) {
        if (m.rows() != rowLevels.size()) {
            throw new IllegalArgumentException("Row levels length is different than matrix rows.");
        }
        if (m.cols() != colLevels.size()) {
            throw new IllegalArgumentException("Col levels length is different than matrix cols.");
        }
        var dt = DensityTable.empty(true, rowLevels, colLevels);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                dt.inc(i, j, m.get(i, j));
            }
        }
        return from(dt, yates);
    }

    public static ChiSqIndependence from(DensityTable<String, String> dt, boolean yates) {
        return new ChiSqIndependence(dt, yates);
    }

    private final DensityTable<String, String> observed;
    private final DensityTable<String, String> expected;
    private final boolean yates;
    private final int df; // degrees of freedom
    private final double chiValue; // chi-square statistic's value
    private final double pValue;

    private ChiSqIndependence(DensityTable<String, String> observed, boolean yates) {
        this.yates = yates;
        this.observed = observed;
        df = (observed.rows() - 1) * (observed.cols() - 1);

        expected = new DensityTable<>(observed.rowIndex(), observed.colIndex());

        double[] rowTotals = observed.rowTotals();
        double[] colTotals = observed.colTotals();
        double total = Arrays.stream(rowTotals).sum();

        double sum = 0.0;
        for (int i = 0; i < observed.rows(); i++) {
            for (int j = 0; j < observed.cols(); j++) {
                double exp = rowTotals[i] * colTotals[j] / total;
                expected.inc(i, j, exp);
                sum += Math.pow(Math.abs(observed.get(i, j) - exp) - (yates ? 0.5 : 0.0), 2) / exp;
            }
        }
        chiValue = sum;
        pValue = 1.0 - ChiSquare.of(df).cdf(sum);
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
    public String toSummary(Printer printer, POpt<?>... options) {

        return "> ChiSqIndependence\n"
                + "\n"
                + "Pearson's Chi-squared test" + (yates ? " with Yates' continuity correction" : "") + "\n"
                + "\n"
                + "X-squared = " + Format.floatFlex(chiValue)
                + ", df = " + df
                + ", p-value = " + Format.pValue(pValue)
                + "\n"
                + "\n"
                + "Observed data:\n"
                + observed.toSummary(printer, options) + "\n"
                + "Expected data:\n"
                + expected.toSummary(printer, options) + "\n";
    }
}
