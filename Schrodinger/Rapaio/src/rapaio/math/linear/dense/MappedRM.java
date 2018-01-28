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

import java.util.Arrays;
import java.util.stream.DoubleStream;

import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/4/15.
 */
public class MappedRM implements RM {

    private static final long serialVersionUID = -3840785397560969659L;

    private final RM ref;
    private final int[] rowIndexes;
    private final int[] colIndexes;

    public MappedRM(RM ref, boolean byRow, int... indexes) {
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
    public void increment(int i, int j, double value) {
        ref.increment(rowIndexes[i], colIndexes[j], value);
    }

    @Override
    public SolidRV mapCol(int i) {
        SolidRV v = SolidRV.empty(rowIndexes.length);
        for (int j = 0; j < rowIndexes.length; j++) {
            v.set(j, ref.get(rowIndexes[j], colIndexes[i]));
        }
        return v;
    }

    @Override
    public RV mapRow(int i) {
        SolidRV v = SolidRV.empty(colIndexes.length);
        for (int j = 0; j < colIndexes.length; j++) {
            v.set(j, ref.get(rowIndexes[i], colIndexes[j]));
        }
        return v;
    }

    @Override
    public SolidRM t() {
        SolidRM copy = SolidRM.empty(colIndexes.length, rowIndexes.length);
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
                .mapToDouble(p -> ref.get(p._1, p._2));
    }

    @Override
    public SolidRM solidCopy() {
        SolidRM copy = SolidRM.empty(rowIndexes.length, colIndexes.length);
        for (int i = 0; i < rowIndexes.length; i++) {
            for (int j = 0; j < colIndexes.length; j++) {
                copy.set(i, j, ref.get(rowIndexes[i], colIndexes[j]));
            }
        }
        return copy;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public double[] getRow(int row) {
      //TODO
      return null;
    }

}
