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
import rapaio.math.linear.base.AbstractDMatrix;
import rapaio.util.function.Double2DoubleFunction;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/4/15.
 */
public class DMatrixMap extends AbstractDMatrix {

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
    public Type type() {
        return Type.MAP;
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
    public DVectorDense mapCol(int i) {
        DVectorDense v = DVectorDense.zeros(rowIndexes.length);
        for (int j = 0; j < rowIndexes.length; j++) {
            v.set(j, ref.get(rowIndexes[j], colIndexes[i]));
        }
        return v;
    }

    @Override
    public DVector mapRow(int i) {
        DVectorDense v = DVectorDense.zeros(colIndexes.length);
        for (int j = 0; j < colIndexes.length; j++) {
            v.set(j, ref.get(rowIndexes[i], colIndexes[j]));
        }
        return v;
    }

    @Override
    public DMatrix apply(Double2DoubleFunction fun) {
        for (int row : rowIndexes) {
            for (int col : colIndexes) {
                ref.set(row, col, fun.applyAsDouble(ref.get(row, col)));
            }
        }
        return this;
    }

    @Override
    public DMatrixStripe t() {
        DMatrixStripe copy = DMatrixStripe.empty(colIndexes.length, rowIndexes.length);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < colIndexes.length; j++) {
                copy.set(j, i, ref.get(rowIndexes[i], colIndexes[j]));
            }
        }
        return copy;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(rowIndexes)
                .boxed()
                .flatMap(r -> Arrays.stream(colIndexes).boxed().map(c -> rapaio.util.Pair.from(r, c)))
                .mapToDouble(p -> ref.get(p.v1, p.v2));
    }

    @Override
    public DMatrixStripe copy() {
        DMatrixStripe copy = DMatrixStripe.empty(rowIndexes.length, colIndexes.length);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < colIndexes.length; j++) {
                copy.set(i, j, ref.get(rowIndexes[i], colIndexes[j]));
            }
        }
        return copy;
    }
}
