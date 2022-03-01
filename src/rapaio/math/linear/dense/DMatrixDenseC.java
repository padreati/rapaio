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

import java.util.Arrays;
import java.util.stream.IntStream;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.MathTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.IntInt2DoubleBiFunction;

public class DMatrixDenseC extends DMatrixDense {

    public static DMatrixDenseC empty(int rows, int cols) {
        return new DMatrixDenseC(rows, cols);
    }

    public static DMatrixDenseC identity(int n) {
        DMatrixDenseC m = new DMatrixDenseC(n, n);
        for (int i = 0; i < n; i++) {
            m.values[i * n + i] = 1;
        }
        return m;
    }

    public static DMatrixDenseC diagonal(DVector v) {
        int n = v.size();
        DMatrixDenseC m = new DMatrixDenseC(n, n);
        for (int i = 0; i < n; i++) {
            m.values[i * n + i] = v.get(i);
        }
        return m;
    }

    public static DMatrixDenseC fill(int rows, int cols, double fill) {
        return new DMatrixDenseC(rows, cols, DoubleArrays.newFill(rows * cols, fill));
    }

    public static DMatrixDenseC fill(int rows, int cols, IntInt2DoubleBiFunction fun) {
        DMatrixDenseC m = empty(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.set(i, j, fun.applyIntIntAsDouble(i, j));
            }
        }
        return m;
    }

    public static DMatrixDenseC random(int rows, int cols) {
        Normal normal = Normal.std();
        return fill(rows, cols, (r, c) -> normal.sampleNext());
    }

    public static DMatrixDenseC random(int rows, int cols, Distribution distribution) {
        return fill(rows, cols, (r, c) -> distribution.sampleNext());
    }

    /**
     * Copy values from an array of arrays into a matrix. Matrix storage type is the default type and values
     * are row oriented.
     *
     * @param values array of arrays of values
     * @return matrix which hold a range of data
     */
    public static DMatrixDenseC copy(double[][] values) {
        return copy(0, values.length, 0, values[0].length, true, values);
    }

    /**
     * Copy values from an array of arrays into a matrix. Matrix storage type is the default type and values
     * are row or column oriented depending on the value of {@code byRows}.
     *
     * @param byRows true means row first orientation, otherwise column first orientation
     * @param values array of arrays of values
     * @return matrix which hold a range of data
     */
    public static DMatrixDenseC copy(boolean byRows, double[][] values) {
        return copy(0, values.length, 0, values[0].length, byRows, values);
    }

    /**
     * Copy values from an array of arrays into a matrix. Matrix storage type and row/column
     * orientation are given as parameter.
     * <p>
     * This is the most customizable way to transfer values from an array of arrays into a matrix.
     * It allows creating of a matrix from a rectangular range of values.
     *
     * @param byRows   if true values are row oriented, if false values are column oriented
     * @param rowStart starting row inclusive
     * @param rowEnd   end row exclusive
     * @param colStart column row inclusive
     * @param colEnd   column end exclusive
     * @param values   array of arrays of values
     * @return matrix which hold a range of data
     */
    public static DMatrixDenseC copy(int rowStart, int rowEnd, int colStart, int colEnd, boolean byRows, double[][] values) {
        int rows = rowEnd - rowStart;
        int cols = colEnd - colStart;
        DMatrixDenseC m = empty(rows, cols);
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

    /**
     * Copies values from an array into a matrix with column orientation.
     * <p>
     * The layout of data is described by {@code inputRows} and {@code columnRows} and this is the same size
     * for the resulted matrix.
     *
     * @param inputRows number of rows for data layout
     * @param inputCols number of columns for data layout
     * @param values    array of values
     * @return matrix with a range of values copied from original array
     */
    public static DMatrixDenseC copy(int inputRows, int inputCols, double... values) {
        return copy(inputRows, inputCols, 0, inputRows, 0, inputCols, true, values);
    }

    /**
     * Copies values from an array into a matrix.
     * <p>
     * <p>
     * The layout of data is described by {@code inputRows} and {@code columnRows}.
     * The row or column orientation is determined by {@code byRows} parameter. If {@code byRows} is true,
     * the values from the array are interpreted as containing rows one after another. If {@code byRows} is
     * false then the interpretation is that the array contains columns one after another.
     * <p>
     * The method creates an array of values of the same size as input data layout.
     *
     * @param byRows    value orientation: true if row oriented, false if column oriented
     * @param inputRows number of rows for data layout
     * @param inputCols number of columns for data layout
     * @param values    array of values
     * @return matrix with a range of values copied from original array
     */
    public static DMatrixDenseC copy(int inputRows, int inputCols, boolean byRows, double... values) {
        return copy(inputRows, inputCols, 0, inputRows, 0, inputCols, byRows, values);
    }

    /**
     * Copies values from a flat array into a matrix.
     * <p>
     * This is the most customizable way to copy values from a contiguous arrays into a matrix.
     * <p>
     * The layout of data from the flat array is described by {@code inputRows} and {@code columnRows}.
     * The row or column orientation is determined by {@code byRows} parameter. If {@code byRows} is true,
     * the values from the array are interpreted as containing rows one after another. If {@code byRows} is
     * false then the interpretation is that the array contains columns one after another.
     * <p>
     * The method allows creation of an array using a contiguous range of rows and columns described by
     * parameters.
     *
     * @param byRows    value orientation: true if row oriented, false if column oriented
     * @param inputRows number of rows for data layout
     * @param inputCols number of columns for data layout
     * @param rowStart  row start inclusive
     * @param rowEnd    row end exclusive
     * @param colStart  column start inclusive
     * @param colEnd    column end exclusive
     * @param values    array of values
     * @return matrix with a range of values copied from original array
     */
    public static DMatrixDenseC copy(int inputRows, int inputCols, int rowStart, int rowEnd, int colStart, int colEnd, boolean byRows,
            double... values) {
        int rows = rowEnd - rowStart;
        int cols = colEnd - colStart;
        DMatrixDenseC m = empty(rows, cols);

        if (byRows) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[inputCols * (Math.max(0, rowStart - 1) + i) + colStart + j]);
                }
            }
        } else {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    m.set(i, j, values[inputRows * (Math.max(0, colStart - 1) + j) + rowStart + i]);
                }
            }
        }
        return m;
    }

    /**
     * Copies data from a data frame into a matrix.
     * Data is collected from frame using {@link Frame#getDouble(int, int)} calls.
     *
     * @param df data frame
     * @return matrix with collected values
     */
    public static DMatrixDenseC copy(Frame df) {
        Var[] vars = df.varStream().toArray(Var[]::new);
        return copy(vars);
    }

    /**
     * Copies data from a list of variables using the specified data storage frame.
     * Data is collected from frame using {@link Frame#getDouble(int, int)} calls.
     *
     * @param vars array of variables
     * @return matrix with collected values
     */
    public static DMatrixDenseC copy(Var... vars) {
        int rows = vars[0].size();
        int cols = vars.length;
        DMatrixDenseC m = empty(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.set(i, j, vars[j].getDouble(i));
            }
        }
        return m;
    }

    public static DMatrixDenseC copy(boolean byRows, DVector... vectors) {
        int minSize = Integer.MAX_VALUE;
        for (DVector vector : vectors) {
            minSize = MathTools.min(vector.size(), minSize);
        }
        if (minSize == 0 || minSize == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Minimum length of a vector is 0 which is invalid.");
        }
        DMatrixDenseC copy;
        if (byRows) {
            copy = DMatrixDenseC.empty(vectors.length, minSize);
            for (int i = 0; i < vectors.length; i++) {
                for (int j = 0; j < minSize; j++) {
                    copy.set(i, j, vectors[i].get(j));
                }
            }
        } else {
            copy = DMatrixDenseC.empty(minSize, vectors.length);
            for (int i = 0; i < minSize; i++) {
                for (int j = 0; j < vectors.length; j++) {
                    copy.set(i, j, vectors[j].get(i));
                }
            }
        }
        return copy;
    }

    public static DMatrixDenseC wrap(int rows, int cols, double... values) {
        return new DMatrixDenseC(rows, cols, values);
    }

    private static final int SLICE_SIZE = 1024;

    public DMatrixDenseC(int rows, int cols) {
        this(rows, cols, new double[rows * cols]);
    }

    public DMatrixDenseC(int rows, int cols, double[] values) {
        super(rows, cols, values);
    }

    @Override
    public double get(int row, int col) {
        return values[col * rowCount + row];
    }

    @Override
    public void set(int row, int col, double value) {
        values[col * rowCount + row] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        values[col * rowCount + row] += value;
    }

    @Override
    public DVector mapCol(int col, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.copy(values, col * rowCount, rowCount);
            return DVector.wrap(copy);
        }
        return new DVectorDense(col * rowCount, rowCount, values);
    }

    @Override
    public DVector dot(DVector b) {
        if (colCount != b.size()) {
            throw new IllegalArgumentException(
                    String.format("Matrix (%d x %d) and vector ( %d ) are not conform for multiplication.",
                            rowCount, colCount, b.size()));
        }

        int slices = colCount / SLICE_SIZE;
        double[][] cslices = new double[slices + 1][];
        IntStream stream = IntStream.range(0, slices + 1);
        if (slices > 1) {
            stream = stream.parallel();
        }
        stream.forEach(s -> {
            double[] slice = new double[rowCount];
            for (int j = s * SLICE_SIZE; j < Math.min(colCount, (s + 1) * SLICE_SIZE); j++) {
                DoubleArrays.addMul(slice, 0, b.get(j), values, j * rowCount, rowCount);
            }
            cslices[s] = slice;
        });

        double[] c = new double[rowCount];
        for (var s : cslices) {
            DoubleArrays.add(c, 0, s, 0, rowCount);
        }
        return new DVectorDense(0, c.length, c);
    }

    @Override
    public DMatrixDenseR t(AlgebraOption<?>... opts) {
        double[] ref = values;
        if (AlgebraOptions.from(opts).isCopy()) {
            ref = DoubleArrays.copy(values, 0, rowCount * colCount);
        }
        return new DMatrixDenseR(colCount, rowCount, ref);
    }

    @Override
    public DMatrixDenseC apply(Double2DoubleFunction fun, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[rowCount * colCount];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = fun.apply(values[i]);
            }
            return new DMatrixDenseC(rowCount, colCount, copy);
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = fun.apply(values[i]);
        }
        return this;
    }

    @Override
    public DMatrix copy() {
        return new DMatrixDenseC(rowCount, colCount, Arrays.copyOf(values, values.length));
    }
}
