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

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.MType;
import rapaio.util.collection.DoubleArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/28/21.
 */
public class DMatrixDenseR extends DMatrixDense {
    private static final long serialVersionUID = -2797681700466963314L;

    public DMatrixDenseR(int rows, int cols) {
        super(MType.RDENSE, rows, cols);
    }

    public DMatrixDenseR(int rows, int cols, double[] array) {
        super(MType.RDENSE, rows, cols, array);
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

    @Override
    public DMatrix copy() {
        double[] copy = DoubleArrays.copy(array, 0, array.length);
        return new DMatrixDenseR(rows, cols, copy);
    }
}
