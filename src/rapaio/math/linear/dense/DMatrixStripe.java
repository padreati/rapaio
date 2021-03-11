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

import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.SOrder;
import rapaio.math.linear.base.AbstractDMatrix;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.IntInt2DoubleBiFunction;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A stripe matrix is a matrix which stores the value as arrays of arrays.
 * Depending on the storage order, it can store arrays of rows or arrays of columns.
 * <p>
 * This memory layout is also known as Liffe lists and it is the standard way of
 * working with bi-dimensional arrays in java.
 * <p>
 * In general, when a stripe matrix is created it will be present a storing order
 * parameter of the type {@code SOrder}. If this parameter is not present, it will
 * be assumed {@code SOrder.R} value or storage in row major order (arrays of rows).
 * This is to meet the default usage of a {@code double[][]} array.
 * <p>
 * Depending on the operation which will use the matrix, the user will chose the
 * appropriate type of matrix storage. Whenever possible, vector and matrix operations
 * will be implemented using parallelization and various cache friendly co-locations.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/2/21.
 */
public abstract class DMatrixStripe extends AbstractDMatrix {

    /**
     * Builds a matrix with n rows and m columns filled with zero of row major storage order.
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return a new instance of the matrix object
     */
    public static DMatrixStripe empty(int rowCount, int colCount) {
        return empty(SOrder.R, rowCount, colCount);
    }

    /**
     * Builds a matrix with n rows and m columns filled with zero of appropriate storage order
     *
     * @param order    {@link SOrder#R} for row major or {@link SOrder#C} for col major storage order
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return a new instance of the matrix object
     */
    public static DMatrixStripe empty(SOrder order, int rowCount, int colCount) {
        if (order.isRowMajor()) {
            return new DMatrixStripeR(rowCount, colCount);
        } else {
            return new DMatrixStripeC(rowCount, colCount);
        }
    }

    /**
     * Builds an identity matrix with n rows and n columns.
     * An identity matrix is a matrix with 1 on the main diagonal
     * and 0 otherwise.
     *
     * @param n number of rows and also number of columns
     * @return a new instance of identity matrix of order n
     */
    public static DMatrixStripe identity(int n) {
        return identity(SOrder.R, n);
    }

    /**
     * Builds an identity matrix with n rows and n columns.
     * An identity matrix is a matrix with 1 on the main diagonal
     * and 0 otherwise.
     *
     * @param n     number of rows and also number of columns
     * @param order {@link SOrder#R} for row major or {@link SOrder#C} for col major storage order
     * @return a new instance of identity matrix of order n
     */
    public static DMatrixStripe identity(SOrder order, int n) {
        DMatrixStripe m = empty(order, n, n);
        for (int i = 0; i < n; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    public static DMatrixStripe random(int rowCount, int colCount) {
        return random(SOrder.R, rowCount, colCount);
    }

    public static DMatrixStripe random(SOrder order, int rowCount, int colCount) {
        Normal normal = Normal.std();
        return DMatrixStripe.fill(order, rowCount, colCount, (r, c) -> normal.sampleNext());
    }

    /**
     * Builds a new matrix filled with a given value.
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     value which fills all cells of the matrix
     * @return new matrix filled with value
     */
    public static DMatrixStripe fill(int rowCount, int colCount, double fill) {
        return fill(SOrder.R, rowCount, colCount, fill);
    }

    /**
     * Builds a new matrix filled with a given value.
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     value which fills all cells of the matrix
     * @return new matrix filled with value
     */
    public static DMatrixStripe fill(SOrder order, int rowCount, int colCount, double fill) {
        DMatrixStripe ret = empty(order, rowCount, colCount);
        if (fill != 0.0) {
            for (double[] array : ret.values) {
                Arrays.fill(array, fill);
            }
        }
        return ret;
    }

    /**
     * Builds a new matrix filled with a given value
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fun      lambda function which computes a value given row and column positions
     * @return new matrix filled with value
     */
    public static DMatrixStripe fill(int rowCount, int colCount, IntInt2DoubleBiFunction fun) {
        return fill(SOrder.R, rowCount, colCount, fun);
    }

    /**
     * Builds a new matrix filled with a given value
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fun      lambda function which computes a value given row and column positions
     * @return new matrix filled with value
     */
    public static DMatrixStripe fill(SOrder order, int rowCount, int colCount, IntInt2DoubleBiFunction fun) {
        DMatrixStripe ret = empty(order, rowCount, colCount);
        for (int i = 0; i < ret.rowCount(); i++) {
            for (int j = 0; j < ret.colCount(); j++) {
                ret.set(i, j, fun.applyIntIntAsDouble(i, j));
            }
        }
        return ret;
    }

    public static DMatrixStripe wrap(double[][] source) {
        return wrap(SOrder.R, source);
    }

    public static DMatrixStripe wrap(SOrder order, double[][] source) {
        if (order.isRowMajor()) {
            return new DMatrixStripeR(source.length, source[0].length, source);
        } else {
            return new DMatrixStripeC(source[0].length, source.length, source);
        }
    }

    public static DMatrixStripe copy(int rows, int cols, double... values) {
        return copy(SOrder.R, rows, cols, values);
    }

    public static DMatrixStripe copy(SOrder order, int rows, int cols, double... values) {
        DMatrixStripe m = empty(order, rows, cols);
        if (order.isRowMajor()) {
            int pos = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[pos++]);
                }
            }
        } else {
            int pos = 0;
            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < rows; i++) {
                    m.set(i, j, values[pos++]);
                }
            }
        }
        return m;
    }

    public static DMatrixStripe copy(double[][] source) {
        return copy(SOrder.R, source);
    }

    public static DMatrixStripe copy(SOrder order, double[][] source) {
        int rowCount = source.length;
        int colCount = source[0].length;
        DMatrixStripe m = empty(order, rowCount, colCount);
        if (order.isRowMajor()) {
            for (int i = 0; i < rowCount; i++) {
                System.arraycopy(source[i], 0, m.values[i], 0, colCount);
            }
        } else {
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < colCount; j++) {
                    m.set(i, j, source[i][j]);
                }
            }
        }
        return m;
    }

    public static DMatrix copy(double[][] source, int rowStart, int rowEnd, int colStart, int colEnd) {
        return copy(SOrder.R, source, rowStart, rowEnd, colStart, colEnd);
    }

    public static DMatrix copy(SOrder order, double[][] source, int rowStart, int rowEnd, int colStart, int colEnd) {
        DMatrix mm = empty(order, rowEnd - rowStart, colEnd - colStart);
        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                mm.set(i - rowStart, j - colStart, source[i][j]);
            }
        }
        return mm;
    }

    public static DMatrixStripe copy(Frame df) {
        return copy(SOrder.R, df);
    }

    public static DMatrixStripe copy(SOrder order, Frame df) {
        DMatrixStripe m = empty(order, df.rowCount(), df.varCount());
        if (order.isRowMajor()) {
            for (int j = 0; j < df.varCount(); j++) {
                for (int i = 0; i < df.rowCount(); i++) {
                    m.set(i, j, df.getDouble(i, j));
                }
            }
        } else {
            for (int i = 0; i < df.varCount(); i++) {
                Var v = df.rvar(i);
                if (v.type().equals(VType.DOUBLE)) {
                    System.arraycopy(((VarDouble) v).elements(), 0, m.values[i], 0, df.rowCount());
                } else {
                    for (int j = 0; j < df.rowCount(); j++) {
                        m.values[i][j] = v.getDouble(j);
                    }
                }
            }
        }
        return m;
    }

    public static DMatrixStripe copy(Var... vars) {
        return copy(SOrder.R, vars);
    }

    public static DMatrixStripe copy(SOrder order, Var... vars) {
        DMatrixStripe m = empty(order, vars[0].size(), vars.length);
        if (order.isRowMajor()) {
            for (int i = 0; i < vars[0].size(); i++) {
                for (int j = 0; j < vars.length; j++) {
                    m.set(i, j, vars[j].getDouble(i));
                }
            }
        } else {
            for (int i = 0; i < vars.length; i++) {
                Var v = vars[i];
                if (v.type().equals(VType.DOUBLE)) {
                    System.arraycopy(((VarDouble) v).elements(), 0, m.values[i], 0, v.size());
                } else {
                    for (int j = 0; j < v.size(); j++) {
                        m.values[i][j] = v.getDouble(j);
                    }
                }
            }
        }
        return m;
    }


    private static final long serialVersionUID = -1798941400862688438L;

    protected final int rowCount;
    protected final int colCount;
    protected final double[][] values;

    protected DMatrixStripe(int rowCount, int colCount, double[][] values) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = values;
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int colCount() {
        return colCount;
    }

    @Override
    public DMatrix apply(Double2DoubleFunction fun) {
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[i].length; j++) {
                values[i][j] = fun.apply(values[i][j]);
            }
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).flatMapToDouble(Arrays::stream);
    }

    /**
     * Dense matrix with values in double floating point precision.
     * Values are stored in arrays of arrays with first array holding column references
     * and secondary level arrays being the column arrays.
     */
    static class DMatrixStripeC extends DMatrixStripe {

        private static final long serialVersionUID = -2186520026933442642L;

        public DMatrixStripeC(int rows, int cols) {
            super(rows, cols, newArray(rows, cols));
        }

        private static double[][] newArray(int rowCount, int colCount) {
            double[][] array = new double[colCount][rowCount];
            for (int i = 0; i < colCount; i++) {
                array[i] = DoubleArrays.newFill(rowCount, 0);
            }
            return array;
        }

        public DMatrixStripeC(int rows, int cols, double[][] array) {
            super(rows, cols, array);
        }

        @Override
        public SOrder order() {
            return SOrder.C;
        }

        @Override
        public double get(int row, int col) {
            return values[col][row];
        }

        @Override
        public void set(int row, int col, double value) {
            values[col][row] = value;
        }

        @Override
        public void inc(int row, int col, double value) {
            values[col][row] += value;
        }

        @Override
        public DMatrix add(double x) {
            for (double[] col : values) {
                for (int i = 0; i < col.length; i++) {
                    col[i] += x;
                }
            }
            return this;
        }

        @Override
        public DVector dot(DVector b) {
            if (b instanceof DVectorDense) {
                DVectorDense vd = (DVectorDense) b;
                double[] array = vd.elements();

                double[] c = DoubleArrays.newFill(rowCount, 0);
                for (int i = 0; i < colCount; i++) {
                    double[] col = values[i];
                    addMultiplied(c, col, array[i]);
                }
                return DVectorDense.wrap(c);
            }
            return super.dot(b);
        }

        private void addMultiplied(double[] c, double[] b, double factor) {
            for (int i = 0; i < c.length; i++) {
                c[i] += b[i] * factor;
            }
        }

        @Override
        public DMatrix dotDiag(DVector v) {
            if (v instanceof DVectorDense) {
                var array = v.asDense().elements();
                var len = v.size();
                for (int i = 0; i < colCount; i++) {
                    DoubleArrays.mult(values[i], 0, array[i], rowCount);
                }
                return this;
            }
            return super.dotDiag(v);
        }

        @Override
        public DMatrix dotDiagT(DVector v) {
            if (v.isDense()) {
                var array = v.asDense().elements();
                var len = v.size();
                for (int i = 0; i < rowCount; i++) {
                    DoubleArrays.mult(values[i], 0, array[i], colCount);
                }
                return this;
            }
            return super.dotDiagT(v);
        }

        @Override
        public DMatrix t() {
            return new DMatrixStripeR(colCount, rowCount, values);
        }

        @Override
        public DMatrixStripeC copy() {
            DMatrixStripeC copy = new DMatrixStripeC(rowCount, colCount);
            for (int i = 0; i < colCount; i++) {
                System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
            }
            return copy;
        }
    }

    /**
     * Dense matrix with values in double floating point precision.
     * Values are stored in arrays of arrays with first array holding row references
     * and secondary level arrays being the row arrays.
     */
    static class DMatrixStripeR extends DMatrixStripe {

        private static final long serialVersionUID = -2186520026933442642L;

        public DMatrixStripeR(int rowCount, int colCount) {
            super(rowCount, colCount, newArray(rowCount, colCount));
        }

        private static double[][] newArray(int rowCount, int colCount) {
            double[][] array = new double[rowCount][colCount];
            for (int i = 0; i < rowCount; i++) {
                array[i] = DoubleArrays.newFill(colCount, 0);
            }
            return array;
        }

        public DMatrixStripeR(int rowCount, int colCount, double[][] values) {
            super(rowCount, colCount, values);
        }

        @Override
        public SOrder order() {
            return SOrder.R;
        }

        @Override
        public double get(int row, int col) {
            return values[row][col];
        }

        @Override
        public void set(int row, int col, double value) {
            values[row][col] = value;
        }

        @Override
        public void inc(int row, int col, double value) {
            values[row][col] += value;
        }

        @Override
        public DMatrix add(double x) {
            for (double[] row : values) {
                for (int i = 0; i < row.length; i++) {
                    row[i] += x;
                }
            }
            return this;
        }

        @Override
        public DVector dot(DVector b) {
            if (b instanceof DVectorDense) {
                DVectorDense vd = (DVectorDense) b;
                int vlen = vd.size();
                double[] varray = vd.elements();

                double[] c = DoubleArrays.newFill(rowCount, 0);
                IntStream.range(0, rowCount).parallel().forEach(i -> c[i] = prodSum(values[i], varray));
                return DVectorDense.wrap(c);
            }
            return super.dot(b);
        }

        private double prodSum(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                sum += a[i] * b[i];
            }
            return sum;
        }

        @Override
        public DMatrix dotDiag(DVector v) {
            if (v instanceof DVectorDense) {
                var array = v.asDense().elements();
                var len = v.size();
                for (int i = 0; i < rowCount; i++) {
                    DoubleArrays.mult(values[i], 0, array, 0, len);
                }
                return this;
            }
            return super.dotDiag(v);
        }

        @Override
        public DMatrix dotDiagT(DVector v) {
            if (v.isDense()) {
                var array = v.asDense().elements();
                var len = v.size();
                for (int i = 0; i < rowCount; i++) {
                    DoubleArrays.mult(values[i], 0, array[i], colCount);
                }
                return this;
            }
            return super.dotDiagT(v);
        }

        @Override
        public DMatrix t() {
            return new DMatrixStripeC(colCount, rowCount, values);
        }

        @Override
        public DMatrixStripeR copy() {
            DMatrixStripeR copy = new DMatrixStripeR(rowCount, colCount);
            for (int i = 0; i < rowCount; i++) {
                System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
            }
            return copy;
        }
    }
}
