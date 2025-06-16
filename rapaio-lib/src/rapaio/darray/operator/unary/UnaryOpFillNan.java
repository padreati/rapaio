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

package rapaio.darray.operator.unary;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayUnaryOp;

public class UnaryOpFillNan<N extends Number> extends DArrayUnaryOp {

    private final float fillFloat;
    private final double fillDouble;

    public UnaryOpFillNan(N fill) {
        super(false);
        fillFloat = fill.floatValue();
        fillDouble = fill.doubleValue();
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor loop, Storage s) {
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor loop, Storage s) {
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor loop, Storage s) {
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor loop, Storage s) {
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor loop, Storage s) {
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor loop, Storage s) {
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor loop, Storage s) {
        var a = Simd.broadcast(fillFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector b = s.getFloatVector(p);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                s.setFloatVector(b, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (Float.isNaN(s.getFloat(p))) {
                    s.setFloat(p, fillFloat);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor loop, Storage s) {
        var a = Simd.broadcast(fillFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector b = s.getFloatVector(p, loop.simdIdx(), 0);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                s.setFloatVector(b, p, loop.simdIdx(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (Float.isNaN(s.getFloat(p))) {
                    s.setFloat(p, fillFloat);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (Float.isNaN(s.getFloat(p))) {
                    s.setFloat(p, fillFloat);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor loop, Storage s) {
        var a = Simd.broadcast(fillDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var b = s.getDoubleVector(p);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                s.setDoubleVector(b, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (Double.isNaN(s.getDouble(p))) {
                    s.setDouble(p, fillDouble);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor loop, Storage s) {
        var a = Simd.broadcast(fillDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector b = s.getDoubleVector(p, loop.simdIdx(), 0);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                s.setDoubleVector(b, p, loop.simdIdx(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (Double.isNaN(s.getDouble(p))) {
                    s.setDouble(p, fillDouble);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (Double.isNaN(s.getDouble(p))) {
                    s.setDouble(p, fillDouble);
                }
                p += loop.step;
            }
        }
    }
}
