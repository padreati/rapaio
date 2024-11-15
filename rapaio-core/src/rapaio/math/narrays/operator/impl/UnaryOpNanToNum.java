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

package rapaio.math.narrays.operator.impl;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.math.narrays.iterators.StrideLoopDescriptor;
import rapaio.math.narrays.operator.NArrayUnaryOp;

public class UnaryOpNanToNum<N extends Number> extends NArrayUnaryOp {

    private final N nan;
    private final N ninf;
    private final N pinf;

    public UnaryOpNanToNum(N nan, N ninf, N pinf) {
        this.nan = nan;
        this.ninf = ninf;
        this.pinf = pinf;
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
    public double applyDouble(double v) {
        v = Double.isNaN(v) ? nan.doubleValue() : v;
        v = Double.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.doubleValue() : ninf.doubleValue());
        return v;
    }

    @Override
    public float applyFloat(float v) {
        v = Float.isNaN(v) ? nan.floatValue() : v;
        v = Float.isFinite(v) ? v : (v == Double.POSITIVE_INFINITY ? pinf.floatValue() : ninf.floatValue());
        return v;
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, float[] array) {
        var vnan = FloatVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = FloatVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = FloatVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector b = FloatVector.fromArray(loop.vs, array, p);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Float.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Float.NEGATIVE_INFINITY));
                    b.intoArray(array, p);
                }
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyFloat(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, float[] array) {
        var vnan = FloatVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = FloatVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = FloatVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector b = FloatVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Float.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Float.NEGATIVE_INFINITY));
                    b.intoArray(array, p, loop.simdOffsets(), 0);
                }
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyFloat(array[p]);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        var vnan = DoubleVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = DoubleVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = DoubleVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector b = DoubleVector.fromArray(loop.vs, array, p);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Double.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Double.NEGATIVE_INFINITY));
                    b.intoArray(array, p);
                }
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyDouble(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        var vnan = DoubleVector.broadcast(loop.vs, nan.floatValue());
        var vpinf = DoubleVector.broadcast(loop.vs, pinf.floatValue());
        var vninf = DoubleVector.broadcast(loop.vs, ninf.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector b = DoubleVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                if (!b.test(VectorOperators.IS_FINITE).allTrue()) {
                    b = b.blend(vnan, b.test(VectorOperators.IS_NAN));
                    b = b.blend(vpinf, b.compare(VectorOperators.EQ, Double.POSITIVE_INFINITY));
                    b = b.blend(vninf, b.compare(VectorOperators.EQ, Double.NEGATIVE_INFINITY));
                    b.intoArray(array, p, loop.simdOffsets(), 0);
                }
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyDouble(array[p]);
                p += loop.step;
            }
        }
    }
}
