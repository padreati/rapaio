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

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.collection.DoubleArrays;

public class DMatrixDenseC extends DMatrixDense {

    private static final int SLICE_SIZE = 1024;

    public DMatrixDenseC(int rows, int cols) {
        this(rows, cols, new double[rows * cols]);
    }

    public DMatrixDenseC(int rows, int cols, double[] values) {
        super(MType.CDENSE, rows, cols, values);
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
    public DMatrix t(AlgebraOption<?>... opts) {
        double[] ref = values;
        if (AlgebraOptions.from(opts).isCopy()) {
            ref = DoubleArrays.copy(values, 0, rowCount * colCount);
        }
        return new DMatrixDenseR(colCount, rowCount, ref);
    }

    @Override
    public DMatrix copy() {
        return new DMatrixDenseC(rowCount, colCount, Arrays.copyOf(values, values.length));
    }
}
