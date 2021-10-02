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

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.math.linear.base.AbstractDMatrix;
import rapaio.math.linear.option.AlgebraOption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/4/15.
 */
public class DMatrixMap extends AbstractDMatrix {

    @Serial
    private static final long serialVersionUID = -3840785397560969659L;

    private final DMatrix ref;
    private final int[] rowIndexes;
    private final int[] colIndexes;

    public DMatrixMap(DMatrix ref, boolean byRow, int... indexes) {
        if (byRow) {
            this.ref = ref;
            this.rowIndexes = indexes;
            this.colIndexes = new int[ref.colCount()];
            for (int i = 0; i < ref.colCount(); i++) {
                this.colIndexes[i] = i;
            }
        } else {
            this.ref = ref;
            this.rowIndexes = new int[ref.rowCount()];
            for (int i = 0; i < ref.rowCount(); i++) {
                this.rowIndexes[i] = i;
            }
            this.colIndexes = indexes;
        }
    }

    @Override
    public MType type() {
        return MType.MAP;
    }

    @Override
    public MType innerType() {
        return ref.innerType();
    }

    @Override
    public int rowCount() {
        return rowIndexes.length;
    }

    @Override
    public int colCount() {
        return colIndexes.length;
    }

    @Override
    public double get(int i, int j) {
        return ref.get(rowIndexes[i], colIndexes[j]);
    }

    @Override
    public void set(int i, int j, double value) {
        ref.set(rowIndexes[i], colIndexes[j], value);
    }

    @Override
    public void inc(int row, int col, double value) {
        ref.inc(rowIndexes[row], colIndexes[col], value);
    }

    @Override
    public DVector map(int index, int axis, AlgebraOption<?>... opts) {
        return switch (axis) {
            case 0 -> mapRow(index, opts);
            case 1 -> mapCol(index, opts);
            default -> throw new IllegalArgumentException("Axis value is invalid.");
        };
    }

    @Override
    public DVector mapCol(int i, AlgebraOption<?>... opts) {
        DVector v = DVector.zeros(rowIndexes.length);
        for (int j = 0; j < rowIndexes.length; j++) {
            v.set(j, ref.get(rowIndexes[j], colIndexes[i]));
        }
        return v;
    }

    @Override
    public DVector mapRow(int i, AlgebraOption<?>... opts) {
        DVector v = DVector.zeros(colIndexes.length);
        for (int j = 0; j < colIndexes.length; j++) {
            v.set(j, ref.get(rowIndexes[i], colIndexes[j]));
        }
        return v;
    }
}
