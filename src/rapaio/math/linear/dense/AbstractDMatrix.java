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

package rapaio.math.linear.dense;

import java.io.Serial;
import java.text.DecimalFormat;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import rapaio.math.MathTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.decomposition.MatrixMultiplication;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.function.Double2DoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public abstract class AbstractDMatrix implements DMatrix {

    @Serial
    private static final long serialVersionUID = -8475836385935066885L;

    protected void checkMatrixSameSize(DMatrix b) {
        if ((rows() != b.rows()) || (cols() != b.cols())) {
            throw new IllegalArgumentException("Matrices are not conform with this operation.");
        }
    }

    @Override
    public DVector mapValues(int[] indexes, int axis) {
        DVector v = DVector.zeros(axis == 0 ? cols() : rows());
        if (axis == 0) {
            for (int i = 0; i < cols(); i++) {
                v.set(i, get(indexes[i], i));
            }
        } else {
            for (int i = 0; i < rows(); i++) {
                v.set(i, get(i, indexes[i]));
            }
        }
        return v;
    }

    @Override
    public DMatrix add(double x) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                inc(i, j, x);
            }
        }
        return this;
    }

    @Override
    public DMatrix addTo(double x, DMatrix to) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(i, j, get(i, j) + x);
            }
        }
        return to;
    }

    @Override
    public DMatrix add(DVector v, int axis) {
        if (axis == 0) {
            if (v.size() != cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    inc(i, j, v.get(j));
                }
            }
        } else {
            if (v.size() != rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    inc(i, j, v.get(i));
                }
            }
        }
        return this;
    }

    @Override
    public DMatrix addTo(DVector v, int axis, DMatrix to) {
        if (axis == 0) {
            if (v.size() != to.cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < to.rows(); i++) {
                for (int j = 0; j < to.cols(); j++) {
                    to.set(i, j, get(i, j) + v.get(j));
                }
            }
        } else {
            if (v.size() != to.rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < to.rows(); i++) {
                for (int j = 0; j < to.cols(); j++) {
                    to.set(i, j, get(i, j) + v.get(i));
                }
            }
        }
        return to;
    }

    @Override
    public DMatrix add(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                inc(i, j, b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix addTo(DMatrix b, DMatrix to) {
        checkMatrixSameSize(b);
        for (int i = 0; i < to.rows(); i++) {
            for (int j = 0; j < to.cols(); j++) {
                to.set(i, j, get(i, j) + b.get(i, j));
            }
        }
        return to;
    }

    @Override
    public DMatrix sub(double x) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                inc(i, j, -x);
            }
        }
        return this;
    }

    @Override
    public DMatrix subTo(double x, DMatrix to) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(i, j, get(i, j) - x);
            }
        }
        return to;
    }

    @Override
    public DMatrix sub(DVector v, int axis) {
        if (axis == 0) {
            if (v.size() != cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    inc(i, j, -v.get(j));
                }
            }
        } else {
            if (v.size() != rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    inc(i, j, -v.get(i));
                }
            }
        }
        return this;
    }

    @Override
    public DMatrix subTo(DVector v, int axis, DMatrix to) {
        if (axis == 0) {
            if (v.size() != to.cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < to.rows(); i++) {
                for (int j = 0; j < to.cols(); j++) {
                    to.set(i, j, get(i, j) - v.get(j));
                }
            }
        } else {
            if (v.size() != to.rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < to.rows(); i++) {
                for (int j = 0; j < to.cols(); j++) {
                    to.set(i, j, get(i, j) - v.get(i));
                }
            }
        }
        return to;
    }

    @Override
    public DMatrix sub(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                inc(i, j, -b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix subTo(DMatrix b, DMatrix to) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(i, j, get(i, j) - b.get(i, j));
            }
        }
        return to;
    }

    @Override
    public DMatrix mul(double x) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                set(i, j, get(i, j) * x);
            }
        }
        return this;
    }

    @Override
    public DMatrix mulTo(double x, DMatrix to) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(i, j, get(i, j) * x);
            }
        }
        return to;
    }

    @Override
    public DMatrix mul(DVector v, int axis) {
        if (axis == 0) {
            if (v.size() != cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    set(i, j, get(i, j) * v.get(j));
                }
            }
        } else {
            if (v.size() != rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    set(i, j, get(i, j) * v.get(i));
                }
            }
        }
        return this;
    }

    @Override
    public DMatrix mulTo(DVector v, int axis, DMatrix to) {
        if (axis == 0) {
            if (v.size() != cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    to.set(i, j, get(i, j) * v.get(j));
                }
            }
        } else {
            if (v.size() != rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    to.set(i, j, get(i, j) * v.get(i));
                }
            }
        }
        return to;
    }

    @Override
    public DMatrix mul(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                set(i, j, get(i, j) * b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix mulTo(DMatrix b, DMatrix to) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(i, j, get(i, j) * b.get(i, j));
            }
        }
        return to;
    }

    @Override
    public DMatrix div(double x) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                set(i, j, get(i, j) / x);
            }
        }
        return this;
    }

    @Override
    public DMatrix divTo(double x, DMatrix to) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(i, j, get(i, j) / x);
            }
        }
        return to;
    }

    @Override
    public DMatrix div(DVector v, int axis) {
        if (axis == 0) {
            if (v.size() != cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    set(i, j, get(i, j) / v.get(j));
                }
            }
        } else {
            if (v.size() != rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    set(i, j, get(i, j) / v.get(i));
                }
            }
        }
        return this;
    }

    @Override
    public DMatrix divTo(DVector v, int axis, DMatrix to) {
        if (axis == 0) {
            if (v.size() != cols()) {
                throw new IllegalArgumentException("Vector has different size then the number of columns.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    to.set(i, j, get(i, j) / v.get(j));
                }
            }
        } else {
            if (v.size() != rows()) {
                throw new IllegalArgumentException("Vector has different size than the number of rows.");
            }
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    to.set(i, j, get(i, j) / v.get(i));
                }
            }
        }
        return to;
    }

    @Override
    public DMatrix div(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                set(i, j, get(i, j) / b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix divTo(DMatrix b, DMatrix to) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(i, j, get(i, j) / b.get(i, j));
            }
        }
        return to;
    }

    @Override
    public DMatrix dot(DMatrix b) {
        if (cols() != b.rows()) {
            throw new IllegalArgumentException(
                    String.format("Matrices not conformant for multiplication: (%d,%d) x (%d,%d)",
                            rows(), cols(), b.rows(), b.cols()));
        }
        return MatrixMultiplication.copyParallel(this, b);
    }

    @Override
    public DVector dot(DVector b) {
        if (cols() != b.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrices not conformant for multiplication: (%d,%d) x (%d,%d)",
                            rows(), cols(), b.size(), 1));
        }
        return MatrixMultiplication.ikjParallel(this, b);
    }

    @Override
    public boolean isSymmetric() {
        if (rows() != cols()) {
            return false;
        }
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                if (get(i, j) != get(j, i)) {
                    return false;
                }
            }
        }
        return true;
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
        if (rows() != cols()) {
            throw new IllegalArgumentException("Matrix is not squared, trace of the matrix is not defined.");
        }
        double sum = 0;
        for (int i = 0; i < rows(); i++) {
            sum += get(i, i);
        }
        return sum;
    }

    /**
     * Computes the rank of the matrix.
     *
     * @return effective numerical rank, obtained from SVD.
     */
    @Override
    public int rank() {
        return svd().rank();
    }

    /**
     * Builds a vector containing elements from the main diagonal.
     */
    @Override
    public DVector diag() {
        DVector v = DVector.zeros(cols());
        for (int i = 0; i < cols(); i++) {
            v.set(i, get(i, i));
        }
        return v;
    }

    @Override
    public DMatrix scatter() {
        DMatrix scatter = DMatrix.empty(cols(), cols());
        DVector mean = DVector.zeros(cols());
        for (int i = 0; i < cols(); i++) {
            mean.set(i, mapCol(i).mean());
        }
        for (int k = 0; k < rows(); k++) {
            DVector row = mapRowNew(k).sub(mean);
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
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                sum += get(i, j);
            }
        }
        return sum;
    }

    @Override
    public DVector sum(int axis) {
        if (axis == 0) {
            double[] sum = new double[cols()];
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    sum[j] += get(i, j);
                }
            }
            return DVector.wrap(sum);
        }
        double[] sum = new double[rows()];
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                sum[i] += get(i, j);
            }
        }
        return DVector.wrap(sum);
    }

    @Override
    public double variance() {
        double mean = mean();
        double sum2 = 0;
        double sum3 = 0;
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                sum2 += Math.pow(get(i, j) - mean, 2);
                sum3 += get(i, j) - mean;
            }
        }
        double size = rows() * cols();
        return (sum2 - Math.pow(sum3, 2) / size) / (size - 1.0);
    }

    @Override
    public DVector variance(int axis) {
        DVector variance = new DVectorDense(axis == 0 ? cols() : rows());
        if (axis == 0) {
            for (int i = 0; i < cols(); i++) {
                variance.set(i, mapCol(i).variance());
            }
        } else {
            for (int i = 0; i < rows(); i++) {
                variance.inc(i, mapRow(i).variance());
            }
        }
        return variance;
    }

    @Override
    public DVector max(int axis) {
        DVector max = axis == 0 ? mapRowNew(0) : mapColNew(0);
        int i = axis == 0 ? 1 : 0;
        for (; i < rows(); i++) {
            int j = axis == 0 ? 0 : 1;
            for (; j < cols(); j++) {
                int index = axis == 0 ? j : i;
                if (max.get(index) < get(i, j)) {
                    max.set(index, get(i, j));
                }
            }
        }
        return max;
    }

    @Override
    public int[] argmax(int axis) {
        if (axis == 0) {
            int[] max = new int[cols()];
            for (int i = 1; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    if (get(max[j], j) < get(i, j)) {
                        max[j] = i;
                    }
                }
            }
            return max;
        }
        int[] max = new int[rows()];
        for (int i = 0; i < rows(); i++) {
            for (int j = 1; j < cols(); j++) {
                if (get(i, max[i]) < get(i, j)) {
                    max[i] = j;
                }
            }
        }
        return max;
    }

    @Override
    public DVector min(int axis) {
        DVector min = axis == 0 ? mapRowNew(0) : mapColNew(0);
        int i = axis == 0 ? 1 : 0;
        for (; i < rows(); i++) {
            int j = axis == 0 ? 0 : 1;
            for (; j < cols(); j++) {
                int index = axis == 0 ? j : i;
                if (min.get(index) > get(i, j)) {
                    min.set(index, get(i, j));
                }
            }
        }
        return min;
    }

    @Override
    public int[] argmin(int axis) {
        if (axis == 0) {
            int[] min = new int[cols()];
            for (int i = 1; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    if (get(min[j], j) > get(i, j)) {
                        min[j] = i;
                    }
                }
            }
            return min;
        }
        int[] min = new int[rows()];
        for (int i = 0; i < rows(); i++) {
            for (int j = 1; j < cols(); j++) {
                if (get(i, min[i]) > get(i, j)) {
                    min[i] = j;
                }
            }
        }
        return min;
    }

    @Override
    public DMatrix apply(Double2DoubleFunction fun) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                set(i, j, fun.apply(get(i, j)));
            }
        }
        return this;
    }

    @Override
    public DMatrix applyTo(Double2DoubleFunction fun, DMatrix to) {
        for (int i = 0; i < to.rows(); i++) {
            for (int j = 0; j < to.cols(); j++) {
                to.set(i, j, fun.apply(get(i, j)));
            }
        }
        return to;
    }

    @Override
    public DMatrix t() {
        DMatrix t = DMatrix.empty(cols(), rows());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                t.set(j, i, get(i, j));
            }
        }
        return t;
    }

    @Override
    public DMatrix tTo(DMatrix to) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                to.set(j, i, get(i, j));
            }
        }
        return to;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.range(0, rows() * cols())
                .mapToDouble(i -> get(Math.floorDiv(i, cols()), Math.floorMod(i, cols())));
    }

    @Override
    public DMatrix copy() {
        DMatrix copy = DMatrix.empty(rows(), cols());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                copy.set(i, j, get(i, j));
            }
        }
        return copy;
    }

    @Override
    public DMatrix resizeCopy(int rows, int cols, double fill) {
        DMatrix copy = DMatrix.empty(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i < rows() && j < cols()) {
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
        if (rows() != m.rows()) {
            return false;
        }
        if (cols() != m.cols()) {
            return false;
        }
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
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
        sb.append("rowCount:").append(rows()).append(", colCount:").append(cols()).append(", values:\n");
        sb.append("[\n");

        int minCols = 10;
        int minRows = 24;
        int ttRows = Math.min(minRows, rows());
        int ttCols = Math.min(minCols, cols());
        int extraRow = rows() > minRows ? 1 : 0;
        int extraCol = cols() > minCols ? 1 : 0;
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

        DecimalFormat floatFormat = printer.getOptions().bind(options).floatFormat();
        int headRows = 20;
        int headCols = 20;
        int tailRows = 2;
        int tailCols = 2;

        boolean fullRows = headRows + tailRows >= rows();
        boolean fullCols = headCols + tailCols >= cols();

        if (fullRows && fullCols) {
            return toFullContent(printer, options);
        }

        int[] rows = new int[Math.min(headRows + tailRows + 1, rows())];

        if (fullRows) {
            for (int i = 0; i < rows(); i++) {
                rows[i] = i;
            }
        } else {
            for (int i = 0; i < headRows; i++) {
                rows[i] = i;
            }
            rows[headRows] = -1;
            for (int i = 0; i < tailRows; i++) {
                rows[i + headRows + 1] = i + rows() - tailRows;
            }
        }
        int[] cols = new int[Math.min(headCols + tailCols + 1, cols())];
        if (fullCols) {
            for (int i = 0; i < cols(); i++) {
                cols[i] = i;
            }
        } else {
            for (int i = 0; i < headCols; i++) {
                cols[i] = i;
            }
            cols[headCols] = -1;
            for (int i = 0; i < tailCols; i++) {
                cols[i + headCols + 1] = i + cols() - tailCols;
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
                    tt.floatString(i + 1, j + 1, floatFormat.format(get(rows[i], cols[j])));
                }
            }
        }
        return tt.getDynamicText(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {

        DecimalFormat floatFormat = printer.getOptions().bind(options).floatFormat();
        TextTable tt = TextTable.empty(rows() + 1, cols() + 1, 1, 1);
        for (int i = 0; i < rows(); i++) {
            tt.intRow(i + 1, 0, i);
        }
        for (int i = 0; i < cols(); i++) {
            tt.intRow(0, i + 1, i);
        }
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                tt.floatString(i + 1, j + 1, floatFormat.format(get(i, j)));
            }
        }
        return tt.getDynamicText(printer, options);
    }
}
