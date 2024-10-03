/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.math.linear.dense;

import java.io.Serial;

import rapaio.experiment.math.linear.DMatrix;
import rapaio.experiment.math.linear.DVector;
import rapaio.util.collection.IntArrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/4/15.
 */
public class DMatrixMap extends AbstractDMatrix {

    @Serial
    private static final long serialVersionUID = -3840785397560969659L;

    private final int offset;
    private final int[] rowIndexes;
    private final int[] colIndexes;
    private final double[] array;

    public DMatrixMap(int offset, int[] rowIndexes, int[] colIndexes, double[] array) {
        this.offset = offset;
        this.rowIndexes = rowIndexes;
        this.colIndexes = colIndexes;
        this.array = array;
    }

    @Override
    public int rows() {
        return rowIndexes.length;
    }

    @Override
    public int cols() {
        return colIndexes.length;
    }

    @Override
    public double get(int row, int col) {
        return array[offset + rowIndexes[row] + colIndexes[col]];
    }

    @Override
    public void set(int row, int col, double value) {
        array[offset + rowIndexes[row] + colIndexes[col]] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        array[offset + rowIndexes[row] + colIndexes[col]] += value;
    }

    @Override
    public DVector mapCol(int col) {
        return new DVectorMap(offset + colIndexes[col], rowIndexes, array);
    }

    @Override
    public DVector mapColTo(DVector to, int col) {
        int pos = offset + colIndexes[col];
        for (int i = 0; i < rowIndexes.length; i++) {
            to.set(i, array[pos + rowIndexes[i]]);
        }
        return to;
    }

    @Override
    public DVector mapRow(int row) {
        return new DVectorMap(offset + rowIndexes[row], colIndexes, array);
    }

    @Override
    public DVector mapRowTo(DVector to, int row) {
        int pos = offset + rowIndexes[row];
        for (int i = 0; i < colIndexes.length; i++) {
            to.set(i, array[pos + colIndexes[i]]);
        }
        return to;
    }

    private int[] transform(int[] base, int[] selection) {
        int[] transform = new int[selection.length];
        for (int i = 0; i < transform.length; i++) {
            transform[i] = base[selection[i]];
        }
        return transform;
    }

    @Override
    public DMatrix mapRows(int[] indexes) {
        int[] transform = transform(rowIndexes, indexes);
        return new DMatrixMap(offset, transform, colIndexes, array);
    }

    @Override
    public DMatrix mapRowsTo(DMatrix to, int... indexes) {
        int[] transform = transform(rowIndexes, indexes);
        for (int i = 0; i < transform.length; i++) {
            for (int j = 0; j < colIndexes.length; j++) {
                to.set(i, j, array[offset + transform[i] + colIndexes[j]]);
            }
        }
        return to;
    }

    @Override
    public DMatrix mapCols(int... indexes) {
        int[] transform = transform(colIndexes, indexes);
        return new DMatrixMap(offset, rowIndexes, transform, array);
    }

    @Override
    public DMatrix mapColsTo(DMatrix to, int... indexes) {
        int[] transform = transform(colIndexes, indexes);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < transform.length; j++) {
                to.set(i, j, array[offset + rowIndexes[i] + transform[j]]);
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
}
