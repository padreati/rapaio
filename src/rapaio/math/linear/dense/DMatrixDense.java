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
import rapaio.math.linear.base.AbstractDMatrix;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;

import java.util.stream.DoubleStream;

/**
 * A dense matrix is a matrix stored as a contiguous array in row major or
 * column major order.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/2/21.
 */
public abstract class DMatrixDense extends AbstractDMatrix {

    private static final long serialVersionUID = 4455940496310789794L;
    protected final MType type;
    protected final int rows;
    protected final int cols;
    protected final double[] array;

    protected DMatrixDense(MType type, int rows, int cols) {
        this(type, rows, cols, DoubleArrays.newFill(rows * cols, 0));
    }

    protected DMatrixDense(MType type, int rows, int cols, double[] array) {
        this.type = type;
        this.rows = rows;
        this.cols = cols;
        this.array = array;
        validate();
    }

    @Override
    public MType type() {
        return type;
    }

    private void validate() {
        if (rows <= 0) {
            throw new IllegalArgumentException("Number of rows must be a finite positive number.");
        }
        if (cols <= 0) {
            throw new IllegalArgumentException("Number of columns must be a finite positive number.");
        }
        if (array.length < rows * cols) {
            throw new IllegalArgumentException("The array of elements is smaller than rows multiplied by columns.");
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

    public double[] getElements() {
        return array;
    }

    /**
     * Apply the given function to all elements of the matrix.
     *
     * @param fun function to be applied
     * @return same instance matrix
     */
    @Override
    public DMatrix apply(Double2DoubleFunction fun) {
        for (int i = 0; i < rows * cols; i++) {
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
        return DoubleStream.of(array).limit((long) rows * cols);
    }
}
