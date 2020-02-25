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

package rapaio.core.tools;

import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Var;
import rapaio.data.index.IndexLabel;
import rapaio.experiment.core.tools.ConcreteRowAverageEntropy;
import rapaio.experiment.core.tools.ConcreteRowIntrinsicInfo;
import rapaio.experiment.core.tools.ConcreteTotalColEntropy;
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
public final class DensityTable<U, V> implements Printable, Serializable {

    /**
     * Builds a table with given test columns and target columns
     *
     * @param rowLevels labels for rows
     * @param colLevels labels for columns
     * @param useFirst  true if using the first row and col, false otherwise
     */
    public static DensityTable<String, String> emptyByLabel(boolean useFirst, List<String> rowLevels, List<String> colLevels) {
        var rowIndex = IndexLabel.fromLabelValues(useFirst ? rowLevels : rowLevels.subList(1, rowLevels.size()));
        var colIndex = IndexLabel.fromLabelValues(useFirst ? colLevels : colLevels.subList(1, colLevels.size()));
        return new DensityTable<>(rowIndex, colIndex);
    }


    // private constructors

    /**
     * Builds a density table from two nominal vectors built from counts
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param rowVar      var on vertical axis
     * @param colVar      var on horizontal axis
     */
    public static DensityTable<String, String> fromLevelCounts(boolean withMissing, Var rowVar, Var colVar) {
        var rowIndex = IndexLabel.fromVarLevels(withMissing, rowVar);
        var colIndex = IndexLabel.fromVarLevels(withMissing, colVar);
        var dt = new DensityTable<>(rowIndex, colIndex);
        for (int i = 0; i < Math.min(rowVar.rowCount(), colVar.rowCount()); i++) {
            if (rowIndex.containsValue(rowVar, i) && colIndex.containsValue(colVar, i)) {
                dt.increment(rowIndex.getIndex(rowVar, i), colIndex.getIndex(colVar, i), 1);
            }
        }
        return dt;
    }

    /**
     * Builds a density table from two nominal vectors.
     * If not null, weights are used instead of counts.
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param rowVar      row var
     * @param colVar      col var
     * @param weights     weights used instead of counts, if not null
     */
    public static DensityTable<String, String> fromLevelWeights(boolean withMissing, Var rowVar, Var colVar, Var weights) {
        var rowIndex = IndexLabel.fromVarLevels(withMissing, rowVar);
        var colIndex = IndexLabel.fromVarLevels(withMissing, colVar);
        var dt = new DensityTable<>(rowIndex, colIndex);
        for (int i = 0; i < Math.min(rowVar.rowCount(), colVar.rowCount()); i++) {
            if (rowIndex.containsValue(rowVar, i) && colIndex.containsValue(colVar, i)) {
                dt.increment(rowIndex.getIndex(rowVar, i), colIndex.getIndex(colVar, i), weights.getDouble(i));
            }
        }
        return dt;
    }

    /**
     * Builds a density table from two nominal vectors built from counts
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param df          source data frame
     * @param rowVarName  var on vertical axis
     * @param colVarName  var on horizontal axis
     */
    public static DensityTable<String, String> fromLevelCounts(boolean withMissing, Frame df, String rowVarName, String colVarName) {
        return fromLevelCounts(withMissing, df.rvar(rowVarName), df.rvar(colVarName));
    }

    /**
     * Builds a density table from two nominal vectors.
     * If not null, weights are used instead of counts.
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param df          source data frame
     * @param rowVarName  row var
     * @param colVarName  col var
     * @param weights     weights used instead of counts, if not null
     */
    public static DensityTable<String, String> fromLevelWeights(boolean withMissing, Frame df, String rowVarName, String colVarName, Var weights) {
        return fromLevelWeights(withMissing, df.rvar(rowVarName), df.rvar(colVarName), weights);
    }

    /**
     * Builds a density table with a binary split, from two nominal vectors.
     * The first row contains instances which have test label equal with given testLabel,
     * second row contains frequencies for the rest of the instances.
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param rowVar      row var
     * @param colVar      col var
     * @param weights     if not null, weights used instead of counts
     * @param rowLevel    row label used for binary split
     */
    public static DensityTable<String, String> fromBinaryLevelWeights(boolean withMissing, Var rowVar, Var colVar, Var weights, String rowLevel) {
        var rowIndex = withMissing ? IndexLabel.fromLabelValues("?", rowLevel, "other") : IndexLabel.fromLabelValues(rowLevel, "other");
        var colIndex = IndexLabel.fromVarLevels(withMissing, colVar);
        var dt = new DensityTable<>(rowIndex, colIndex);

        for (int i = 0; i < Math.min(rowVar.rowCount(), colVar.rowCount()); i++) {
            int rowId = rowVar.getLabel(i).equals(rowLevel) ? 0 : 1;
            if (withMissing) {
                if (rowVar.isMissing(i)) {
                    rowId = 0;
                } else {
                    rowId++;
                }
            } else {
                if (rowVar.isMissing(i)) {
                    continue;
                }
            }
            dt.increment(rowId, colIndex.getIndex(colVar, i), weights != null ? weights.getDouble(i) : 1);
        }
        return dt;
    }

    /**
     * Builds a density table with a binary split, from two nominal vectors.
     * The first row contains instances which have test label equal with given testLabel,
     * second row contains frequencies for the rest of the instances.
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param df          source data frame
     * @param rowVarName  row var
     * @param colVarName  col var
     * @param weights     if not null, weights used instead of counts
     * @param rowLevel    row label used for binary split
     */
    public static DensityTable<String, String> fromBinaryLevelWeights(boolean withMissing, Frame df, String rowVarName, String colVarName, Var weights, String rowLevel) {
        return fromBinaryLevelWeights(withMissing, df.rvar(rowVarName), df.rvar(colVarName), weights, rowLevel);
    }

    private static final long serialVersionUID = 4359080329548577980L;

    public static final List<String> NUMERIC_DEFAULT_LABELS = Arrays.asList("?", "less-equals", "greater");

    private final Index<U> rowIndex;
    private final Index<V> colIndex;

    // table with frequencies
    private final double[][] values;

    private DensityTable(Index<U> rowIndex, Index<V> colIndex) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.values = new double[rowIndex.size()][colIndex.size()];
    }

    public DensityTable<U, V> newInstance() {
        return new DensityTable<>(rowIndex, colIndex);
    }

    public int rowCount() {
        return rowIndex.size();
    }

    public int colCount() {
        return colIndex.size();
    }

    public Index<U> rowIndex() {
        return rowIndex;
    }

    public Index<V> colIndex() {
        return colIndex;
    }

    public double get(int row, int col) {
        return values[row][col];
    }

    public double get(U row, V col) {
        return values[rowIndex.getIndex(row)][colIndex.getIndex(col)];
    }

    public void increment(int row, int col, double weight) {
        values[row][col] += weight;
    }

    public void increment(U row, V col, double weight) {
        values[rowIndex.getIndex(row)][colIndex.getIndex(col)] += weight;
    }

    public double splitByRowAverageEntropy() {
        return new ConcreteRowAverageEntropy().getSplitInfo(0, rowIndex.size(), colIndex.size(), values);
    }

    public double splitByRowInfoGain() {
        double totalColEntropy = new ConcreteTotalColEntropy().getSplitInfo(0, rowIndex.size(), colIndex.size(), values);
        return totalColEntropy - splitByRowAverageEntropy();
    }

    public double splitByRowGainRatio() {
        double splitByRowIntrinsicInfo = new ConcreteRowIntrinsicInfo().getSplitInfo(0, rowIndex.size(), colIndex.size(), values);
        return splitByRowInfoGain() / splitByRowIntrinsicInfo;
    }

    /**
     * Computes the number of columns which have totals equal or greater than minWeight
     *
     * @return number of columns which meet criteria
     */
    public boolean hasColsWithMinimumCount(double minWeight, int minCounts) {
        int count = 0;
        for (int i = 0; i < rowIndex.size(); i++) {
            double total = 0;
            for (int j = 1; j < colIndex.size(); j++) {
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
        double[] rowTotals = new double[rowIndex.size()];
        double[] colTotals = new double[colIndex.size()];
        double total = 0.0;
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                rowTotals[i] += values[i][j];
                colTotals[j] += values[i][j];
                total += values[i][j];
            }
        }
        if (total <= 0) {
            return 1;
        }

        double gini = 1.0;
        for (int i = 0; i < colIndex.size(); i++) {
            gini -= Math.pow(colTotals[i] / total, 2);
        }

        for (int i = 0; i < rowIndex.size(); i++) {
            double gini_k = 1;
            for (int j = 0; j < colIndex.size(); j++) {
                if (rowTotals[i] > 0)
                    gini_k -= Math.pow(values[i][j] / rowTotals[i], 2);
            }
            gini -= gini_k * rowTotals[i] / total;
        }
        return gini;
    }

    public double splitByColGiniGain() {
        double[] rowTotals = new double[rowIndex.size()];
        double[] colTotals = new double[colIndex.size()];
        double total = 0.0;
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                rowTotals[i] += values[i][j];
                colTotals[j] += values[i][j];
                total += values[i][j];
            }
        }
        if (total <= 0) {
            return 1;
        }

        double gini = 1.0;
        for (int i = 0; i < rowIndex.size(); i++) {
            gini -= Math.pow(rowTotals[i] / total, 2);
        }

        for (int i = 0; i < colIndex.size(); i++) {
            double gini_k = 1;
            for (int j = 0; j < rowIndex.size(); j++) {
                if (colTotals[i] > 0)
                    gini_k -= Math.pow(values[j][i] / colTotals[i], 2);
            }
            gini -= gini_k * colTotals[i] / total;
        }
        return gini;
    }

    public double[] rowTotals() {
        double[] totals = new double[rowIndex.size()];
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                totals[i] += values[i][j];
            }
        }
        return totals;
    }

    public double[] colTotals() {
        double[] totals = new double[colIndex.size()];
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                totals[j] += values[i][j];
            }
        }
        return totals;
    }

    public DensityTable<U, V> normalizeOverall() {
        var norm = newInstance();
        double total = 0;
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                norm.values[i][j] = values[i][j];
                total += values[i][j];
            }
        }
        if (total > 0) {
            for (int i = 0; i < rowIndex.size(); i++) {
                for (int j = 0; j < colIndex.size(); j++) {
                    norm.values[i][j] /= total;
                }
            }
        }
        return norm;
    }

    public DensityTable<U, V> normalizeOnRows() {
        var norm = newInstance();
        double[] rowTotals = new double[rowIndex.size()];
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                norm.values[i][j] = values[i][j];
                rowTotals[i] += values[i][j];
            }
        }
        for (int i = 0; i < rowIndex.size(); i++) {
            if (rowTotals[i] > 0) {
                for (int j = 0; j < colIndex.size(); j++) {
                    norm.values[i][j] /= rowTotals[i];
                }
            }
        }
        return norm;
    }

    public DensityTable<U, V> normalizeOnCols() {
        var norm = newInstance();
        double[] colTotals = new double[colIndex.size()];
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                norm.values[i][j] = values[i][j];
                colTotals[j] += values[i][j];
            }
        }
        for (int i = 0; i < colIndex.size(); i++) {
            if (colTotals[i] > 0)
                for (int j = 0; j < rowIndex.size(); j++) {
                    norm.values[j][i] /= colTotals[i];
                }
        }
        return norm;
    }

    @Override
    public String toSummary() {
        TextTable tt = TextTable.empty(rowIndex.size() + 2, colIndex.size() + 2, 1, 0);
        putLevels(tt);
        tt.textRight(0, colIndex.size() + 1, "total");
        tt.textRight(rowIndex.size() + 1, 0, "total");
        putValues(tt);
        double[] rowTotals = rowTotals();
        for (int i = 0; i < rowIndex.size(); i++) {
            tt.floatFlex(i + 1, colIndex.size() + 1, rowTotals[i]);
        }
        double[] colTotals = colTotals();
        for (int i = 0; i < colIndex.size(); i++) {
            tt.floatFlex(rowIndex.size() + 1, i + 1, colTotals[i]);
        }
        double total = Arrays.stream(rowTotals).sum();
        tt.floatFlex(rowIndex.size() + 1, colIndex.size() + 1, total);
        return tt.getDynamicText();
    }

    private void putLevels(TextTable tt) {
        for (int i = 0; i < rowIndex.size(); i++) {
            tt.textRight(i + 1, 0, rowIndex.getValueString(i));
        }
        for (int i = 0; i < colIndex.size(); i++) {
            tt.textRight(0, i + 1, colIndex.getValueString(i));
        }
    }

    private void putValues(TextTable tt) {
        for (int i = 0; i < rowIndex.size(); i++) {
            for (int j = 0; j < colIndex.size(); j++) {
                tt.floatFlex(i + 1, j + 1, values[i][j]);
            }
        }
    }
}
