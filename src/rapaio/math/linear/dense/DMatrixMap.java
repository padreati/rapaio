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

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.util.collection.IntArrays;

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
        if (ref instanceof DMatrixMap mref) {
            if (byRow) {
                this.ref = mref.ref;
                this.rowIndexes = Arrays.copyOf(indexes, indexes.length);
                this.colIndexes = mref.colIndexes;
            } else {
                this.ref = mref.ref;
                this.rowIndexes = mref.rowIndexes;
                this.colIndexes = Arrays.copyOf(indexes, indexes.length);
            }
            return;
        }
        if (byRow) {
            this.ref = ref;
            this.rowIndexes = Arrays.copyOf(indexes, indexes.length);
            this.colIndexes = IntArrays.newSeq(0, ref.colCount());
        } else {
            this.ref = ref;
            this.rowIndexes = IntArrays.newSeq(0, ref.rowCount());
            this.colIndexes = Arrays.copyOf(indexes, indexes.length);
        }
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
    public void inc(int i, int j, double value) {
        ref.inc(rowIndexes[i], colIndexes[j], value);
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
