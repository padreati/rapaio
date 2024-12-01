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

package rapaio.narray.operator.impl;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.data.OperationNotAvailableException;
import rapaio.narray.Storage;
import rapaio.narray.iterators.StrideLoopDescriptor;
import rapaio.narray.operator.NArrayUnaryOp;

public class UnaryOpSoftmax extends NArrayUnaryOp {

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
        FloatVector vmax = FloatVector.broadcast(loop.vs, max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getFloatVector(loop.vs, p));
                p += loop.simdLen;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.size; i++) {
                max = Math.max(max, s.getFloat(p));
                p++;
            }
        }
        vmax = FloatVector.broadcast(loop.vs, max);
        float sum = 0;
        FloatVector vsum = FloatVector.zero(loop.vs);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(loop.vs, p);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setFloatVector(v, p);
                p += loop.simdLen;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                float v = s.getFloat(p);
                v = (float) Math.exp(v - max);
                sum += v;
                s.setFloat(p, v);
                p++;
            }
        }
        vsum = FloatVector.broadcast(loop.vs, sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(loop.vs, p);
                s.setFloatVector(v.div(vsum), p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setFloat(p, s.getFloat(p) / sum);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float max = Float.NEGATIVE_INFINITY;
        FloatVector vmax = FloatVector.broadcast(loop.vs, max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getFloatVector(loop.vs, p, loop.simdOffsets(), 0));
                p += loop.simdLen * loop.step;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.size; i++) {
                max = Math.max(max, s.getFloat(p));
                p += loop.step;
            }
        }
        vmax = FloatVector.broadcast(loop.vs, max);
        float sum = 0;
        FloatVector vsum = FloatVector.zero(loop.vs);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(loop.vs, p, loop.simdOffsets(), 0);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setFloatVector(v, p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                float v = s.getFloat(p);
                v = (float) Math.exp(v - max);
                sum += v;
                s.setFloat(p, v);
                p += loop.step;
            }
        }
        vsum = FloatVector.broadcast(loop.vs, sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = s.getFloatVector(loop.vs, p, loop.simdOffsets(), 0);
                s.setFloatVector(v.div(vsum), p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.size; i++) {
                s.setFloat(p, s.getFloat(p) / sum);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float max = Float.NEGATIVE_INFINITY;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                max = Math.max(max, s.getFloat(p));
                p += loop.step;
            }
        }
        float sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                float v = s.getFloat(p);
                v = (float) Math.exp(v - max);
                sum += v;
                s.setFloat(p, v);
                p += loop.step;
            }
        }
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setFloat(p, s.getFloat(p) / sum);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double max = Double.NEGATIVE_INFINITY;
        DoubleVector vmax = DoubleVector.broadcast(loop.vs, max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getDoubleVector(loop.vs, p));
                p += loop.simdLen;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.size; i++) {
                max = Math.max(max, s.getDouble(p));
                p++;
            }
        }
        vmax = DoubleVector.broadcast(loop.vs, max);
        double sum = 0;
        DoubleVector vsum = DoubleVector.zero(loop.vs);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(loop.vs, p);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setDoubleVector(v, p);
                p += loop.simdLen;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                double v = s.getDouble(p);
                v = Math.exp(v - max);
                sum += v;
                s.setDouble(p, v);
                p++;
            }
        }
        vsum = DoubleVector.broadcast(loop.vs, sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(loop.vs, p);
                s.setDoubleVector(v.div(vsum), p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setDouble(p, s.getDouble(p) / sum);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double max = Double.NEGATIVE_INFINITY;
        DoubleVector vmax = DoubleVector.broadcast(loop.vs, max);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                vmax = vmax.max(s.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0));
                p += loop.simdLen * loop.step;
            }
            max = Math.max(max, vmax.reduceLanes(VectorOperators.MAX));
            for (; i < loop.size; i++) {
                max = Math.max(max, s.getDouble(p));
                p += loop.step;
            }
        }
        vmax = DoubleVector.broadcast(loop.vs, max);
        double sum = 0;
        DoubleVector vsum = DoubleVector.zero(loop.vs);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0);
                v = v.sub(vmax).lanewise(VectorOperators.EXP);
                vsum = vsum.add(v);
                s.setDoubleVector(v, p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                double v = s.getDouble(p);
                v = Math.exp(v - max);
                sum += v;
                s.setDouble(p, v);
                p += loop.step;
            }
        }
        vsum = DoubleVector.broadcast(loop.vs, sum);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = s.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0);
                s.setDoubleVector(v.div(vsum), p, loop.simdOffsets(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.size; i++) {
                s.setDouble(p, s.getDouble(p) / sum);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double max = Double.NEGATIVE_INFINITY;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                max = Math.max(max, s.getDouble(p));
                p += loop.step;
            }
        }
        double sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double v = s.getDouble(p);
                v = Math.exp(v - max);
                sum += v;
                s.setDouble(p, v);
                p += loop.step;
            }
        }
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setDouble(p, s.getDouble(p) / sum);
                p += loop.step;
            }
        }
    }
}
