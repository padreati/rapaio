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
import rapaio.narray.operator.NArrayOp;
import rapaio.narray.operator.NArrayReduceOp;

public final class ReduceOpVarc extends NArrayReduceOp {

    private final int ddof;
    private final double initMean;

    public ReduceOpVarc(int ddof, double initMean) {
        this.ddof = ddof;
        this.initMean = initMean;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor<Byte> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor<Byte> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor<Byte> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor<Integer> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor<Integer> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor<Integer> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor<Float> loop, Storage storage) {
        float mean = Double.isFinite(initMean) ? (float) initMean : NArrayOp.reduceMean().reduceFloat(loop, storage);
        FloatVector vmean = FloatVector.broadcast(loop.vs, mean);

        float sum2 = 0;
        float sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            FloatVector vsum2 = FloatVector.zero(loop.vs);
            FloatVector vsum3 = FloatVector.zero(loop.vs);
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = storage.getFloatVector(loop.vs, p);
                FloatVector c = a.sub(vmean);
                FloatVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                float centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p++;
            }
        }
        int size = loop.size * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor<Float> loop, Storage storage) {
        float mean = Double.isFinite(initMean) ? (float) initMean : NArrayOp.reduceMean().reduceFloat(loop, storage);
        FloatVector vmean = FloatVector.broadcast(loop.vs, mean);

        float sum2 = 0;
        float sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            FloatVector vsum2 = FloatVector.zero(loop.vs);
            FloatVector vsum3 = FloatVector.zero(loop.vs);
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = storage.getFloatVector(loop.vs, p, loop.simdOffsets(), 0);
                FloatVector c = a.sub(vmean);
                FloatVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen * loop.step;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                float centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.size * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor<Float> loop, Storage storage) {
        float mean = Double.isFinite(initMean) ? (float) initMean : NArrayOp.reduceMean().reduceFloat(loop, storage);
        float sum2 = 0;
        float sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                float centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.size * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor<Double> loop, Storage storage) {
        double mean = Double.isFinite(initMean) ? initMean : NArrayOp.reduceMean().reduceDouble(loop, storage);
        DoubleVector vmean = DoubleVector.broadcast(loop.vs, mean);

        double sum2 = 0;
        double sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            DoubleVector vsum2 = DoubleVector.broadcast(loop.vs, 0);
            DoubleVector vsum3 = DoubleVector.broadcast(loop.vs, 0);
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = storage.getDoubleVector(loop.vs, p);
                DoubleVector c = a.sub(vmean);
                DoubleVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                double centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p++;
            }
        }
        int size = loop.size * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor<Double> loop, Storage storage) {
        double mean = Double.isFinite(initMean) ? initMean : NArrayOp.reduceMean().reduceDouble(loop, storage);
        DoubleVector vmean = DoubleVector.broadcast(loop.vs, mean);

        double sum2 = 0;
        double sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            DoubleVector vsum2 = DoubleVector.broadcast(loop.vs, 0);
            DoubleVector vsum3 = DoubleVector.broadcast(loop.vs, 0);
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = storage.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0);
                DoubleVector c = a.sub(vmean);
                DoubleVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen * loop.step;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.size; i++) {
                double centered = storage.getDouble(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.size * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor<Double> loop, Storage storage) {
        double mean = Double.isFinite(initMean) ? initMean : NArrayOp.reduceMean().reduceDouble(loop, storage);
        double sum2 = 0;
        double sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                double centered = storage.getDouble(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.size * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }
}
