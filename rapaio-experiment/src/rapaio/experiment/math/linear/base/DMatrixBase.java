/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.experiment.math.linear.base;

import java.util.Arrays;
import java.util.stream.IntStream;

import rapaio.experiment.math.linear.DMatrix;
import rapaio.experiment.math.linear.DVector;
import rapaio.experiment.math.linear.dense.AbstractDMatrix;
import rapaio.experiment.math.linear.dense.DMatrixMap;
import rapaio.experiment.math.linear.dense.DVectorDense;
import rapaio.experiment.math.linear.dense.DVectorStride;
import rapaio.util.collection.IntArrays;

public class DMatrixBase extends AbstractDMatrix {

    private final int rows;
    private final int cols;
    private final double[] array;

    public DMatrixBase(int rows, int cols, double[] array) {
        this.rows = rows;
        this.cols = cols;
        this.array = array;
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public int cols() {
        return cols;
    }

    @Override
    public double get(int row, int col) {
        return array[col * rows + row];
    }

    @Override
    public void set(int row, int col, double value) {
        array[col * rows + row] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        array[col * rows + row] += value;
    }

    @Override
    public DVector mapRow(int row) {
        return new DVectorStride(row, rows, cols, array);
    }

    @Override
    public DVector mapRowTo(DVector to, int row) {
        for (int i = 0; i < cols; i++) {
            to.set(i, array[row + i * rows]);
        }
        return to;
    }

    @Override
    public DVector mapCol(int col) {
        return new DVectorDense(col * rows, rows, array);
    }

    @Override
    public DVector mapColTo(DVector to, int col) {
        for (int i = 0; i < rows; i++) {
            to.set(i, array[col * rows + i]);
        }
        return to;
    }

    @Override
    public DMatrix mapRows(int... indexes) {
        int[] rowIndexes = Arrays.copyOf(indexes, indexes.length);
        int[] colIndexes = IntArrays.newSeq(0, cols());
        IntArrays.mul(colIndexes, 0, rows(), colIndexes.length);
        return new DMatrixMap(0, rowIndexes, colIndexes, array);
    }

    @Override
    public DMatrix mapRowsTo(DMatrix to, int... indexes) {
        int[] rowIndexes = Arrays.copyOf(indexes, indexes.length);
        int[] colIndexes = IntArrays.newSeq(0, cols());
        IntArrays.mul(colIndexes, 0, rows(), colIndexes.length);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < colIndexes.length; j++) {
                to.set(i, j, array[rowIndexes[i] + colIndexes[j]]);
            }
        }
        return to;
    }

    @Override
    public DMatrix mapCols(int... indexes) {
        int[] rowIndexes = IntArrays.newSeq(0, cols());
        int[] colIndexes = Arrays.copyOf(indexes, indexes.length);
        IntArrays.mul(colIndexes, 0, rows(), colIndexes.length);
        return new DMatrixMap(0, rowIndexes, colIndexes, array);
    }

    @Override
    public DMatrix mapColsTo(DMatrix to, int... indexes) {
        int[] rowIndexes = IntArrays.newSeq(0, cols());
        int[] colIndexes = Arrays.copyOf(indexes, indexes.length);
        IntArrays.mul(colIndexes, 0, rows(), colIndexes.length);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < colIndexes.length; j++) {
                to.set(i, j, array[rowIndexes[i] + colIndexes[j]]);
            }
        }
        return to;
    }

    @Override
    public DMatrix rangeRows(int start, int end) {
        return mapRows(IntArrays.newSeq(start, end));
    }

    @Override
    public DMatrix rangeRowsTo(DMatrix to, int start, int end) {
        return mapRowsTo(to, IntArrays.newSeq(start, end));
    }

    @Override
    public DMatrix rangeCols(int start, int end) {
        return mapCols(IntArrays.newSeq(start, end));
    }

    @Override
    public DMatrix rangeColsTo(DMatrix to, int start, int end) {
        return mapColsTo(to, IntArrays.newSeq(start, end));
    }

    @Override
    public DMatrix dot(DMatrix b) {
        if (cols() != b.rows()) {
            throw new IllegalArgumentException(
                    String.format("Matrices not conformant for multiplication: (%d,%d) x (%d,%d)",
                            rows(), cols(), b.rows(), b.cols()));
        }
        DMatrix C = DMatrix.empty(rows(), b.cols());
        DVector[] as = new DVector[C.rows()];
        DVector[] bs = new DVector[C.cols()];
        for (int i = 0; i < C.rows(); i++) {
            as[i] = mapRowNew(i);
        }
        for (int i = 0; i < C.cols(); i++) {
            bs[i] = b.mapCol(i);
        }
        IntStream.range(0, C.rows()).parallel().forEach(i -> {
            for (int j = 0; j < C.cols(); j++) {
                C.set(i, j, as[i].dot(bs[j]));
            }
        });
        return C;
    }
}
