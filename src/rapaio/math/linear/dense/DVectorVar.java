/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.dense;

import java.util.stream.DoubleStream;

import rapaio.data.MappedVar;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.util.collection.DoubleArrays;

/**
 * Wrapper class over a {@link Var} and offers basic implementations for vector operations.
 * If the original variable is {@link VarDouble} than a better wrapper is {@link DVectorDense}.
 * <p>
 * Attention must be provided since operations are stored in the original variable and if
 * that does not store doubles, than loss of precision will be involved.
 *
 * @param <T> type of the original variable
 */
public class DVectorVar<T extends Var> extends AbstractDVector {

    private final T ref;

    public DVectorVar(T ref) {
        this.ref = ref;
    }

    @Override
    public int size() {
        return ref.size();
    }

    @Override
    public DVector map(int... indexes) {
        return new DVectorVar<>(MappedVar.byRows(ref, indexes));
    }

    @Override
    public DVector mapTo(DVector to, int... indexes) {
        for (int i = 0; i < indexes.length; i++) {
            to.set(i, ref.getDouble(indexes[i]));
        }
        return to;
    }

    @Override
    public DVectorDense copy() {
        double[] copy = DoubleArrays.newFrom(0, size(), ref::getDouble);
        return DVector.wrap(copy);
    }

    @Override
    public double get(int i) {
        return ref.getDouble(i);
    }

    @Override
    public void set(int i, double value) {
        ref.setDouble(i, value);
    }

    @Override
    public void inc(int i, double value) {
        ref.setDouble(i, ref.getDouble(i) + value);
    }

    @Override
    public DVector fill(double value) {
        for (int i = 0; i < ref.size(); i++) {
            ref.setDouble(i, value);
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return ref.stream().mapToDouble();
    }

    @Override
    public VarDouble dv() {
        if (ref instanceof VarDouble refd) {
            return refd;
        }
        double[] copy = DoubleArrays.newFrom(0, size(), ref::getDouble);
        return VarDouble.wrap(copy);
    }
}
