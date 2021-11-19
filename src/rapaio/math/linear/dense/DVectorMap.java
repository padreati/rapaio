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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.base.AbstractDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/21.
 */
public class DVectorMap extends AbstractDVector {

    @Serial
    private static final long serialVersionUID = -1952913189054878826L;

    private final DVector source;
    private final int[] indexes;

    public DVectorMap(DVector source, int... indexes) {
        if (source instanceof DVectorMap sourceMap) {
            int[] copy = new int[indexes.length];
            for (int i = 0; i < indexes.length; i++) {
                copy[i] = sourceMap.indexes[indexes[i]];
            }
            this.indexes = copy;
            this.source = sourceMap.source;
        } else {
            this.indexes = indexes;
            this.source = source;
        }
    }

    @Override
    public int size() {
        return indexes.length;
    }

    @Override
    public DVector map(int[] idxs, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[idxs.length];
            for (int i = 0; i < idxs.length; i++) {
                copy[i] = source.get(indexes[idxs[i]]);
            }
            return new DVectorDense(0, copy.length, copy);
        } else {
            return new DVectorMap(this, idxs);
        }
    }

    @Override
    public DVectorDense copy() {
        double[] copy = new double[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            copy[i] = source.get(indexes[i]);
        }
        return new DVectorDense(0, copy.length, copy);
    }

    @Override
    public double get(int i) {
        return source.get(indexes[i]);
    }

    @Override
    public void set(int i, double value) {
        source.set(indexes[i], value);
    }

    @Override
    public void inc(int i, double value) {
        source.inc(indexes[i], value);
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.of(indexes).mapToDouble(source::get);
    }

    @Override
    public VarDouble asVarDouble() {
        double[] copy = new double[indexes.length];
        int pos = 0;
        for (int i : indexes) {
            copy[pos++] = source.get(i);
        }
        return VarDouble.wrapArray(copy.length, copy);
    }
}
