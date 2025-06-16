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
import rapaio.darray.operator.DArrayOp;
import rapaio.darray.operator.DArrayReduceOp;
import rapaio.data.OperationNotAvailableException;

public final class ReduceOpVarc extends DArrayReduceOp {

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
    protected byte reduceByteVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        float mean = Double.isFinite(initMean) ? (float) initMean : DArrayOp.reduceMean().reduceFloat(loop, storage);
        FloatVector vmean = Simd.broadcast(mean);

        float sum2 = 0;
        float sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            FloatVector vsum2 = Simd.zeroFloat();
            FloatVector vsum3 = Simd.zeroFloat();
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = storage.getFloatVector(p);
                FloatVector c = a.sub(vmean);
                FloatVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                float centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p++;
            }
        }
        int size = loop.bound * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor loop, Storage storage) {
        float mean = Double.isFinite(initMean) ? (float) initMean : DArrayOp.reduceMean().reduceFloat(loop, storage);
        FloatVector vmean = FloatVector.broadcast(Simd.vsFloat, mean);

        float sum2 = 0;
        float sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            FloatVector vsum2 = Simd.zeroFloat();
            FloatVector vsum3 = Simd.zeroFloat();
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = storage.getFloatVector(p, loop.simdIdx(), 0);
                FloatVector c = a.sub(vmean);
                FloatVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen * loop.step;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                float centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.bound * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor loop, Storage storage) {
        float mean = Double.isFinite(initMean) ? (float) initMean : DArrayOp.reduceMean().reduceFloat(loop, storage);
        float sum2 = 0;
        float sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.bound; i++) {
                float centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.bound * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        double mean = Double.isFinite(initMean) ? initMean : DArrayOp.reduceMean().reduceDouble(loop, storage);
        DoubleVector vmean = Simd.broadcast(mean);

        double sum2 = 0;
        double sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            DoubleVector vsum2 = Simd.zeroDouble();
            DoubleVector vsum3 = Simd.zeroDouble();
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = storage.getDoubleVector(p);
                DoubleVector c = a.sub(vmean);
                DoubleVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                double centered = storage.getFloat(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p++;
            }
        }
        int size = loop.bound * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor loop, Storage storage) {
        double mean = Double.isFinite(initMean) ? initMean : DArrayOp.reduceMean().reduceDouble(loop, storage);
        DoubleVector vmean = Simd.broadcast(mean);

        double sum2 = 0;
        double sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            DoubleVector vsum2 = Simd.zeroDouble();
            DoubleVector vsum3 = Simd.zeroDouble();
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = storage.getDoubleVector(p, loop.simdIdx(), 0);
                DoubleVector c = a.sub(vmean);
                DoubleVector c2 = c.mul(c);
                vsum2 = vsum2.add(c2);
                vsum3 = vsum3.add(c);
                p += loop.simdLen * loop.step;
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                double centered = storage.getDouble(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.bound * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor loop, Storage storage) {
        double mean = Double.isFinite(initMean) ? initMean : DArrayOp.reduceMean().reduceDouble(loop, storage);
        double sum2 = 0;
        double sum3 = 0;
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.bound; i++) {
                double centered = storage.getDouble(p) - mean;
                sum2 += centered * centered;
                sum3 += centered;
                p += loop.step;
            }
        }
        int size = loop.bound * loop.offsets.length;
        return ((sum2 - (sum3 * sum3) / (size-ddof)) / (size - ddof));
    }
}
