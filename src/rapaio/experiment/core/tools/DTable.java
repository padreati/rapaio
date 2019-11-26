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

package rapaio.experiment.core.tools;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Distribution table.
 * <p>
 * Table tool class to facilitate various operations on two variables regarding frequencies.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public final class DTable implements Printable, Serializable {

    public static final List<String> NUMERIC_DEFAULT_LABELS = Arrays.asList("?", "less-equals", "greater");
    private static final long serialVersionUID = 4359080329548577980L;
    private final List<String> rowLevels;
    private final List<String> colLevels;

    // table with frequencies
    private final int start;
    private final double[][] values;

    // printing info
    private boolean totalSummary = true;

    /**
     * Builds a table with given test columns and target columns
     *
     * @param rowLevels labels for rows
     * @param colLevels labels for columns
     * @param useFirst  true if using the first row and col, false otherwise
     */
    public static DTable empty(List<String> rowLevels, List<String> colLevels, boolean useFirst) {
        return new DTable(rowLevels, colLevels, useFirst);
    }


    // private constructors

    /**
     * Builds a density table from two nominal vectors built from counts
     *
     * @param rowVar   var on vertical axis
     * @param colVar   var on horizontal axis
     * @param useFirst true if using the first row and col, false otherwise
     */
    public static DTable fromCounts(Var rowVar, Var colVar, boolean useFirst) {
        return new DTable(rowVar, colVar, VarDouble.fill(rowVar.rowCount(), 1), useFirst);
    }

    /**
     * Builds a density table from two nominal vectors.
     * If not null, weights are used instead of counts.
     *
     * @param rowVar   row var
     * @param colVar   col var
     * @param weights  weights used instead of counts, if not null
     * @param useFirst true if using the first row and col, false otherwise
     */
    public static DTable fromWeights(Var rowVar, Var colVar, Var weights, boolean useFirst) {
        return new DTable(rowVar, colVar, weights, useFirst);
    }

    /**
     * Builds a density table from two nominal vectors built from counts
     *
     * @param df         source data frame
     * @param rowVarName var on vertical axis
     * @param colVarName var on horizontal axis
     * @param useFirst   true if using the first row and col, false otherwise
     */
    public static DTable fromCounts(Frame df, String rowVarName, String colVarName, boolean useFirst) {
        return new DTable(df, rowVarName, colVarName, VarDouble.fill(df.rowCount(), 1), useFirst);
    }

    /**
     * Builds a density table from two nominal vectors.
     * If not null, weights are used instead of counts.
     *
     * @param df         source data frame
     * @param rowVarName row var
     * @param colVarName col var
     * @param weights    weights used instead of counts, if not null
     * @param useFirst   true if using the first row and col, false otherwise
     */
    public static DTable fromWeights(Frame df, String rowVarName, String colVarName, Var weights, boolean useFirst) {
        return new DTable(df, rowVarName, colVarName, weights, useFirst);
    }

    /**
     * Builds a density table with a binary split, from two nominal vectors.
     * The first row contains instances which have test label equal with given testLabel,
     * second row contains frequencies for the rest of the instances.
     *
     * @param rowVar   row var
     * @param colVar   col var
     * @param weights  if not null, weights used instead of counts
     * @param rowLevel row label used for binary split
     * @param useFirst true if using the first row and col, false otherwise
     */
    public static DTable binaryFromWeights(Var rowVar, Var colVar, Var weights, String rowLevel, boolean useFirst) {
        return new DTable(rowVar, colVar, weights, rowLevel, useFirst);
    }

    /**
     * Builds a density table with a binary split, from two nominal vectors.
     * The first row contains instances which have test label equal with given testLabel,
     * second row contains frequencies for the rest of the instances.
     *
     * @param df         source data frame
     * @param rowVarName row var
     * @param colVarName col var
     * @param weights    if not null, weights used instead of counts
     * @param rowLevel   row label used for binary split
     * @param useFirst   true if using the first row and col, false otherwise
     */
    public static DTable binaryFromWeights(Frame df, String rowVarName, String colVarName, Var weights, String rowLevel, boolean useFirst) {
        return new DTable(df, rowVarName, colVarName, weights, rowLevel, useFirst);
    }

    private DTable(List<String> rowLevels, List<String> colLevels, boolean useFirst) {
        this.rowLevels = rowLevels;
        this.colLevels = colLevels;
        this.start = useFirst ? 0 : 1;
        this.values = new double[rowLevels.size()][colLevels.size()];
    }

    private DTable(Var rowVar, Var colVar, Var weights, boolean useFirst) {
        this(rowVar.levels(), colVar.levels(), useFirst);

        if (!(rowVar.type().isNominal() || rowVar.type().equals(VType.BINARY) || rowVar.type().equals(VType.INT)))
            throw new IllegalArgumentException("row var must be nominal");
        if (!(colVar.type().isNominal() || colVar.type().equals(VType.BINARY) || rowVar.type().equals(VType.INT)))
            throw new IllegalArgumentException("col var is not nominal");
        if (rowVar.rowCount() != colVar.rowCount())
            throw new IllegalArgumentException("row and col vars must have same row count");

        int rowOffset = (rowVar.type().equals(VType.BINARY) || rowVar.type().equals(VType.INT)) ? 1 : 0;
        int colOffset = (colVar.type().equals(VType.BINARY) || rowVar.type().equals(VType.INT)) ? 1 : 0;
        for (int i = 0; i < rowVar.rowCount(); i++) {
            update(rowVar.getInt(i) + rowOffset, colVar.getInt(i) + colOffset, weights != null ? weights.getDouble(i) : 1);
        }
    }

    private DTable(Frame df, String rowVarName, String colVarName, Var weights, boolean useFirst) {
        this(df.levels(rowVarName), df.levels(colVarName), useFirst);

        if (!(df.type(rowVarName).isNominal() || df.type(rowVarName).equals(VType.BINARY) || df.type(rowVarName).equals(VType.INT)))
            throw new IllegalArgumentException("row var must be nominal");
        if (!(df.type(colVarName).isNominal() || df.type(colVarName).equals(VType.BINARY) || df.type(colVarName).equals(VType.INT)))
            throw new IllegalArgumentException("col var is not nominal");

        int rowOffset = (df.type(rowVarName).equals(VType.BINARY) || df.type(rowVarName).equals(VType.INT)) ? 1 : 0;
        int colOffset = (df.type(colVarName).equals(VType.BINARY) || df.type(colVarName).equals(VType.INT)) ? 1 : 0;
        int rowVarIndex = df.varIndex(rowVarName);
        int colVarIndex = df.varIndex(colVarName);
        for (int i = 0; i < df.rowCount(); i++) {
            update(df.getInt(i, rowVarIndex) + rowOffset, df.getInt(i, colVarIndex) + colOffset, weights != null ? weights.getDouble(i) : 1);
        }
    }

    private DTable(Var rowVar, Var colVar, Var weights, String rowLevel, boolean useFirst) {
        this(Arrays.asList("?", rowLevel, "other"), colVar.levels(), useFirst);

        if (!rowVar.type().isNominal()) throw new IllegalArgumentException("row var must be nominal");
        if (!colVar.type().isNominal()) throw new IllegalArgumentException("col var is not nominal");
        if (rowVar.rowCount() != colVar.rowCount())
            throw new IllegalArgumentException("row and col variables must have same size");

        for (int i = 0; i < rowVar.rowCount(); i++) {
            int index = 0;
            if (!rowVar.isMissing(i)) {
                index = (rowVar.getLabel(i).equals(rowLevel)) ? 1 : 2;
            }
            update(index, colVar.getInt(i), weights != null ? weights.getDouble(i) : 1);
        }
    }

    private DTable(Frame df, String rowVarName, String colVarName, Var weights, String rowLevel, boolean useFirst) {
        this(Arrays.asList("?", rowLevel, "other"), df.levels(colVarName), useFirst);

        if (!df.type(rowVarName).isNominal()) throw new IllegalArgumentException("row var must be nominal");
        if (!df.type(colVarName).isNominal()) throw new IllegalArgumentException("col var is not nominal");

        int rowVarIndex = df.varIndex(rowVarName);
        int colVarIndex = df.varIndex(colVarName);
        for (int i = 0; i < df.rowCount(); i++) {
            int index = 0;
            if (!df.isMissing(i, rowVarIndex)) {
                index = (df.getLabel(i, rowVarIndex).equals(rowLevel)) ? 1 : 2;
            }
            update(index, df.getInt(i, colVarIndex), weights != null ? weights.getDouble(i) : 1);
        }
    }

    public DTable withTotalSummary(boolean totalSummary) {
        this.totalSummary = totalSummary;
        return this;
    }

    public boolean useFirst() {
        return start == 0;
    }

    public int start() {
        return start;
    }

    public int rowCount() {
        return rowLevels.size();
    }

    public int colCount() {
        return colLevels.size();
    }

    public List<String> rowLevels() {
        return rowLevels;
    }

    public List<String> colLevels() {
        return colLevels;
    }

    public double get(int row, int col) {
        return values[row][col];
    }

    public void update(int row, int col, double weight) {
        values[row][col] += weight;
    }

    public double splitByRowAverageEntropy() {
        return new ConcreteRowAverageEntropy().getSplitInfo(start, rowLevels.size(), colLevels.size(), values);
    }

    public double splitByRowInfoGain() {
        double totalColEntropy = new ConcreteTotalColEntropy().getSplitInfo(start, rowLevels.size(), colLevels.size(), values);
        return totalColEntropy - splitByRowAverageEntropy();
    }

    public double splitByRowGainRatio() {
        double splitByRowIntrinsicInfo = new ConcreteRowIntrinsicInfo().getSplitInfo(start, rowLevels.size(), colLevels.size(), values);
        return splitByRowInfoGain() / splitByRowIntrinsicInfo;
    }

    /**
     * Computes the number of columns which have totals equal or greater than minWeight
     *
     * @return number of columns which meet criteria
     */
    public boolean hasColsWithMinimumCount(double minWeight, int minCounts) {
        int count = 0;
        for (int i = start; i < rowLevels.size(); i++) {
            double total = 0;
            for (int j = 1; j < colLevels.size(); j++) {
                total += values[i][j];
            }
            if (total >= minWeight) {
                count++;
                if (count >= minCounts) {
                    return true;
                }
            }
        }
        return false;
    }

    public double splitByRowGiniGain() {
        double[] rowTotals = new double[rowLevels.size()];
        double[] colTotals = new double[colLevels.size()];
        double total = 0.0;
        for (int i = start; i < rowLevels.size(); i++) {
            for (int j = start; j < colLevels.size(); j++) {
                rowTotals[i] += values[i][j];
                colTotals[j] += values[i][j];
                total += values[i][j];
            }
        }
        if (total <= 0) {
            return 1;
        }

        double gini = 1.0;
        for (int i = start; i < colLevels.size(); i++) {
            gini -= Math.pow(colTotals[i] / total, 2);
        }

        for (int i = start; i < rowLevels.size(); i++) {
            double gini_k = 1;
            for (int j = start; j < colLevels.size(); j++) {
                if (rowTotals[i] > 0)
                    gini_k -= Math.pow(values[i][j] / rowTotals[i], 2);
            }
            gini -= gini_k * rowTotals[i] / total;
        }
        return gini;
    }

    public double splitByColGiniGain() {
        double[] rowTotals = new double[rowLevels.size()];
        double[] colTotals = new double[colLevels.size()];
        double total = 0.0;
        for (int i = start; i < rowLevels.size(); i++) {
            for (int j = start; j < colLevels.size(); j++) {
                rowTotals[i] += values[i][j];
                colTotals[j] += values[i][j];
                total += values[i][j];
            }
        }
        if (total <= 0) {
            return 1;
        }

        double gini = 1.0;
        for (int i = start; i < rowLevels.size(); i++) {
            gini -= Math.pow(rowTotals[i] / total, 2);
        }

        for (int i = start; i < colLevels.size(); i++) {
            double gini_k = 1;
            for (int j = start; j < rowLevels.size(); j++) {
                if (colTotals[i] > 0)
                    gini_k -= Math.pow(values[j][i] / colTotals[i], 2);
            }
            gini -= gini_k * colTotals[i] / total;
        }
        return gini;
    }

    public double[] rowTotals() {
        double[] totals = new double[rowLevels.size()];
        for (int i = 0; i < rowLevels.size(); i++) {
            for (int j = 0; j < colLevels.size(); j++) {
                totals[i] += values[i][j];
            }
        }
        return totals;
    }

    public double[] colTotals() {
        double[] totals = new double[colLevels.size()];
        for (int i = 0; i < rowLevels.size(); i++) {
            for (int j = 0; j < colLevels.size(); j++) {
                totals[j] += values[i][j];
            }
        }
        return totals;
    }

    public DTable normalizeOverall() {
        DTable norm = DTable.empty(rowLevels, colLevels, start == 0).withTotalSummary(totalSummary);
        double total = 0;
        for (int i = start; i < rowLevels.size(); i++) {
            for (int j = start; j < colLevels.size(); j++) {
                norm.values[i][j] = values[i][j];
                total += values[i][j];
            }
        }
        if (total > 0) {
            for (int i = start; i < rowLevels.size(); i++) {
                for (int j = start; j < colLevels.size(); j++) {
                    norm.values[i][j] /= total;
                }
            }
        }
        return norm;
    }

    public DTable normalizeOnRows() {
        DTable norm = DTable.empty(rowLevels, colLevels, start == 0).withTotalSummary(totalSummary);
        double[] rowTotals = new double[rowLevels.size()];
        for (int i = start; i < rowLevels.size(); i++) {
            for (int j = start; j < colLevels.size(); j++) {
                norm.values[i][j] = values[i][j];
                rowTotals[i] += values[i][j];
            }
        }
        for (int i = start; i < rowLevels.size(); i++) {
            if (rowTotals[i] > 0)
                for (int j = start; j < colLevels.size(); j++) {
                    norm.values[i][j] /= rowTotals[i];
                }
        }
        return norm;
    }

    public DTable normalizeOnCols() {
        DTable norm = DTable.empty(rowLevels, colLevels, start == 0).withTotalSummary(totalSummary);
        double[] colTotals = new double[colLevels.size()];
        for (int i = start; i < rowLevels.size(); i++) {
            for (int j = start; j < colLevels.size(); j++) {
                norm.values[i][j] = values[i][j];
                colTotals[j] += values[i][j];
            }
        }
        for (int i = start; i < colLevels.size(); i++) {
            if (colTotals[i] > 0)
                for (int j = start; j < rowLevels.size(); j++) {
                    norm.values[j][i] /= colTotals[i];
                }
        }
        return norm;
    }

    @Override
    public String toSummary() {

        if (totalSummary) {
            TextTable tt = TextTable.empty(rowLevels.size() - start + 2, colLevels.size() - start + 2, 1, 0);

            putLevels(tt);
            tt.textRight(0, colLevels.size() - start + 1, "total");
            tt.textRight(rowLevels.size() - start + 1, 0, "total");
            putValues(tt);
            double[] rowTotals = rowTotals();
            for (int i = start; i < rowLevels.size(); i++) {
                tt.floatFlex(i - start + 1, colLevels.size() - start + 1, rowTotals[i]);
            }
            double[] colTotals = colTotals();
            for (int i = start; i < colLevels.size(); i++) {
                tt.floatFlex(rowLevels.size() - start + 1, i - start + 1, colTotals[i]);
            }
            double total = Arrays.stream(rowTotals).skip(start).sum();
            tt.floatFlex(rowLevels.size() - start + 1, colLevels.size() - start + 1, total);
            return tt.getDynamicText();
        } else {
            TextTable tt = TextTable.empty(rowLevels.size() - start + 1, colLevels.size() - start + 1, 1, 0);

            putLevels(tt);
            putValues(tt);
            return tt.getDynamicText();
        }
    }

    private void putLevels(TextTable tt) {
        for (int i = start; i < rowLevels.size(); i++) {
            tt.textRight(i - start + 1, 0, rowLevels.get(i));
        }
        for (int i = start; i < colLevels.size(); i++) {
            tt.textRight(0, i - start + 1, colLevels.get(i));
        }
    }

    private void putValues(TextTable tt) {
        for (int i = start; i < rowLevels.size(); i++) {
            for (int j = start; j < colLevels.size(); j++) {
                tt.floatFlex(i - start + 1, j - start + 1, values[i][j]);
            }
        }
    }
}
