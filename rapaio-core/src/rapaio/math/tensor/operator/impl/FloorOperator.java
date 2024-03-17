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

package rapaio.math.tensor.operator.impl;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import rapaio.math.tensor.operator.TensorUnaryOp;

public final class FloorOperator implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public byte applyByte(byte v) {
        return v;
    }

    @Override
    public int applyInt(int v) {
        return v;
    }

    @Override
    public float applyFloat(float v) {
        return (float) StrictMath.floor(v);
    }

    @Override
    public double applyDouble(double v) {
        return StrictMath.floor(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        return v;
    }

    @Override
    public IntVector applyInt(IntVector v) {
        return v;
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        VectorMask<Float> m = v.compare(VectorOperators.LT, 0);
        if (m.anyTrue()) {
            v = v.sub(1.f, m);
        }
        return v.convert(VectorOperators.F2I, 0).convert(VectorOperators.I2F, 0).reinterpretAsFloats();
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        VectorMask<Double> m = v.compare(VectorOperators.LT, 0);
        if (m.anyTrue()) {
            v = v.sub(1., m);
        }
        return v.convert(VectorOperators.D2L, 0).convert(VectorOperators.L2D, 0).reinterpretAsDoubles();
    }
}
