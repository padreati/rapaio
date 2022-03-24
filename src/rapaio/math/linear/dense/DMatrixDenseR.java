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
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.IntInt2DoubleBiFunction;

public class DMatrixDenseR extends AbstractDMatrix implements DMatrixStore {

    public static DMatrixDenseR empty(int rows, int cols) {
        return new DMatrixDenseR(rows, cols);
    }

    public static DMatrixDenseR identity(int n) {
        DMatrixDenseR m = new DMatrixDenseR(n, n);
        for (int i = 0; i < n; i++) {
            m.array[i * n + i] = 1;
        }
        return m;
    }

    public static DMatrixDenseR diagonal(DVector v) {
        int n = v.size();
        DMatrixDenseR m = new DMatrixDenseR(n, n);
        for (int i = 0; i < n; i++) {
            m.array[i * n + i] = v.get(i);
        }
        return m;
    }

    public static DMatrixDenseR fill(int rows, int cols, double fill) {
        return new DMatrixDenseR(0, rows, cols, DoubleArrays.newFill(rows * cols, fill));
    }

    public static DMatrixDenseR fill(int rows, int cols, IntInt2DoubleBiFunction fun) {
        DMatrixDenseR m = empty(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.set(i, j, fun.applyIntIntAsDouble(i, j));
            }
        }
        return m;
    }

    public static DMatrixDenseR random(int rows, int cols) {
        Normal normal = Normal.std();
        return fill(rows, cols, (r, c) -> normal.sampleNext());
    }

    public static DMatrixDenseR random(int rows, int cols, Distribution distribution) {
        return fill(rows, cols, (r, c) -> distribution.sampleNext());
    }

    /**
     * Copy values from an array of arrays into a matrix. Matrix storage type is the default type and values
     * are row oriented.
     *
     * @param values array of arrays of values
     * @return matrix which hold a range of data
     */
    public static DMatrixDenseR copy(double[][] values) {
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
    public static DMatrixDenseR copy(boolean byRows, double[][] values) {
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
    public static DMatrixDenseR copy(int rowStart, int rowEnd, int colStart, int colEnd, boolean byRows, double[][] values) {
        int rows = rowEnd - rowStart;
        int cols = colEnd - colStart;
        DMatrixDenseR m = empty(rows, cols);
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
    public static DMatrixDenseR copy(int inputRows, int inputCols, double... values) {
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
    public static DMatrixDenseR copy(int inputRows, int inputCols, boolean byRows, double... values) {
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
    public static DMatrixDenseR copy(int inputRows, int inputCols, int rowStart, int rowEnd, int colStart, int colEnd, boolean byRows,
            double... values) {
        int rows = rowEnd - rowStart;
        int cols = colEnd - colStart;
        DMatrixDenseR m = empty(rows, cols);

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
    public static DMatrixDenseR copy(Frame df) {
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
    public static DMatrixDenseR copy(Var... vars) {
        int rows = vars[0].size();
        int cols = vars.length;
        DMatrixDenseR m = empty(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.set(i, j, vars[j].getDouble(i));
            }
        }
        return m;
    }

    public static DMatrixDenseR copy(boolean byRows, DVector... vectors) {
        int minSize = Integer.MAX_VALUE;
        for (DVector vector : vectors) {
            minSize = MathTools.min(vector.size(), minSize);
        }
        if (minSize == 0 || minSize == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Minimum length of a vector is 0 which is invalid.");
        }
        DMatrixDenseR copy;
        if (byRows) {
            copy = DMatrixDenseR.empty(vectors.length, minSize);
            for (int i = 0; i < vectors.length; i++) {
                for (int j = 0; j < minSize; j++) {
                    copy.set(i, j, vectors[i].get(j));
                }
            }
        } else {
            copy = DMatrixDenseR.empty(minSize, vectors.length);
            for (int i = 0; i < minSize; i++) {
                for (int j = 0; j < vectors.length; j++) {
                    copy.set(i, j, vectors[j].get(i));
                }
            }
        }
        return copy;
    }

    public static DMatrixDenseR wrap(int rows, int cols, double... values) {
        return new DMatrixDenseR(0, rows, cols, values);
    }

    private static final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
    private static final int speciesLen = species.length();
    private final int offset;
    private final int rows;
    private final int cols;
    private final int rowStride;
    private final double[] array;

    private final int loopBoundRow;
    private final int loopBoundCol;
    private final VectorMask<Double> loopMaskRow;
    private final VectorMask<Double> loopMaskCol;
    private final int[] colVectorIndexes;

    public DMatrixDenseR(int rows, int cols) {
        this(0, rows, cols, new double[rows * cols]);
    }

    public DMatrixDenseR(int offset, int rows, int cols, double[] array) {
        this(offset, rows, cols, cols, array);
    }

    public DMatrixDenseR(int offset, int rows, int cols, int rowStride, double[] array) {
        this.offset = offset;
        this.rows = rows;
        this.cols = cols;
        this.rowStride = rowStride;
        this.array = array;
        this.loopBoundRow = species.loopBound(cols);
        this.loopMaskRow = species.indexInRange(loopBoundRow, cols);
        this.loopBoundCol = species.loopBound(rows);
        this.loopMaskCol = species.indexInRange(loopBoundCol, rows);
        this.colVectorIndexes = new int[speciesLen];
        for (int i = 0; i < speciesLen; i++) {
            colVectorIndexes[i] = i * rowStride;
        }
    }

    @Override
    public double[] solidArrayCopy() {
        if (cols == rowStride) {
            return Arrays.copyOfRange(array, offset, offset + rows * cols);
        } else {
            double[] copy = new double[rows * cols];
            for (int i = 0; i < rows; i++) {
                System.arraycopy(array, offset + i * rowStride, copy, i * cols, cols);
            }
            return copy;
        }
    }

    @Override
    public void apply(Double2DoubleFunction fun) {
        if (cols == rowStride) {
            int len = offset + rows * cols;
            for (int i = offset; i < len; i++) {
                array[i] = fun.apply(array[i]);
            }
        } else {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    set(i, j, fun.apply(get(i, j)));
                }
            }
        }
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
        return array[offset + row * rowStride + col];
    }

    @Override
    public void set(int row, int col, double value) {
        array[offset + row * rowStride + col] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        array[offset + row * rowStride + col] += value;
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
        return DoubleVector.fromArray(species, array, offset + row * rowStride + i);
    }

    @Override
    public DoubleVector loadVectorRow(int row, int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(species, array, offset + row * rowStride + i, m);
    }

    @Override
    public void storeVectorRow(DoubleVector vector, int row, int i) {
        vector.intoArray(array, offset + row * rowStride + i);
    }

    @Override
    public void storeVectorRow(DoubleVector vector, int row, int i, VectorMask<Double> m) {
        vector.intoArray(array, offset + row * rowStride + i, m);
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
        return DoubleVector.fromArray(species, array, offset + i * rowStride + col, colVectorIndexes, 0);
    }

    @Override
    public DoubleVector loadVectorCol(int col, int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(species, array, offset + i * rowStride + col, colVectorIndexes, 0, m);
    }

    @Override
    public void storeVectorCol(DoubleVector vector, int col, int i) {
        vector.intoArray(array, offset + i * rowStride + col, colVectorIndexes, 0);
    }

    @Override
    public void storeVectorCol(DoubleVector vector, int col, int i, VectorMask<Double> m) {
        vector.intoArray(array, offset + i * rowStride + col, colVectorIndexes, 0, m);
    }

    @Override
    public DVector mapRow(int row) {
        return new DVectorDense(offset + row * rowStride, cols, array);
    }

    @Override
    public DVector mapRowTo(int row, DVector to) {
        if (to instanceof DVectorDense tod) {
            System.arraycopy(array, offset + row * rowStride, tod.array(), tod.offset(), cols);
            return tod;
        }
        for (int i = 0; i < cols; i++) {
            to.set(i, array[offset + row * rowStride + i]);
        }
        return to;
    }

    @Override
    public DVector mapCol(int col) {
        return new DVectorStride(offset + col, rowStride, rows, array);
    }

    @Override
    public DVector mapColTo(int col, DVector to) {
        for (int i = 0; i < rows; i++) {
            to.set(i, get(i, col));
        }
        return to;
    }

    @Override
    public DMatrix mapRows(int... indexes) {
        int[] rowIndexes = Arrays.copyOf(indexes, indexes.length);
        IntArrays.mul(rowIndexes, 0, rowStride, rowIndexes.length);
        int[] colIndexes = IntArrays.newSeq(0, cols);
        return new DMatrixMap(offset, rowIndexes, colIndexes, array);
    }

    @Override
    public DMatrix mapRowsTo(int[] indexes, DMatrix to) {
        int[] rowIndexes = Arrays.copyOf(indexes, indexes.length);
        IntArrays.mul(rowIndexes, 0, rowStride, rowIndexes.length);
        int[] colIndexes = IntArrays.newSeq(0, cols);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < colIndexes.length; j++) {
                to.set(i, j, array[offset + rowIndexes[i] + colIndexes[j]]);
            }
        }
        return to;
    }

    @Override
    public DMatrix mapCols(int... indexes) {
        int[] rowIndexes = IntArrays.newSeq(0, rows);
        IntArrays.mul(rowIndexes, 0, rowStride, rowIndexes.length);
        return new DMatrixMap(offset, rowIndexes, indexes, array);
    }

    @Override
    public DMatrix mapColsTo(int[] indexes, DMatrix to) {
        int[] rowIndexes = IntArrays.newSeq(0, rows);
        IntArrays.mul(rowIndexes, 0, rowStride, rowIndexes.length);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < indexes.length; j++) {
                to.set(i, j, array[offset + rowIndexes[i] + indexes[j]]);
            }
        }
        return to;
    }

    @Override
    public DMatrix rangeRows(int start, int end) {
        return mapRows(IntArrays.newSeq(start, end));
    }

    @Override
    public DMatrix rangeRowsTo(int start, int end, DMatrix to) {
        return mapRowsTo(IntArrays.newSeq(start, end), to);
    }

    @Override
    public DMatrix rangeCols(int start, int end) {
        return mapCols(IntArrays.newSeq(start, end));
    }

    @Override
    public DMatrix rangeColsTo(int start, int end, DMatrix to) {
        return mapColsTo(IntArrays.newSeq(start, end), to);
    }

    @Override
    public DVector dot(DVector b) {
        if (b.size() != colCount()) {
            throw new IllegalArgumentException("Matrix ( %d x %d ) and vector ( %d ) not compatible for multiplication."
                    .formatted(rows, cols, b.size()));
        }

        // obtain the vector array of elements either as a reference or as a copy
        double[] vector = b.valueStream().toArray();

        // allocate memory for the result vector
        double[] c = new double[rowCount()];

        // employ parallelism only if we have large row vectors
        final int sliceSize = 256;
        final int slices = rowCount() / sliceSize;
        IntStream stream = IntStream.range(0, slices + 1);
        if (slices > 1) {
            stream = stream.parallel();
        }
        stream.forEach(s -> {
            for (int i = s * sliceSize; i < Math.min(rowCount(), (s + 1) * sliceSize); i++) {
                c[i] = DoubleArrays.dotSum(array, offset + i * rowStride, vector, 0, cols);
            }
        });
        return new DVectorDense(0, c.length, c);
    }

    @Override
    public DMatrix t(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            return new DMatrixDenseC(0, cols, rows, solidArrayCopy());
        }
        return new DMatrixDenseC(offset, cols, rows, rowStride, array);
    }

    @Override
    public DMatrixDenseR apply(Double2DoubleFunction fun, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = solidArrayCopy();
            for (int i = 0; i < copy.length; i++) {
                copy[i] = fun.apply(copy[i]);
            }
            return new DMatrixDenseR(0, rows, cols, copy);
        }
        apply(fun);
        return this;
    }

    @Override
    public DMatrix copy() {
        return new DMatrixDenseR(0, rows, cols, solidArrayCopy());
    }
}
