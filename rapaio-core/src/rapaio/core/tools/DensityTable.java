/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static java.lang.StrictMath.min;
import static java.lang.StrictMath.pow;

import static rapaio.math.MathTools.log2;
import static rapaio.util.collection.DoubleArrays.nanSum;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Var;
import rapaio.data.index.IndexLabel;
import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.Shape;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

/**
 * Two-way table which holds frequencies between two discrete variables.
 * <p>
 * For each variable we have a set of labels, one row oriented and the other column oriented.
 * Density table facilitates various computations on the two-way tables.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class DensityTable<U, V> implements Printable, Serializable {

    /**
     * Builds a table with given row labels and column labels.
     *
     * @param useFirst  true if using the first row and col, false otherwise
     * @param rowLabels labels for rows
     * @param colLabels labels for columns
     */
    public static DensityTable<String, String> empty(boolean useFirst, List<String> rowLabels, List<String> colLabels) {
        var rowIndex = IndexLabel.fromLabelValues(useFirst ? rowLabels : rowLabels.subList(1, rowLabels.size()));
        var colIndex = IndexLabel.fromLabelValues(useFirst ? colLabels : colLabels.subList(1, colLabels.size()));
        return new DensityTable<>(rowIndex, colIndex);
    }

    /**
     * Builds a count table from two variables using label representation.
     * <p>
     * The values from density table are number of instances if the weights are null. Otherwise,
     * the values are the corresponding weights.
     * <p>
     * The variable types must be {@link rapaio.data.VarType#NOMINAL} or {@link rapaio.data.VarType#BINARY}.
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param df          source data frame
     * @param rowName     row variable name
     * @param colName     col variable name
     * @param weights     weights used instead of counts, if not null
     */
    public static DensityTable<String, String> fromLabels(boolean withMissing, Frame df, String rowName, String colName, Var weights) {
        return fromLabels(withMissing, df.rvar(rowName), df.rvar(colName), weights);
    }

    /**
     * Builds a density table from two variables using label representation.
     * <p>
     * The values from density table are number of instances if the weights are null. Otherwise,
     * the values are the corresponding weights.
     * <p>
     * The variable types must be {@link rapaio.data.VarType#NOMINAL} or {@link rapaio.data.VarType#BINARY}.
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param rowVar      row var
     * @param colVar      col var
     * @param weights     weights used instead of counts, if not null
     */
    public static DensityTable<String, String> fromLabels(boolean withMissing, Var rowVar, Var colVar, Var weights) {
        var rowIndex = IndexLabel.fromVarLevels(withMissing, rowVar);
        var colIndex = IndexLabel.fromVarLevels(withMissing, colVar);
        var dt = new DensityTable<>(rowIndex, colIndex);
        for (int i = 0; i < min(rowVar.size(), colVar.size()); i++) {
            if (rowIndex.containsValue(rowVar, i) && colIndex.containsValue(colVar, i)) {
                dt.inc(rowIndex.getIndex(rowVar, i), colIndex.getIndex(colVar, i), weights != null ? weights.getDouble(i) : 1);
            }
        }
        return dt;
    }

    /**
     * Builds a density table with a binary split on rows from two variables.
     * The first row contains instances which have label equal with given row level.
     * The second row contains instances which have label not equal with the given row level.
     * <p>
     * If the weights parameter is null, the instance count is used instead, otherwise the
     * values are weights.
     *
     * @param withMissing true if using the first labels, false otherwise
     * @param df          source data frame
     * @param rowVarName  row variable name
     * @param colVarName  col variable name
     * @param weights     if not null, weights used instead of counts
     * @param rowLevel    row label used for binary split
     */
    public static DensityTable<String, String> fromBinaryLevelWeights(boolean withMissing, Frame df, String rowVarName, String colVarName,
            Var weights, String rowLevel) {
        return fromBinaryLevelWeights(withMissing, df.rvar(rowVarName), df.rvar(colVarName), weights, rowLevel);
    }

    /**
     * Builds a density table with a binary split on rows from two variables.
     * The first row contains instances which have label equal with given row level.
     * The second row contains instances which have label not equal with the given row level.
     * <p>
     * If the weights parameter is null, the instance count is used instead, otherwise the
     * values are weights.
     *
     * @param withMissing true if using the first row and col, false otherwise
     * @param rowVar      row variable
     * @param colVar      col variable
     * @param weights     if not null, weights used instead of counts
     * @param rowLevel    row label used for binary split
     */
    public static DensityTable<String, String> fromBinaryLevelWeights(boolean withMissing, Var rowVar, Var colVar, Var weights,
            String rowLevel) {
        var rowIndex = withMissing ? IndexLabel.fromLabelValues("?", rowLevel, "other") : IndexLabel.fromLabelValues(rowLevel, "other");
        var colIndex = IndexLabel.fromVarLevels(withMissing, colVar);
        var dt = new DensityTable<>(rowIndex, colIndex);

        for (int i = 0; i < min(rowVar.size(), colVar.size()); i++) {
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
            dt.inc(rowId, colIndex.getIndex(colVar, i), weights != null ? weights.getDouble(i) : 1);
        }
        return dt;
    }

    @Serial
    private static final long serialVersionUID = 4359080329548577980L;

    public static final List<String> NUMERIC_DEFAULT_LABELS = Arrays.asList("?", "less-equals", "greater");

    private final Index<U> rowIndex;
    private final Index<V> colIndex;

    private final NArray<Double> values;

    public DensityTable(Index<U> rowIndex, Index<V> colIndex) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.values = NArrayManager.base().zeros(DType.DOUBLE, Shape.of(rowIndex.size(), colIndex.size()));
    }

    public int rows() {
        return rowIndex.size();
    }

    public int cols() {
        return colIndex.size();
    }

    public Index<U> rowIndex() {
        return rowIndex;
    }

    public Index<V> colIndex() {
        return colIndex;
    }

    public double get(int row, int col) {
        return values.getDouble(row, col);
    }

    public double get(U row, V col) {
        return values.getDouble(rowIndex.getIndex(row), colIndex.getIndex(col));
    }

    public void inc(int row, int col, double weight) {
        values.incDouble(weight, row, col);
    }

    public void inc(U row, V col, double weight) {
        values.incDouble(weight, rowIndex.getIndex(row), colIndex.getIndex(col));
    }

    public double total() {
        return values.sum();
    }

    public double[] rowTotals() {
        return values.sum1d(1).asDoubleArray();
    }

    public double[] colTotals() {
        return values.sum1d(0).asDoubleArray();
    }

    private DensityTable<U, V> copy() {
        var copy = new DensityTable<>(rowIndex, colIndex);
        values.copyTo(copy.values);
        return copy;
    }

    public DensityTable<U, V> normalizeOverall() {
        var norm = copy();
        double total = norm.total();
        if (total > 0) {
            norm.values.div_(total);
        }
        return norm;
    }

    public DensityTable<U, V> normalizeOnRows() {
        var norm = copy();
        norm.values.div_(norm.values.sum1d(1).stretch(1));
        return norm;
    }

    public DensityTable<U, V> normalizeOnCols() {
        var norm = copy();
        norm.values.div_(norm.values.sum1d(0));
        return norm;
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
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
        return tt.getDynamicText(printer, options);
    }

    /**
     * Computes the number of columns which have totals equal or greater than minWeight.
     *
     * @return number of columns which meet criteria
     */
    public boolean hasColsWithMinimumCount(double minWeight, int minCounts) {
        int count = 0;
        for (int i = 0; i < rowIndex.size(); i++) {
            double total = 0;
            for (int j = 1; j < colIndex.size(); j++) {
                total += values.getDouble(i, j);
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
                tt.floatFlex(i + 1, j + 1, values.getDouble(i, j));
            }
        }
    }

    public double splitByRowAverageEntropy() {
        return concreteRowAverageEntropy.getSplitInfo();
    }

    public double splitByRowInfoGain() {
        double totalColEntropy = concreteTotalColEntropy.getSplitInfo();
        return totalColEntropy - splitByRowAverageEntropy();
    }

    public double splitByRowGainRatio() {
        double splitByRowIntrinsicInfo = concreteRowIntrinsicInfo.getSplitInfo();
        return splitByRowInfoGain() / splitByRowIntrinsicInfo;
    }

    public double splitByRowGiniGain() {
        return giniGain(true);
    }

    public double splitByColGiniGain() {
        return giniGain(false);
    }

    private double giniGain(boolean splitByRows) {
        double total = values.sum();
        if (total <= 0) {
            return 1;
        }

        var rowTotals = values.sum1d(1);
        var colTotals = values.sum1d(0);

        var splitByTotals = splitByRows ? rowTotals : colTotals;
        var straightTotals = splitByRows ? colTotals : rowTotals;

        double gini = 1.0 - straightTotals.copy().div(total).sqr().sum();

        for (int i = 0; i < splitByTotals.size(); i++) {
            double ginik = 1;
            for (int j = 0; j < straightTotals.size(); j++) {
                if (splitByTotals.getDouble(i) > 0) {
                    var value = splitByRows ? values.getDouble(i, j) : values.getDouble(j, i);
                    ginik -= pow(value / splitByTotals.getDouble(i), 2);
                }
            }
            gini -= ginik * splitByTotals.getDouble(i) / total;
        }

        return gini;
    }

    private final DensityTableFunction concreteRowAverageEntropy = new DensityTableFunction(this, true,
            (double total, double[] totals, NArray<Double> values, int rowLength, int colLength) -> {
                double gain = 0;
                for (int i = 0; i < rowLength; i++) {
                    for (int j = 0; j < colLength; j++) {
                        if (values.getDouble(i, j) > 0) {
                            gain += -log2(values.getDouble(i, j) / totals[i]) * values.getDouble(i, j) / total;
                        }
                    }
                }
                return gain;
            });

    private final DensityTableFunction concreteRowIntrinsicInfo = new DensityTableFunction(this, true,
            (double total, double[] totals, NArray<Double> values, int rowLength, int colLength) -> {
                double splitInfo = 0;
                for (double val : totals) {
                    if (val > 0) {
                        splitInfo += -log2(val / total) * val / total;
                    }
                }
                return splitInfo;
            });
    private final DensityTableFunction concreteTotalColEntropy = new DensityTableFunction(this, false,
            (double total, double[] totals, NArray<Double> values, int rowLength, int colLength) -> {
                double entropy = 0;
                for (double val : totals) {
                    if (val > 0) {
                        entropy += -log2(val / total) * val / total;
                    }
                }
                return entropy;
            });

    record DensityTableFunction(DensityTable<?, ?> dt, boolean onRow, Function function) {

        public double getSplitInfo() {
            double[] totals = new double[onRow ? dt.rowIndex.size() : dt.colIndex.size()];
            for (int i = 0; i < dt.rowIndex.size(); i++) {
                for (int j = 0; j < dt.colIndex.size(); j++) {
                    totals[onRow ? i : j] += dt.values.getDouble(i, j);
                }
            }
            double total = nanSum(totals, 0, totals.length);
            return function.apply(total, totals, dt.values, dt.rowIndex.size(), dt.colIndex.size());
        }
    }

    @FunctionalInterface
    interface Function {
        double apply(double total, double[] totals, NArray<Double> values, int rowLength, int colLength);
    }
}

