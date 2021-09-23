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

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.util.collection.DoubleArrays;

import java.io.Serial;

/**
 * Dense matrix with values in double floating point precision.
 * Values are stored in arrays of arrays with first array holding column references
 * and secondary level arrays being the column arrays.
 */
public class DMatrixStripeC extends DMatrixStripe {

    @Serial
    private static final long serialVersionUID = -2186520026933442642L;

    public DMatrixStripeC(int rows, int cols) {
        super(MType.CSTRIPE, rows, cols, newArray(rows, cols));
    }

    public DMatrixStripeC(int rows, int cols, double[][] array) {
        super(MType.CSTRIPE, rows, cols, array);
    }

    private static double[][] newArray(int rowCount, int colCount) {
        double[][] array = new double[colCount][rowCount];
        for (int i = 0; i < colCount; i++) {
            array[i] = DoubleArrays.newFill(rowCount, 0);
        }
        return array;
    }

    @Override
    public DVector mapCol(int col) {
        return DVector.wrapArray(rowCount, values[col]);
    }

    @Override
    public DMatrix mapCols(int... indexes) {
        double[][] array = new double[indexes.length][rowCount];
        for (int i = 0; i < indexes.length; i++) {
            array[i] = values[indexes[i]];
        }
        return new DMatrixStripeC(rowCount, indexes.length, array);
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
        double[] array = (b instanceof DVectorDense) ? ((DVectorDense)b).elements() : b.valueStream().toArray();
        double[] c = DoubleArrays.newFill(rowCount, 0);
        for (int i = 0; i < colCount; i++) {
            for (int j = 0; j < c.length; j++) {
                c[j] += values[i][j] * array[i];
            }
        }
        return new DVectorDense(c.length, c);
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
        double[][] copy = new double[colCount][rowCount];
        for (int i = 0; i < colCount; i++) {
            copy[i] = DoubleArrays.copy(values[i], 0, rowCount);
        }
        return new DMatrixStripeC(rowCount, colCount, copy);
    }

    @Override
    public DMatrix resizeCopy(int rows, int cols, double fill) {
        double[][] copy = new double[cols][rows];

        for (int i = 0; i < Math.min(cols, colCount); i++) {
            copy[i] = DoubleArrays.newFill(rows, fill);
            System.arraycopy(values[i], 0, copy[i], 0, Math.min(rows, rowCount));
        }
        for (int i = colCount; i < cols; i++) {
            copy[i] = DoubleArrays.newFill(rows, fill);
        }
        return new DMatrixStripeC(rows, cols, copy);
    }
}
