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
import java.util.Arrays;
import java.util.stream.IntStream;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.collection.DoubleArrays;

/**
 * Dense matrix with values in double floating point precision.
 * Values are stored in arrays of arrays with first array holding row references
 * and secondary level arrays being the row arrays.
 */
public class DMatrixStripeR extends DMatrixStripe {

    @Serial
    private static final long serialVersionUID = -2186520026933442642L;

    public DMatrixStripeR(int rowCount, int colCount) {
        super(MType.RSTRIPE, rowCount, colCount, newArray(rowCount, colCount));
    }

    public DMatrixStripeR(int rowCount, int colCount, double[][] values) {
        super(MType.RSTRIPE, rowCount, colCount, values);
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
    public DVector mapRow(final int row, AlgebraOption<?>... opts) {
        double[] ref = values[row];
        if (AlgebraOptions.from(opts).isCopy()) {
            ref = Arrays.copyOf(values[row], values[row].length);
        }
        return new DVectorDense(ref.length, ref);
    }

    @Override
    public DMatrix mapRows(int[] indexes, AlgebraOption<?>... opts) {
        if (indexes.length == 0) {
            throw new IllegalArgumentException("Cannot map rows with empty indexes.");
        }
        double[][] wrap = new double[indexes.length][colCount];
        for (int i = 0; i < indexes.length; i++) {
            if (AlgebraOptions.from(opts).isCopy()) {
                wrap[i] = Arrays.copyOf(values[indexes[i]], values[indexes[i]].length);
            } else {
                wrap[i] = values[indexes[i]];
            }
        }
        return new DMatrixStripeR(indexes.length, colCount, wrap);
    }

    @Override
    public DVector dot(DVector b) {
        double[] varray = (b instanceof DVectorDense) ? ((DVectorDense) b).elements() : b.valueStream().toArray();
        double[] c = DoubleArrays.newFill(rowCount, 0);
        IntStream.range(0, rowCount).parallel().forEach(i -> {
            double sum = 0;
            for (int j = 0; j < values[i].length; j++) {
                sum += values[i][j] * varray[j];
            }
            c[i] = sum;
        });
        return new DVectorDense(c.length, c);
    }

    @Override
    public DMatrix t(AlgebraOption<?>... opts) {
        double[][] ref = values;
        if (AlgebraOptions.from(opts).isCopy()) {
            ref = new double[values.length][];
            for (int i = 0; i < values.length; i++) {
                ref[i] = Arrays.copyOf(values[i], values[i].length);
            }
        }
        return new DMatrixStripeC(colCount, rowCount, ref);
    }

    @Override
    public DMatrixStripeR copy() {
        double[][] copy = new double[rowCount][colCount];
        for (int i = 0; i < rowCount; i++) {
            copy[i] = DoubleArrays.copy(values[i], 0, colCount);
        }
        return new DMatrixStripeR(rowCount, colCount, copy);
    }
}
