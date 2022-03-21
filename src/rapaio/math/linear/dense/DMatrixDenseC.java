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

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
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

public class DMatrixDenseC extends AbstractDMatrix implements DMatrixStore {

    public static DMatrixDenseC empty(int rows, int cols) {
        return new DMatrixDenseC(rows, cols);
    }

    public static DMatrixDenseC identity(int n) {
        DMatrixDenseC m = new DMatrixDenseC(n, n);
        for (int i = 0; i < n; i++) {
            m.array[i * n + i] = 1;
        }
        return m;
    }

    public static DMatrixDenseC diagonal(DVector v) {
        int n = v.size();
        DMatrixDenseC m = new DMatrixDenseC(n, n);
        for (int i = 0; i < n; i++) {
            m.array[i * n + i] = v.get(i);
        }
        return m;
    }

    public static DMatrixDenseC fill(int rows, int cols, double fill) {
        return new DMatrixDenseC(0, rows, cols, DoubleArrays.newFill(rows * cols, fill));
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
        return new DMatrixDenseC(0, rows, cols, values);
    }

    private static final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
    private static final int speciesLen = species.length();
    private static final int SLICE_SIZE = 512;

    private final int offset;
    private final int rows;
    private final int cols;
    private final double[] array;

    private final int loopBoundRow;
    private final int loopBoundCol;
    private final VectorMask<Double> loopMaskRow;
    private final VectorMask<Double> loopMaskCol;
    private final int[] rowVectorIndexes;

    public DMatrixDenseC(int rows, int cols) {
        this(0, rows, cols, new double[rows * cols]);
    }

    public DMatrixDenseC(int offset, int rows, int cols, double[] array) {
        this.offset = offset;
        this.rows = rows;
        this.cols = cols;
        this.array = array;
        this.loopBoundRow = species.loopBound(cols);
        this.loopMaskRow = species.indexInRange(loopBoundRow, cols);
        this.loopBoundCol = species.loopBound(rows);
        this.loopMaskCol = species.indexInRange(loopBoundCol, rows);
        this.rowVectorIndexes = new int[speciesLen];
        for (int i = 0; i < speciesLen; i++) {
            rowVectorIndexes[i] = i * cols;
        }
    }

    @Override
    public double[] solidArrayCopy() {
        return Arrays.copyOfRange(array, offset, offset + rows * cols);
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int colCount() {
        return cols;
    }

    @Override
    public double get(int row, int col) {
        return array[offset + col * rows + row];
    }

    @Override
    public void set(int row, int col, double value) {
        array[offset + col * rows + row] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        array[offset + col * rows + row] += value;
    }

    @Override
    public VectorSpecies<Double> species() {
        return species;
    }

    @Override
    public int speciesLen() {
        return speciesLen;
    }

    @Override
    public int loopBoundRow() {
        return loopBoundRow;
    }

    @Override
    public VectorMask<Double> loopMaskRow() {
        return loopMaskRow;
    }

    @Override
    public DoubleVector loadVectorRow(int row, int i) {
        return DoubleVector.fromArray(species, array, offset + i * rows + row, rowVectorIndexes, 0);
    }

    @Override
    public DoubleVector loadVectorRow(int row, int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(species, array, offset + i * rows + row, rowVectorIndexes, 0, m);
    }

    @Override
    public void storeVectorRow(DoubleVector vector, int row, int i) {
        vector.intoArray(array, offset + i * rows + row, rowVectorIndexes, 0);
    }

    @Override
    public void storeVectorRow(DoubleVector vector, int row, int i, VectorMask<Double> m) {
        vector.intoArray(array, offset + i * rows + row, rowVectorIndexes, 0, m);
    }

    @Override
    public int loopBoundCol() {
        return loopBoundCol;
    }

    @Override
    public VectorMask<Double> loopMaskCol() {
        return loopMaskCol;
    }

    @Override
    public DoubleVector loadVectorCol(int col, int i) {
        return DoubleVector.fromArray(species, array, offset + col * rows + i);
    }

    @Override
    public DoubleVector loadVectorCol(int col, int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(species, array, offset + col * rows + i, m);
    }

    @Override
    public void storeVectorCol(DoubleVector vector, int col, int i) {
        vector.intoArray(array, offset + col * rows + i);
    }

    @Override
    public void storeVectorCol(DoubleVector vector, int col, int i, VectorMask<Double> m) {
        vector.intoArray(array, offset + col * rows + i, m);
    }

    @Override
    public DVector mapRow(int row) {
        return new DVectorStride(offset + row, rows, cols, array);
    }

    @Override
    public DVector mapRowTo(int row, DVector to) {
        for (int i = 0; i < cols; i++) {
            to.set(i, array[offset + row + i * rows]);
        }
        return to;
    }

    @Override
    public DVector mapCol(int col) {
        return new DVectorDense(offset + col * rows, rows, array);
    }

    @Override
    public DVector mapColTo(int col, DVector to) {
        int i = 0;
        if (to instanceof DVectorDense tos) {
            int bound = species.loopBound(rows);
            for (; i < bound; i += speciesLen) {
                var va = loadVectorCol(col, i);
                tos.storeVector(va, i);
            }
        }
        for (; i < rows; i++) {
            to.set(i, get(i, col));
        }
        return to;
    }

    @Override
    public DVector dot(DVector b) {
        if (cols != b.size()) {
            throw new IllegalArgumentException(
                    "Matrix (%d x %d) and vector ( %d ) are not conform for multiplication.".formatted(
                            rows, cols, b.size()));
        }
        int slices = cols / SLICE_SIZE;
        DVectorDense[] cslices = new DVectorDense[slices + 1];
        var stream = IntStream.range(0, slices + 1).unordered();
        if (slices > 0) {
            stream = stream.parallel();
        }
        stream.forEach(s -> {
            DVectorDense slice = new DVectorDense(0, rows, new double[rows]);
            for (int j = s * SLICE_SIZE; j < Math.min(cols, (s + 1) * SLICE_SIZE); j++) {
                slice.addMul(b.get(j), mapCol(j));
            }
            cslices[s] = slice;
        });
        DVectorDense c = cslices[0];
        for (int i = 1; i < cslices.length; i++) {
            c.add(cslices[i]);
        }
        return c;
    }

    @Override
    public DMatrixDenseR t(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] ref = solidArrayCopy();
            return new DMatrixDenseR(0, cols, rows, ref);
        }
        return new DMatrixDenseR(offset, cols, rows, array);
    }

    @Override
    public DMatrixDenseC apply(Double2DoubleFunction fun, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            var copy = new DMatrixDenseC(0, rows, cols, solidArrayCopy());
            copy.apply(fun);
            return copy;
        }
        apply(fun);
        return this;
    }

    @Override
    public void apply(Double2DoubleFunction fun) {
        int len = offset + rows * cols;
        for (int i = offset; i < len; i++) {
            array[i] = fun.apply(array[i]);
        }
    }

    @Override
    public DMatrix copy() {
        return new DMatrixDenseC(0, rows, cols, solidArrayCopy());
    }
}
