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

package rapaio.core.tools;

import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

import java.io.Serializable;
import java.util.Arrays;

import static rapaio.math.MTools.log2;

/**
 * Distribution table.
 * <p>
 * Table tool class to facilitate various operations on two variables regarding frequencies.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class DTable implements Printable, Serializable {

    public static final String[] NUMERIC_DEFAULT_LABELS = new String[]{"?", "less-equals", "greater"};
    private static final long serialVersionUID = 4359080329548577980L;
    private final String[] rowLevels;
    private final String[] colLevels;

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
    public static DTable empty(String[] rowLevels, String[] colLevels, boolean useFirst) {
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
        return new DTable(rowVar, colVar, NumericVar.fill(rowVar.getRowCount(), 1), useFirst);
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

    private DTable(String[] rowLevels, String[] colLevels, boolean useFirst) {
        this.rowLevels = rowLevels;
        this.colLevels = colLevels;
        this.start = useFirst ? 0 : 1;
        this.values = new double[rowLevels.length][colLevels.length];
    }

    private DTable(Var rowVar, Var colVar, Var weights, boolean useFirst) {
        this(rowVar.getLevels(), colVar.getLevels(), useFirst);

        if (!(rowVar.getType().isNominal() || rowVar.getType().equals(VarType.BINARY)))
            throw new IllegalArgumentException("row var must be nominal");
        if (!(colVar.getType().isNominal() || colVar.getType().equals(VarType.BINARY)))
            throw new IllegalArgumentException("col var is not nominal");
        if (rowVar.getRowCount() != colVar.getRowCount())
            throw new IllegalArgumentException("row and col vars must have same row count");

        int rowOffset = rowVar.getType().equals(VarType.BINARY) ? 1 : 0;
        int colOffset = colVar.getType().equals(VarType.BINARY) ? 1 : 0;
        for (int i = 0; i < rowVar.getRowCount(); i++) {
            update(rowVar.getIndex(i) + rowOffset, colVar.getIndex(i) + colOffset, weights != null ? weights.getValue(i) : 1);
        }
    }

    private DTable(Var rowVar, Var colVar, Var weights, String rowLevel, boolean useFirst) {
        this(new String[]{"?", rowLevel, "other"}, colVar.getLevels(), useFirst);

        if (!rowVar.getType().isNominal()) throw new IllegalArgumentException("row var must be nominal");
        if (!colVar.getType().isNominal()) throw new IllegalArgumentException("col var is not nominal");
        if (rowVar.getRowCount() != colVar.getRowCount())
            throw new IllegalArgumentException("row and col variables must have same size");

        for (int i = 0; i < rowVar.getRowCount(); i++) {
            int index = 0;
            if (!rowVar.isMissing(i)) {
                index = (rowVar.getLabel(i).equals(rowLevel)) ? 1 : 2;
            }
            update(index, colVar.getIndex(i), weights != null ? weights.getValue(i) : 1);
        }
    }

    public boolean useFirst() {
        return start == 0;
    }

    public int start() {
        return start;
    }

    public int getRowCount() {
        return rowLevels.length;
    }

    public int getgetColCount() {
        return colLevels.length;
    }

    public String[] getRowLevels() {
        return rowLevels;
    }

    public String[] getColLevels() {
        return colLevels;
    }

    public DTable withTotalSummary(boolean totalSummary) {
        this.totalSummary = totalSummary;
        return this;
    }

    public double get(int row, int col) {
        return values[row][col];
    }

    public void reset() {
        for (double[] line : values) Arrays.fill(line, 0, line.length, 0);
    }

    public void update(int row, int col, double weight) {
        values[row][col] += weight;
    }

    public void moveOnCol(int row1, int row2, int col, double weight) {
        update(row1, col, -weight);
        update(row2, col, weight);
    }

    public void moveOnRow(int row, int col1, int col2, double weight) {
        update(row, col1, -weight);
        update(row, col2, weight);
    }

    public double totalColEntropy() {
    	AbstractSplit abstractSplit = new ConcreteTotalColEntropy();
        double entropy = abstractSplit.getSplitInfo(start, rowLevels.length, colLevels.length, values);
        return entropy;
    }

    public double totalRowEntropy() {
    	AbstractSplit abstractSplit = new ConcreteTotalRowEntropy();
        double entropy = abstractSplit.getSplitInfo(start, rowLevels.length, colLevels.length, values);
        return entropy;
    }

    public double splitByRowAverageEntropy() {
    	AbstractSplit abstractSplit = new ConcreteRowAverageEntropy();
        double gain = abstractSplit.getSplitInfo(start, rowLevels.length, colLevels.length, values);
        return gain;
    }

    public double splitByColAverageEntropy() {
    	AbstractSplit abstractSplit = new ConcreteColAverageEntropy();
        double gain = abstractSplit.getSplitInfo(start, rowLevels.length, colLevels.length, values);
        return gain;
    }

    public double splitByRowInfoGain() {
        return totalColEntropy() - splitByRowAverageEntropy();
    }

    public double splitByColInfoGain() {
        return totalRowEntropy() - splitByColAverageEntropy();
    }

    public double splitByRowIntrinsicInfo() {
        AbstractSplit abstractSplit = new ConcreteRowIntrinsicInfo();
        double splitInfo = abstractSplit.getSplitInfo(start, rowLevels.length, colLevels.length, values);
        return splitInfo;
    }

    public double splitByColIntrinsicInfo() {
    	AbstractSplit abstractSplit = new ConcreteColIntrinsicInfo();
        double splitInfo = abstractSplit.getSplitInfo(start, rowLevels.length, colLevels.length, values);
        return splitInfo;
    }

    public double splitByRowGainRatio() {
        return splitByRowInfoGain() / splitByRowIntrinsicInfo();
    }

    public double splitByColGainRatio() {
        return splitByColInfoGain() / splitByColIntrinsicInfo();
    }

    /**
     * Computes the number of columns which have totals equal or greater than minWeight
     *
     * @return number of columns which meet criteria
     */
    public boolean hasColsWithMinimumCount(double minWeight, int minCounts) {
        int count = 0;
        for (int i = start; i < rowLevels.length; i++) {
            double total = 0;
            for (int j = 1; j < colLevels.length; j++) {
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
        double[] rowTotals = new double[rowLevels.length];
        double[] colTotals = new double[colLevels.length];
        double total = 0.0;
        for (int i = start; i < rowLevels.length; i++) {
            for (int j = start; j < colLevels.length; j++) {
                rowTotals[i] += values[i][j];
                colTotals[j] += values[i][j];
                total += values[i][j];
            }
        }
        if (total <= 0) {
            return 1;
        }

        double gini = 1.0;
        for (int i = start; i < colLevels.length; i++) {
            gini -= Math.pow(colTotals[i] / total, 2);
        }

        for (int i = start; i < rowLevels.length; i++) {
            double gini_k = 1;
            for (int j = start; j < colLevels.length; j++) {
                if (rowTotals[i] > 0)
                    gini_k -= Math.pow(values[i][j] / rowTotals[i], 2);
            }
            gini -= gini_k * rowTotals[i] / total;
        }
        return gini;
    }

    public double splitByColGiniGain() {
        double[] rowTotals = new double[rowLevels.length];
        double[] colTotals = new double[colLevels.length];
        double total = 0.0;
        for (int i = start; i < rowLevels.length; i++) {
            for (int j = start; j < colLevels.length; j++) {
                rowTotals[i] += values[i][j];
                colTotals[j] += values[i][j];
                total += values[i][j];
            }
        }
        if (total <= 0) {
            return 1;
        }

        double gini = 1.0;
        for (int i = start; i < rowLevels.length; i++) {
            gini -= Math.pow(rowTotals[i] / total, 2);
        }

        for (int i = start; i < colLevels.length; i++) {
            double gini_k = 1;
            for (int j = start; j < rowLevels.length; j++) {
                if (colTotals[i] > 0)
                    gini_k -= Math.pow(values[j][i] / colTotals[i], 2);
            }
            gini -= gini_k * colTotals[i] / total;
        }
        return gini;
    }

    public double[] rowTotals() {
        double[] totals = new double[rowLevels.length];
        for (int i = 0; i < rowLevels.length; i++) {
            for (int j = 0; j < colLevels.length; j++) {
                totals[i] += values[i][j];
            }
        }
        return totals;
    }

    public double[] colTotals() {
        double[] totals = new double[colLevels.length];
        for (int i = 0; i < rowLevels.length; i++) {
            for (int j = 0; j < colLevels.length; j++) {
                totals[j] += values[i][j];
            }
        }
        return totals;
    }

    public DTable normalizeOverall() {
        DTable norm = DTable.empty(rowLevels, colLevels, start == 0).withTotalSummary(totalSummary);
        double total = 0;
        for (int i = start; i < rowLevels.length; i++) {
            for (int j = start; j < colLevels.length; j++) {
                norm.values[i][j] = values[i][j];
                total += values[i][j];
            }
        }
        if (total > 0) {
            for (int i = start; i < rowLevels.length; i++) {
                for (int j = start; j < colLevels.length; j++) {
                    norm.values[i][j] /= total;
                }
            }
        }
        return norm;
    }

    public DTable normalizeOnRows() {
        DTable norm = DTable.empty(rowLevels, colLevels, start == 0).withTotalSummary(totalSummary);
        double[] rowTotals = new double[rowLevels.length];
        for (int i = start; i < rowLevels.length; i++) {
            for (int j = start; j < colLevels.length; j++) {
                norm.values[i][j] = values[i][j];
                rowTotals[i] += values[i][j];
            }
        }
        for (int i = start; i < rowLevels.length; i++) {
            if (rowTotals[i] > 0)
                for (int j = start; j < colLevels.length; j++) {
                    norm.values[i][j] /= rowTotals[i];
                }
        }
        return norm;
    }

    public DTable normalizeOnCols() {
        DTable norm = DTable.empty(rowLevels, colLevels, start == 0).withTotalSummary(totalSummary);
        double[] colTotals = new double[colLevels.length];
        for (int i = start; i < rowLevels.length; i++) {
            for (int j = start; j < colLevels.length; j++) {
                norm.values[i][j] = values[i][j];
                colTotals[j] += values[i][j];
            }
        }
        for (int i = start; i < colLevels.length; i++) {
            if (colTotals[i] > 0)
                for (int j = start; j < rowLevels.length; j++) {
                    norm.values[j][i] /= colTotals[i];
                }
        }
        return norm;
    }

    @Override
    public String getSummary() {

        if (totalSummary) {
            TextTable tt = TextTable.newEmpty(rowLevels.length - start + 2, colLevels.length - start + 2);
            tt.withHeaderRows(1);
            tt.withSplit(WS.getPrinter().textWidth());

            for (int i = start; i < rowLevels.length; i++) {
                tt.set(i - start + 1, 0, rowLevels[i], 1);
            }
            for (int i = start; i < colLevels.length; i++) {
                tt.set(0, i - start + 1, colLevels[i], 1);
            }
            tt.set(0, colLevels.length - start + 1, "total", 1);
            tt.set(rowLevels.length - start + 1, 0, "total", 1);
            for (int i = start; i < rowLevels.length; i++) {
                for (int j = start; j < colLevels.length; j++) {
                    tt.set(i - start + 1, j - start + 1, WS.formatShort(values[i][j]), 1);
                }
            }
            double[] rowTotals = rowTotals();
            for (int i = start; i < rowLevels.length; i++) {
                tt.set(i - start + 1, colLevels.length - start + 1, WS.formatShort(rowTotals[i]), 1);
            }
            double[] colTotals = colTotals();
            for (int i = start; i < colLevels.length; i++) {
                tt.set(rowLevels.length - start + 1, i - start + 1, WS.formatShort(colTotals[i]), 1);
            }
            double total = Arrays.stream(rowTotals).skip(start).sum();
            tt.set(rowLevels.length - start + 1, colLevels.length - start + 1, WS.formatShort(total), 1);
            return tt.getSummary();
        } else {
            TextTable tt = TextTable.newEmpty(rowLevels.length - start + 1, colLevels.length - start + 1);
            tt.withHeaderRows(1);
            tt.withSplit(WS.getPrinter().textWidth());

            for (int i = start; i < rowLevels.length; i++) {
                tt.set(i - start + 1, 0, rowLevels[i], 1);
            }
            for (int i = start; i < colLevels.length; i++) {
                tt.set(0, i - start + 1, colLevels[i], 1);
            }
            for (int i = start; i < rowLevels.length; i++) {
                for (int j = start; j < colLevels.length; j++) {
                    tt.set(i - start + 1, j - start + 1, WS.formatShort(values[i][j]), 1);
                }
            }
            return tt.getSummary();
        }
    }
}
