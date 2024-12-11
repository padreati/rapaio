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
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayUnaryOp;
import rapaio.data.OperationNotAvailableException;

public class UnaryOpSoftmax extends DArrayUnaryOp {

    public UnaryOpSoftmax() {
        super(true);
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float max = Float.NEGATIVE_INFINITY;
        FloatVector vmax = Simd.broadcast(max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getFloatVector(p));
                p += loop.simdLen;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                max = Math.max(max, s.getFloat(p));
                p++;
            }
        }
        vmax = Simd.broadcast(max);
        float sum = 0;
        FloatVector vsum = Simd.zeroFloat();
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(p);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setFloatVector(v, p);
                p += loop.simdLen;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                float v = s.getFloat(p);
                v = (float) Math.exp(v - max);
                sum += v;
                s.setFloat(p, v);
                p++;
            }
        }
        vsum = Simd.broadcast(sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(p);
                s.setFloatVector(v.div(vsum), p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                s.setFloat(p, s.getFloat(p) / sum);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float max = Float.NEGATIVE_INFINITY;
        FloatVector vmax = Simd.broadcast(max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getFloatVector(p, loop.simdOffsets(), 0));
                p += loop.simdLen * loop.step;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                max = Math.max(max, s.getFloat(p));
                p += loop.step;
            }
        }
        vmax = Simd.broadcast(max);
        float sum = 0;
        FloatVector vsum = Simd.zeroFloat();
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(p, loop.simdOffsets(), 0);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setFloatVector(v, p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                float v = s.getFloat(p);
                v = (float) Math.exp(v - max);
                sum += v;
                s.setFloat(p, v);
                p += loop.step;
            }
        }
        vsum = Simd.broadcast(sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(p, loop.simdOffsets(), 0);
                s.setFloatVector(v.div(vsum), p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.bound; i++) {
                s.setFloat(p, s.getFloat(p) / sum);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float max = Float.NEGATIVE_INFINITY;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                max = Math.max(max, s.getFloat(p));
                p += loop.step;
            }
        }
        float sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                float v = s.getFloat(p);
                v = (float) Math.exp(v - max);
                sum += v;
                s.setFloat(p, v);
                p += loop.step;
            }
        }
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                s.setFloat(p, s.getFloat(p) / sum);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double max = Double.NEGATIVE_INFINITY;
        DoubleVector vmax = Simd.broadcast(max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getDoubleVector(p));
                p += loop.simdLen;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                max = Math.max(max, s.getDouble(p));
                p++;
            }
        }
        vmax = Simd.broadcast(max);
        double sum = 0;
        DoubleVector vsum = Simd.zeroDouble();
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(p);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setDoubleVector(v, p);
                p += loop.simdLen;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                double v = s.getDouble(p);
                v = Math.exp(v - max);
                sum += v;
                s.setDouble(p, v);
                p++;
            }
        }
        vsum = Simd.broadcast(sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(p);
                s.setDoubleVector(v.div(vsum), p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                s.setDouble(p, s.getDouble(p) / sum);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double max = Double.NEGATIVE_INFINITY;
        DoubleVector vmax = Simd.broadcast(max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getDoubleVector(p, loop.simdOffsets(), 0));
                p += loop.simdLen * loop.step;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                max = Math.max(max, s.getDouble(p));
                p += loop.step;
            }
        }
        vmax = Simd.broadcast(max);
        double sum = 0;
        DoubleVector vsum = Simd.zeroDouble();
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(p, loop.simdOffsets(), 0);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setDoubleVector(v, p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                double v = s.getDouble(p);
                v = Math.exp(v - max);
                sum += v;
                s.setDouble(p, v);
                p += loop.step;
            }
        }
        vsum = Simd.broadcast(sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(p, loop.simdOffsets(), 0);
                s.setDoubleVector(v.div(vsum), p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.bound; i++) {
                s.setDouble(p, s.getDouble(p) / sum);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double max = Double.NEGATIVE_INFINITY;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                max = Math.max(max, s.getDouble(p));
                p += loop.step;
            }
        }
        double sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                double v = s.getDouble(p);
                v = Math.exp(v - max);
                sum += v;
                s.setDouble(p, v);
                p += loop.step;
            }
        }
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                s.setDouble(p, s.getDouble(p) / sum);
                p += loop.step;
            }
        }
    }
}
