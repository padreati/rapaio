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
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayUnaryOp;
import rapaio.data.OperationNotAvailableException;


// This code is generated automatically

public class UnaryOpExpm1 extends DArrayUnaryOp {

    public UnaryOpExpm1() {
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
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = s.getFloatVector(p);
                a = a.lanewise(VectorOperators.EXPM1);
                s.setFloatVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                float a = s.getFloat(p);
                a = (float) Math.expm1(a);
                s.setFloat(p, a);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = s.getFloatVector(p, loop.simdOffsets(), 0);
                a = a.lanewise(VectorOperators.EXPM1);
                s.setFloatVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                float a = s.getFloat(p);
                a = (float) Math.expm1(a);
                s.setFloat(p, a);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                float a = s.getFloat(p);
                a = (float) Math.expm1(a);
                s.setFloat(p, a);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = s.getDoubleVector(p);
                a = a.lanewise(VectorOperators.EXPM1);
                s.setDoubleVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                double a = s.getDouble(p);
                a = Math.expm1(a);
                s.setDouble(p, a);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = s.getDoubleVector(p, loop.simdOffsets(), 0);
                a = a.lanewise(VectorOperators.EXPM1);
                s.setDoubleVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                double a = s.getDouble(p);
                a = Math.expm1(a);
                s.setDouble(p, a);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                double a = s.getDouble(p);
                a = Math.expm1(a);
                s.setDouble(p, a);
                p += loop.step;
            }
        }
    }
}