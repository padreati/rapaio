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

public class DMatrixDenseR extends DMatrixDense {

    public DMatrixDenseR(int rows, int cols) {
        this(rows, cols, new double[rows * cols]);
    }

    public DMatrixDenseR(int rows, int cols, double[] values) {
        super(MType.RDENSE, rows, cols, values);
    }

    @Override
    public double get(int row, int col) {
        return values[row * colCount + col];
    }

    @Override
    public void set(int row, int col, double value) {
        values[row * colCount + col] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        values[row * colCount + col] += value;
    }

    @Override
    public DMatrix t(AlgebraOption<?>... opts) {
        double[] ref = values;
        if (AlgebraOptions.from(opts).isCopy()) {
            ref = Arrays.copyOf(values, rowCount * colCount);
        }
        return new DMatrixDenseC(colCount, rowCount, ref);
    }

    @Override
    public DMatrix copy() {
        return new DMatrixDenseR(rowCount, colCount, Arrays.copyOf(values, values.length));
    }

    @Override
    public DVector dot(DVector b) {

        if (b.size() != colCount) {
            throw new IllegalArgumentException(String.format(
                    "Matrix ( %d x %d ) and vector ( %d ) not compatible for multiplication.", rowCount, colCount, b.size()));
        }

        // obtain the vector array of elements either as a reference or as a copy
        double[] vector = b.valueStream().toArray();

        // allocate memory for the result vector
        double[] c = new double[rowCount];

        // employ parallelism only if we have large row vectors
        final int sliceSize = 256;
        final int slices = rowCount / sliceSize;
        IntStream stream = IntStream.range(0, slices + 1);
        if (slices > 1) {
            stream = stream.parallel();
        }
        stream.forEach(s -> {
            for (int i = s * sliceSize; i < Math.min(rowCount, (s + 1) * sliceSize); i++) {
                c[i] = DoubleArrays.dotSum(values, i * colCount, vector, 0, colCount);
            }
        });
        return new DVectorDense(0, c.length, c);
    }
}
