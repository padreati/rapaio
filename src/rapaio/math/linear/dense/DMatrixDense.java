/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.math.linear.dense;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.SOrder;
import rapaio.math.linear.base.AbstractDMatrix;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.IntInt2DoubleBiFunction;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * A dense matrix is a matrix stored as a contiguous array in row major or
 * column major order.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/2/21.
 */
public abstract class DMatrixDense extends AbstractDMatrix {

    public static DMatrixDense empty(int rows, int cols) {
        return empty(SOrder.C, rows, cols);
    }

    public static DMatrixDense empty(SOrder order, int rows, int cols) {
        return order.isColMajor() ? new DMatrixDenseC(rows, cols) : new DMatrixDenseR(rows, cols);
    }

    public static DMatrixDense identity(SOrder order, int n) {
        DMatrixDense m = empty(order, n, n);
        for (int i = 0; i < n * n; i += n + 1) {
            m.array[i] = 1;
        }
        return m;
    }

    public static DMatrixDense fill(SOrder order, int rows, int cols, double fill) {
        DMatrixDense m = empty(order, rows, cols);
        if (fill != 0) {
            Arrays.fill(m.array, 0, m.len, fill);
        }
        return m;
    }

    public static DMatrixDense fill(SOrder order, int rows, int cols, IntInt2DoubleBiFunction fun) {
        DMatrixDense m = empty(order, rows, cols);
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                m.set(i, j, fun.applyIntIntAsDouble(i, j));
            }
        }
        return m;
    }

    public static DMatrixDense random(SOrder order, int rows, int cols) {
        DMatrixDense m = empty(order, rows, cols);
        for (int i = 0; i < m.array.length; i++) {
            m.array[i] = RandomSource.nextDouble();
        }
        return m;
    }

    public static DMatrixDense copy(SOrder order, boolean byRows, double[][] values) {
        int rows = byRows ? values.length : values[0].length;
        int cols = byRows ? values[0].length : values.length;
        DMatrixDense m = empty(order, rows, cols);

        if (byRows) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[i][j]);
                }
            }
        } else {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[j][i]);
                }
            }
        }
        return m;
    }

    public static DMatrixDense copy(SOrder order, boolean byRows, double[][] values, int rowStart, int rowEnd, int colStart, int colEnd) {
        int rows = rowEnd - rowStart;
        int cols = colEnd - colStart;
        DMatrixDense m = empty(order, rows, cols);

        if (byRows) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[i + rowStart][j + colStart]);
                }
            }
        } else {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[j + colStart][i + rowStart]);
                }
            }
        }
        return m;
    }

    public static DMatrixDense copy(boolean byRow, int rows, int cols, double... values) {
        return copy(SOrder.C, byRow, rows, cols, values);
    }

    public static DMatrixDense copy(SOrder order, boolean byRow, int rows, int cols, double... values) {
        double[] copy = DoubleArrays.copy(values, 0, values.length);
        if (byRow == order.isRowMajor()) {
            return wrap(order, rows, cols, copy);
        } else {
            DMatrixDense m = empty(order, rows, cols);
            int pos = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[pos++]);
                }
            }
            return m;
        }
    }

    public static DMatrixDense copy(Frame df) {
        return copy(SOrder.C, df);
    }

    public static DMatrixDense copy(SOrder order, Frame df) {
        int rows = df.rowCount();
        int cols = df.varCount();
        DMatrixDense m = empty(order, rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.set(i, j, df.getDouble(i, j));
            }
        }
        return m;
    }

    public static DMatrixDense copy(Var... vars) {
        return copy(SOrder.C, vars);
    }

    public static DMatrixDense copy(SOrder order, Var... vars) {
        int rows = vars[0].size();
        int cols = vars.length;
        DMatrixDense m = empty(order, rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.set(i, j, vars[j].getDouble(i));
            }
        }
        return m;
    }

    public static DMatrixDense wrap(int rows, int cols, double[] array) {
        return new DMatrixDenseC(rows, cols, array);
    }

    public static DMatrixDense wrap(SOrder order, int rows, int cols, double[] array) {
        return order.isRowMajor() ? new DMatrixDenseR(rows, cols, array) : new DMatrixDenseC(rows, cols, array);
    }

    private static final long serialVersionUID = 4455940496310789794L;
    protected final SOrder order;
    protected final int rows;
    protected final int cols;
    protected final double[] array;

    protected final int len;

    protected DMatrixDense(SOrder order, int rows, int cols) {
        this(order, rows, cols, DoubleArrays.newFill(rows * cols, 0));
    }

    protected DMatrixDense(SOrder order, int rows, int cols, double[] array) {
        this.order = order;
        this.rows = rows;
        this.cols = cols;
        this.array = array;
        this.len = rows * cols;
        validate();
    }

    private void validate() {
        if (rows <= 0) {
            throw new IllegalArgumentException("Number of rows must be a finite positive number.");
        }
        if (cols <= 0) {
            throw new IllegalArgumentException("Number of columns must be a finite positive number.");
        }
        if (array.length < len) {
            throw new IllegalArgumentException("The array of elements is smaller than rows multiplied by columns.");
        }
    }

    @Override
    public SOrder order() {
        return order;
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int colCount() {
        return cols;
    }

    /**
     * Apply the given function to all elements of the matrix.
     *
     * @param fun function to be applied
     * @return same instance matrix
     */
    @Override
    public DMatrix apply(Double2DoubleFunction fun) {
        for (int i = 0; i < len; i++) {
            array[i] = fun.apply(array[i]);
        }
        return this;
    }

    /**
     * Stream of double values, the element order is not guaranteed,
     * it depends on the implementation.
     *
     * @return double value stream
     */
    @Override
    public DoubleStream valueStream() {
        return DoubleStream.of(array).limit(len);
    }

    @Override
    public DMatrix copy() {
        double[] copy = DoubleArrays.copy(array, 0, array.length);
        return wrap(order, rows, cols, copy);
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/28/21.
     */
    private static class DMatrixDenseC extends DMatrixDense {

        private static final long serialVersionUID = 6165595084302179897L;

        protected DMatrixDenseC(int rows, int cols) {
            super(SOrder.C, rows, cols);
        }

        protected DMatrixDenseC(int rows, int cols, double[] array) {
            super(SOrder.C, rows, cols, array);
        }

        /**
         * Getter for value found at given row and column index.
         *
         * @param row row index
         * @param col column index
         * @return value at given row index and column index
         */
        @Override
        public double get(int row, int col) {
            return array[col * rows + row];
        }

        /**
         * Sets value at the given row and column indexes
         *
         * @param row   row index
         * @param col   column index
         * @param value value to be set
         */
        @Override
        public void set(int row, int col, double value) {
            array[col * rows + row] = value;
        }

        /**
         * Increment the value at given position.
         *
         * @param row   row index
         * @param col   column index
         * @param value value to be added
         */
        @Override
        public void inc(int row, int col, double value) {
            array[col * rows + row] += value;
        }

        /**
         * Creates an instance of a transposed matrix. Depending on implementation
         * this can be a view of the original data.
         *
         * @return new transposed matrix
         */
        @Override
        public DMatrixDense t() {
            return new DMatrixDenseR(cols, rows, array);
        }
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/28/21.
     */
    private static class DMatrixDenseR extends DMatrixDense {
        private static final long serialVersionUID = -2797681700466963314L;

        protected DMatrixDenseR(int rows, int cols) {
            super(SOrder.R, rows, cols);
        }

        protected DMatrixDenseR(int rows, int cols, double[] array) {
            super(SOrder.R, rows, cols, array);
        }

        /**
         * Getter for value found at given row and column index.
         *
         * @param row row index
         * @param col column index
         * @return value at given row index and column index
         */
        @Override
        public double get(int row, int col) {
            return array[row * cols + col];
        }

        /**
         * Sets value at the given row and column indexes
         *
         * @param row   row index
         * @param col   column index
         * @param value value to be set
         */
        @Override
        public void set(int row, int col, double value) {
            array[row * cols + col] = value;
        }

        /**
         * Increment the value at given position.
         *
         * @param row   row index
         * @param col   column index
         * @param value value to be added
         */
        @Override
        public void inc(int row, int col, double value) {
            array[row * cols + col] += value;
        }

        /**
         * Creates an instance of a transposed matrix. Depending on implementation
         * this can be a view of the original data.
         *
         * @return new transposed matrix
         */
        @Override
        public DMatrixDense t() {
            return new DMatrixDenseC(cols, rows, array);
        }
    }
}
