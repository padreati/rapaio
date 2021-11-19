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

import java.util.stream.DoubleStream;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.VType;
import rapaio.math.linear.base.AbstractDVector;
import rapaio.util.collection.DoubleArrays;

public class DVectorVar<T extends Var> extends AbstractDVector {

    private final T ref;

    public DVectorVar(T ref) {
        this.ref = ref;
    }

    @Override
    public VType type() {
        return VType.VAR;
    }

    @Override
    public VType innerType() {
        return VType.VAR;
    }

    @Override
    public int size() {
        return ref.size();
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
    public DoubleStream valueStream() {
        return ref.stream().mapToDouble();
    }

    public T asVar() {
        return ref;
    }

    @Override
    public VarDouble asVarDouble() {
        if (ref instanceof VarDouble dref) {
            return dref;
        }
        return VarDouble.wrap(DoubleArrays.newFrom(0, ref.size(), ref::getDouble));
    }
}
