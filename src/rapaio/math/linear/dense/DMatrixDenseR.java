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
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.util.collection.DoubleArrays;

import java.io.Serial;
import java.util.stream.IntStream;

/**
 * Dense matrix with values in double floating point precision.
 * Values are stored in arrays of arrays with first array holding row references
 * and secondary level arrays being the row arrays.
 */
public class DMatrixDenseR extends DMatrixDense {

    @Serial
    private static final long serialVersionUID = -2186520026933442642L;

    public DMatrixDenseR(int rowCount, int colCount) {
        super(MType.RDENSE, rowCount, colCount, newArray(rowCount, colCount));
    }

    public DMatrixDenseR(int rowCount, int colCount, double[][] values) {
        super(MType.RDENSE, rowCount, colCount, values);
    }

    private static double[][] newArray(int rowCount, int colCount) {
        double[][] array = new double[rowCount][colCount];
        for (int i = 0; i < rowCount; i++) {
            array[i] = DoubleArrays.newFill(colCount, 0);
        }
        return array;
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
    public DVector mapRow(final int row) {
        return new DVectorDense(values[row].length, values[row]);
    }

    @Override
    public DMatrix mapRows(int... indexes) {
        if (indexes.length == 0) {
            throw new IllegalArgumentException("Cannot map rows with empty indexes.");
        }
        double[][] wrap = new double[indexes.length][colCount];
        for (int i = 0; i < indexes.length; i++) {
            wrap[i] = values[indexes[i]];
        }
        return new DMatrixDenseR(indexes.length, colCount, wrap);
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
        if (b instanceof DVectorDense vd) {
            int vlen = vd.size();
            double[] varray = vd.elements();

            double[] c = DoubleArrays.newFill(rowCount, 0);
            IntStream.range(0, rowCount).parallel().forEach(i -> c[i] = prodSum(values[i], varray));
            return new DVectorDense(c.length, c);
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
        return new DMatrixDenseC(colCount, rowCount, values);
    }

    @Override
    public DMatrixDenseR copy() {
        DMatrixDenseR copy = new DMatrixDenseR(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
        }
        return copy;
    }
}
