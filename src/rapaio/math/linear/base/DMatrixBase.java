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

package rapaio.math.linear.base;

import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.AbstractDMatrix;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorStride;

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
    public int rowCount() {
        return rows;
    }

    @Override
    public int colCount() {
        return cols;
    }

    @Override
    public double get(int row, int col) {
        return array[col * rows + row];
    }

    @Override
    public void set(int row, int col, double value) {
        array[col * rows + col] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        array[col * rows + col] += value;
    }

    @Override
    public DVector mapRow(int row) {
        return new DVectorStride(row, rows, cols, array);
    }

    @Override
    public DVector mapRowTo(int row, DVector to) {
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
    public DVector mapColTo(int col, DVector to) {
        for (int i = 0; i < rows; i++) {
            to.set(i, array[col + i * rows]);
        }
        return to;
    }
}
