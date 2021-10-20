/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.base;

import java.io.Serial;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import rapaio.math.MathTools;
import rapaio.math.linear.Algebra;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.math.linear.decomposition.MatrixMultiplication;
import rapaio.math.linear.decomposition.SVDecomposition;
import rapaio.math.linear.dense.DMatrixMap;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public abstract class AbstractDMatrix implements DMatrix {

    @Serial
    private static final long serialVersionUID = -8475836385935066885L;

    @Override
    public abstract MType innerType();

    protected void checkMatrixSameSize(DMatrix b) {
        if ((rowCount() != b.rowCount()) || (colCount() != b.colCount())) {
            throw new IllegalArgumentException("Matrices are not conform with this operation.");
        }
    }

    @Override
    public DVector mapRow(final int row, AlgebraOption<?>... opts) {
        DVector v = DVector.zeros(colCount());
        for (int j = 0; j < colCount(); j++) {
            v.set(j, get(row, j));
        }
        return v;
    }

    @Override
    public DVector mapCol(int col, AlgebraOption<?>... opts) {
        DVector v = DVector.zeros(rowCount());
        for (int j = 0; j < rowCount(); j++) {
            v.set(j, get(j, col));
        }
        return v;
    }

    @Override
    public DMatrix map(int[] indexes, int axis, AlgebraOption<?>... opts) {
        return switch (axis) {
            case 0 -> mapRows(indexes, opts);
            case 1 -> mapCols(indexes, opts);
            default -> throw new IllegalArgumentException("Valid axis value must be specified.");
        };
    }

    public DMatrix mapRows(final int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            DMatrix copy = DMatrix.empty(indexes.length, colCount());
            for (int i = 0; i < indexes.length; i++) {
                for (int j = 0; j < colCount(); j++) {
                    copy.set(i, j, get(indexes[i], j));
                }
            }
            return copy;
        }
        return new DMatrixMap(this, true, indexes);
    }

    public DMatrix mapCols(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            DMatrix copy = DMatrix.empty(rowCount(), indexes.length);
            for (int i = 0; i < rowCount(); i++) {
                for (int j = 0; j < indexes.length; j++) {
                    copy.set(i, j, get(i, indexes[j]));
                }
            }
            return copy;
        }
        return new DMatrixMap(this, false, indexes);
    }

    @Override
    public DVector mapValues(int[] indexes, int axis) {
        if (axis == 0) {
            DVector v = DVector.zeros(rowCount());
            for (int i = 0; i < rowCount(); i++) {
                v.set(i, get(i, indexes[i]));
            }
            return v;
        } else {
            DVector v = DVector.zeros(colCount());
            for (int i = 0; i < colCount(); i++) {
                v.set(i, get(indexes[i], i));
            }
            return v;
        }
    }

    @Override
    public DMatrix range(int start, int end, int axis, AlgebraOption<?>... opts) {
        return switch (axis) {
            case 0 -> rangeRows(start, end, opts);
            case 1 -> rangeCols(start, end, opts);
            default -> throw new IllegalArgumentException("Valid axis values must be specified.");
        };
    }

    public DMatrix rangeRows(final int start, final int end, AlgebraOption<?>... opts) {
        return mapRows(IntArrays.newSeq(start, end), opts);
    }

    public DMatrix rangeCols(int start, int end, AlgebraOption<?>... opts) {
        int[] cols = IntArrays.newSeq(start, end);
        return mapCols(cols, opts);
    }

    @Override
    public DMatrix remove(int[] indexes, int axis, AlgebraOption<?>... opts) {
        return switch (axis) {
            case 0 -> removeRows(indexes, opts);
            case 1 -> removeCols(indexes, opts);
            default -> throw new IllegalArgumentException("Valid axis values must be specified.");
        };
    }

    public DMatrix removeRows(int[] indexes, AlgebraOption<?>... opts) {
        Set<Integer> rem = Arrays.stream(indexes).boxed()
                .filter(v -> v >= 0)
                .filter(v -> v < rowCount())
                .collect(Collectors.toSet());
        int[] rows = new int[rowCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rowCount(); i++) {
            if (rem.contains(i)) {
                continue;
            }
            rows[pos++] = i;
        }
        return mapRows(rows, opts);
    }

    public DMatrix removeCols(int[] indexes, AlgebraOption<?>... opts) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] cols = new int[colCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < colCount(); i++) {
            if (rem.contains(i)) {
                continue;
            }
            cols[pos++] = i;
        }
        return mapCols(cols, opts);
    }

    @Override
    public DMatrix add(double x, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                ref.set(i, j, get(i, j) + x);
            }
        }
        return ref;
    }

    @Override
    public DMatrix add(DVector v, int axis, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        if (axis == 0) {
            if (v.size() != ref.colCount()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < ref.rowCount(); i++) {
                for (int j = 0; j < ref.colCount(); j++) {
                    ref.set(i, j, get(i, j) + v.get(j));
                }
            }
        } else {
            if (v.size() != ref.rowCount()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < ref.rowCount(); i++) {
                for (int j = 0; j < ref.colCount(); j++) {
                    ref.set(i, j, get(i, j) + v.get(i));
                }
            }
        }
        return ref;
    }

    @Override
    public DMatrix add(DMatrix b, AlgebraOption<?>... opts) {
        checkMatrixSameSize(b);
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < ref.rowCount(); i++) {
            for (int j = 0; j < ref.colCount(); j++) {
                ref.set(i, j, get(i, j) + b.get(i, j));
            }
        }
        return ref;
    }

    @Override
    public DMatrix sub(double x, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                ref.set(i, j, get(i, j) - x);
            }
        }
        return ref;
    }

    @Override
    public DMatrix sub(DVector v, int axis, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        if (axis == 0) {
            if (v.size() != ref.colCount()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < ref.rowCount(); i++) {
                for (int j = 0; j < ref.colCount(); j++) {
                    ref.set(i, j, get(i, j) - v.get(j));
                }
            }
        } else {
            if (v.size() != ref.rowCount()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < ref.rowCount(); i++) {
                for (int j = 0; j < ref.colCount(); j++) {
                    ref.set(i, j, get(i, j) - v.get(i));
                }
            }
        }
        return ref;
    }

    @Override
    public DMatrix sub(DMatrix b, AlgebraOption<?>... opts) {
        checkMatrixSameSize(b);
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                ref.set(i, j, get(i, j) - b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix mul(double x, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                ref.set(i, j, get(i, j) * x);
            }
        }
        return ref;
    }

    @Override
    public DMatrix mul(DVector v, int axis, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        if (axis == 0) {
            if (v.size() != colCount()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    ref.set(i, j, get(i, j) * v.get(j));
                }
            }
        } else {
            if (v.size() != rowCount()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    ref.set(i, j, get(i, j) * v.get(i));
                }
            }
        }
        return ref;
    }

    @Override
    public DMatrix mul(DMatrix b, AlgebraOption<?>... opts) {
        checkMatrixSameSize(b);
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                ref.set(i, j, get(i, j) * b.get(i, j));
            }
        }
        return ref;
    }

    @Override
    public DMatrix div(double x, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                ref.set(i, j, get(i, j) / x);
            }
        }
        return ref;
    }

    @Override
    public DMatrix div(DVector v, int axis, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        if (axis == 0) {
            if (v.size() != colCount()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    ref.set(i, j, get(i, j) / v.get(j));
                }
            }
        } else {
            if (v.size() != rowCount()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    ref.set(i, j, get(i, j) / v.get(i));
                }
            }
        }
        return ref;
    }

    @Override
    public DMatrix div(DMatrix b, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                ref.set(i, j, get(i, j) / b.get(i, j));
            }
        }
        return ref;
    }

    @Override
    public DMatrix dot(DMatrix B) {
        if (colCount() != B.rowCount()) {
            throw new IllegalArgumentException(
                    String.format("Matrices not conformant for multiplication: (%d,%d) x (%d,%d)",
                            rowCount(), colCount(), B.rowCount(), B.colCount()));
        }
        return MatrixMultiplication.copyParallel(this, B);
    }

    @Override
    public DVector dot(DVector b) {
        if (colCount() != b.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrices not conformant for multiplication: (%d,%d) x (%d,%d)",
                            rowCount(), colCount(), b.size(), 1));
        }
        return MatrixMultiplication.ikjParallel(this, b);
    }

    /**
     * Trace of the matrix, if the matrix is square. The trace of a squared
     * matrix is the sum of the elements from the main diagonal.
     * Otherwise returns an exception.
     *
     * @return value of the matrix trace
     */
    @Override
    public double trace() {
        if (rowCount() != colCount()) {
            throw new IllegalArgumentException("Matrix is not squared, trace of the matrix is not defined.");
        }
        double sum = 0;
        for (int i = 0; i < rowCount(); i++) {
            sum += get(i, i);
        }
        return sum;
    }

    /**
     * Matrix rank
     *
     * @return effective numerical rank, obtained from SVD.
     */
    @Override
    public int rank() {
        return SVDecomposition.from(this).rank();
    }

    /**
     * Diagonal vector of values
     */
    @Override
    public DVector diag() {
        DVector v = DVector.zeros(colCount());
        for (int i = 0; i < colCount(); i++) {
            v.set(i, get(i, i));
        }
        return v;
    }

    @Override
    public DMatrix scatter() {
        DMatrix scatter = DMatrix.empty(DMatrix.defaultMType(), colCount(), colCount());
        DVector mean = DVector.zeros(colCount());
        for (int i = 0; i < colCount(); i++) {
            mean.set(i, mapCol(i).mean());
        }
        for (int k = 0; k < rowCount(); k++) {
            DVector row = mapRow(k, Algebra.copy()).sub(mean);
            for (int i = 0; i < row.size(); i++) {
                for (int j = 0; j < row.size(); j++) {
                    scatter.inc(i, j, row.get(i) * row.get(j));
                }
            }
        }
        return scatter;
    }

    @Override
    public double sum() {
        double sum = 0;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                sum += get(i, j);
            }
        }
        return sum;
    }

    @Override
    public DVector sum(int axis) {
        if (axis == 0) {
            double[] sum = new double[colCount()];
            for (int i = 0; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    sum[j] += get(i, j);
                }
            }
            return DVector.wrap(sum);
        }
        double[] sum = new double[rowCount()];
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                sum[i] += get(i, j);
            }
        }
        return DVector.wrap(sum);
    }

    @Override
    public double variance() {
        if (rowCount() == 0 || colCount() == 0) {
            return Double.NaN;
        }
        double mean = mean();
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                sum2 += Math.pow(get(i, j) - mean, 2);
                sum3 += get(i, j) - mean;
            }
        }
        double size = rowCount() * colCount();
        return (sum2 - Math.pow(sum3, 2) / size) / (size - 1.0);
    }

    @Override
    public DVector variance(int axis) {
        if (rowCount() == 0 || colCount() == 0) {
            return DVector.fill(axis == 0 ? colCount() : rowCount(), Double.NaN);
        }
        if (axis == 0) {
            DVector variance = DVector.fill(colCount(), 0);
            for (int i = 0; i < colCount(); i++) {
                variance.inc(i, mapCol(i).variance());
            }
            return variance;
        }
        DVector variance = DVector.fill(rowCount(), 0);
        for (int i = 0; i < rowCount(); i++) {
            variance.inc(i, mapRow(i).variance());
        }
        return variance;
    }

    @Override
    public DVector amax(int axis) {
        if (axis == 0) {
            DVector max = DVector.copy(mapRow(0));
            for (int i = 1; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    if (max.get(j) < get(i, j)) {
                        max.set(j, get(i, j));
                    }
                }
            }
            return max;
        }
        DVector max = DVector.copy(mapCol(0));
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 1; j < colCount(); j++) {
                if (max.get(i) < get(i, j)) {
                    max.set(i, get(i, j));
                }
            }
        }
        return max;
    }

    @Override
    public int[] argmax(int axis) {
        if (axis == 0) {
            int[] max = new int[colCount()];
            for (int i = 1; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    if (get(max[j], j) < get(i, j)) {
                        max[j] = i;
                    }
                }
            }
            return max;
        }
        int[] max = new int[rowCount()];
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 1; j < colCount(); j++) {
                if (get(i, max[i]) < get(i, j)) {
                    max[i] = j;
                }
            }
        }
        return max;
    }

    @Override
    public DVector amin(int axis) {
        if (axis == 0) {
            DVector min = DVector.copy(mapRow(0));
            for (int i = 1; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    if (min.get(j) > get(i, j)) {
                        min.set(j, get(i, j));
                    }
                }
            }
            return min;
        }
        DVector min = DVector.copy(mapCol(0));
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 1; j < colCount(); j++) {
                if (min.get(i) > get(i, j)) {
                    min.set(i, get(i, j));
                }
            }
        }
        return min;
    }

    @Override
    public int[] argmin(int axis) {
        if (axis == 0) {
            int[] min = new int[colCount()];
            for (int i = 1; i < rowCount(); i++) {
                for (int j = 0; j < colCount(); j++) {
                    if (get(min[j], j) > get(i, j)) {
                        min[j] = i;
                    }
                }
            }
            return min;
        }
        int[] min = new int[rowCount()];
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 1; j < colCount(); j++) {
                if (get(i, min[i]) > get(i, j)) {
                    min[i] = j;
                }
            }
        }
        return min;
    }

    @Override
    public DMatrix apply(Double2DoubleFunction fun, AlgebraOption<?>... opts) {
        DMatrix ref = AlgebraOptions.from(opts).isCopy() ? DMatrix.empty(rowCount(), colCount()) : this;
        for (int i = 0; i < ref.rowCount(); i++) {
            for (int j = 0; j < ref.colCount(); j++) {
                ref.set(i, j, fun.apply(get(i, j)));
            }
        }
        return ref;
    }

    @Override
    public DMatrix t(AlgebraOption<?>... opts) {
        DMatrix t = DMatrix.empty(colCount(), rowCount());
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                t.set(j, i, get(i, j));
            }
        }
        return t;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.range(0, rowCount() * colCount())
                .mapToDouble(i -> get(Math.floorDiv(i, colCount()), Math.floorMod(i, colCount())));
    }

    @Override
    public DMatrix copy() {
        DMatrix copy = DMatrix.empty(rowCount(), colCount());
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                copy.set(i, j, get(i, j));
            }
        }
        return copy;
    }

    @Override
    public DMatrix resizeCopy(int rows, int cols, double fill) {
        DMatrix copy = DMatrix.empty(innerType(), rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i < rowCount() && j < colCount()) {
                    copy.set(i, j, get(i, j));
                } else {
                    copy.set(i, j, fill);
                }
            }
        }
        return copy;
    }

    @Override
    public boolean deepEquals(DMatrix m, double eps) {
        if (rowCount() != m.rowCount()) {
            return false;
        }
        if (colCount() != m.colCount()) {
            return false;
        }
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                if (!MathTools.eq(get(i, j), m.get(i, j), eps)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("{");
        sb.append("rowCount:").append(rowCount()).append(", colCount:").append(colCount()).append(", values:\n");
        sb.append("[\n");

        int minCols = 10;
        int minRows = 24;
        int ttRows = Math.min(minRows, rowCount());
        int ttCols = Math.min(minCols, colCount());
        int extraRow = rowCount() > minRows ? 1 : 0;
        int extraCol = colCount() > minCols ? 1 : 0;
        TextTable tt = TextTable.empty(ttRows + extraRow, ttCols + extraCol + 2);

        for (int i = 0; i < ttRows + extraRow; i++) {
            for (int j = 0; j < ttCols + extraCol + 2; j++) {
                if (j == 0) {
                    tt.textCenter(i, j, " [");
                    continue;
                }
                if (j == ttCols + extraCol + 1) {
                    tt.textCenter(i, j, "]" + ((i != ttRows + extraRow) ? ',' : ""));
                    continue;
                }
                if (extraCol == 1 && j == ttCols + extraCol) {
                    tt.textCenter(i, j, "..");
                    continue;
                }
                if (extraRow == 1 && i == ttRows + extraRow - 1) {
                    tt.textCenter(i, j, "..");
                    continue;
                }
                tt.floatFlex(i, j, get(i, j - 1));
            }
        }
        sb.append(tt.getRawText()).append("]}");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        int headRows = 20;
        int headCols = 20;
        int tailRows = 2;
        int tailCols = 2;

        boolean fullRows = headRows + tailRows >= rowCount();
        boolean fullCols = headCols + tailCols >= colCount();

        if (fullRows && fullCols) {
            return toFullContent(printer, options);
        }

        int[] rows = new int[Math.min(headRows + tailRows + 1, rowCount())];

        if (fullRows) {
            for (int i = 0; i < rowCount(); i++) {
                rows[i] = i;
            }
        } else {
            for (int i = 0; i < headRows; i++) {
                rows[i] = i;
            }
            rows[headRows] = -1;
            for (int i = 0; i < tailRows; i++) {
                rows[i + headRows + 1] = i + rowCount() - tailRows;
            }
        }
        int[] cols = new int[Math.min(headCols + tailCols + 1, colCount())];
        if (fullCols) {
            for (int i = 0; i < colCount(); i++) {
                cols[i] = i;
            }
        } else {
            for (int i = 0; i < headCols; i++) {
                cols[i] = i;
            }
            cols[headCols] = -1;
            for (int i = 0; i < tailCols; i++) {
                cols[i + headCols + 1] = i + colCount() - tailCols;
            }
        }
        TextTable tt = TextTable.empty(rows.length + 1, cols.length + 1, 1, 1);
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == -1) {
                tt.textCenter(i + 1, 0, "...");
            } else {
                tt.intRow(i + 1, 0, rows[i]);
            }
        }
        for (int i = 0; i < cols.length; i++) {
            if (cols[i] == -1) {
                tt.textCenter(0, i + 1, "...");
            } else {
                tt.intRow(0, i + 1, cols[i]);
            }
        }
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                if (rows[i] == -1 || cols[j] == -1) {
                    tt.textCenter(i + 1, j + 1, "...");
                } else {
                    tt.floatFlexLong(i + 1, j + 1, get(rows[i], cols[j]));
                }
            }
        }
        return tt.getDynamicText(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {

        TextTable tt = TextTable.empty(rowCount() + 1, colCount() + 1, 1, 1);
        for (int i = 0; i < rowCount(); i++) {
            tt.intRow(i + 1, 0, i);
        }
        for (int i = 0; i < colCount(); i++) {
            tt.intRow(0, i + 1, i);
        }
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                tt.floatFlexLong(i + 1, j + 1, get(i, j));
            }
        }
        return tt.getDynamicText(printer, options);
    }
}
