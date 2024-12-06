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

package rapaio.darray.operator.impl;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayUnaryOp;

public class UnaryOpNanToNum<N extends Number> extends DArrayUnaryOp {

    private final N nan;
    private final N ninf;
    private final N pinf;

    public UnaryOpNanToNum(N nan, N ninf, N pinf) {
        super(false);
        this.nan = nan;
        this.ninf = ninf;
        this.pinf = pinf;
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, Storage s) {
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, Storage s) {
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor<Byte> loop, Storage s) {
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, Storage s) {
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, Storage s) {
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor<Integer> loop, Storage s) {
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        var vnan = FloatVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = FloatVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = FloatVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var b = s.getFloatVector(loop.vs, p);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Float.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Float.NEGATIVE_INFINITY));
                    s.setFloatVector(b, p);
                }
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                float v = s.getFloat(p);
                v = Float.isNaN(v) ? nan.floatValue() : v;
                v = Float.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.floatValue() : ninf.floatValue());
                s.setFloat(p, v);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        var vnan = FloatVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = FloatVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = FloatVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector b = s.getFloatVector(loop.vs, p, loop.simdOffsets(), 0);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Float.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Float.NEGATIVE_INFINITY));
                    s.setFloatVector(b, p, loop.simdOffsets(), 0);
                }
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                float v = s.getFloat(p);
                v = Float.isNaN(v) ? nan.floatValue() : v;
                v = Float.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.floatValue() : ninf.floatValue());
                s.setFloat(p, v);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                float v = s.getFloat(p);
                v = Float.isNaN(v) ? nan.floatValue() : v;
                v = Float.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.floatValue() : ninf.floatValue());
                s.setFloat(p, v);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        var vnan = DoubleVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = DoubleVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = DoubleVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector b = s.getDoubleVector(loop.vs, p);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Double.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Double.NEGATIVE_INFINITY));
                    s.setDoubleVector(b, p);
                }
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                double v = s.getDouble(p);
                v = Double.isNaN(v) ? nan.doubleValue() : v;
                v = Double.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.doubleValue() : ninf.doubleValue());
                s.setDouble(p, v);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        var vnan = DoubleVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = DoubleVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = DoubleVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector b = s.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Double.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Double.NEGATIVE_INFINITY));
                    s.setDoubleVector(b, p, loop.simdOffsets(), 0);
                }
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                double v = s.getDouble(p);
                v = Double.isNaN(v) ? nan.doubleValue() : v;
                v = Double.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.doubleValue() : ninf.doubleValue());
                s.setDouble(p, v);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double v = s.getDouble(p);
                v = Double.isNaN(v) ? nan.doubleValue() : v;
                v = Double.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.doubleValue() : ninf.doubleValue());
                s.setDouble(p, v);
                p += loop.step;
            }
        }
    }
}
