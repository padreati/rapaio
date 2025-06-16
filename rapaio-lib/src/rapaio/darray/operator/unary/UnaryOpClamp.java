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

import jdk.incubator.vector.VectorOperators;
import rapaio.darray.DType;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayUnaryOp;

public class UnaryOpClamp<N extends Number> extends DArrayUnaryOp {

    private final boolean hasMin;
    private final boolean hasMax;

    private final byte byteMin;
    private final byte byteMax;
    private final int intMin;
    private final int intMax;
    private final float floatMin;
    private final float floatMax;
    private final double doubleMin;
    private final double doubleMax;

    public UnaryOpClamp(DType<N> dtype, N min, N max) {
        super(false);
        hasMin = !dtype.isNaN(min);
        hasMax = !dtype.isNaN(max);

        byteMin = min.byteValue();
        byteMax = max.byteValue();
        intMin = min.intValue();
        intMax = max.intValue();
        floatMin = min.floatValue();
        floatMax = max.floatValue();
        doubleMin = min.doubleValue();
        doubleMax = max.doubleValue();
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getByteVector(p);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, byteMin);
                    a = a.blend(byteMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, byteMax);
                    a = a.blend(byteMax, m);
                }
                s.setByteVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getByte(p) < byteMin) {
                    s.setByte(p, byteMin);
                }
                if (hasMax && s.getByte(p) > byteMax) {
                    s.setByte(p, byteMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getByteVector(p, loop.simdIdx(), 0);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, byteMin);
                    a = a.blend(byteMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, byteMax);
                    a = a.blend(byteMax, m);
                }
                s.setByteVector(a, p, loop.simdIdx(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getByte(p) < byteMin) {
                    s.setByte(p, byteMin);
                }
                if (hasMax && s.getByte(p) > byteMax) {
                    s.setByte(p, byteMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (hasMin && s.getByte(p) < byteMin) {
                    s.setByte(p, byteMin);
                }
                if (hasMax && s.getByte(p) > byteMax) {
                    s.setByte(p, byteMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getIntVector(p);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, intMin);
                    a = a.blend(intMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, intMax);
                    a = a.blend(intMax, m);
                }
                s.setIntVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getInt(p) < intMin) {
                    s.setInt(p, intMin);
                }
                if (hasMax && s.getInt(p) > intMax) {
                    s.setInt(p, intMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getIntVector(p, loop.simdIdx(), 0);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, intMin);
                    a = a.blend(intMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, intMax);
                    a = a.blend(intMax, m);
                }
                s.setIntVector(a, p, loop.simdIdx(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getInt(p) < intMin) {
                    s.setInt(p, intMin);
                }
                if (hasMax && s.getInt(p) > intMax) {
                    s.setInt(p, intMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (hasMin && s.getInt(p) < intMin) {
                    s.setInt(p, intMin);
                }
                if (hasMax && s.getInt(p) > intMax) {
                    s.setInt(p, intMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getFloatVector(p);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, floatMin);
                    a = a.blend(floatMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, floatMax);
                    a = a.blend(floatMax, m);
                }
                s.setFloatVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getFloat(p) < floatMin) {
                    s.setFloat(p, floatMin);
                }
                if (hasMax && s.getFloat(p) > floatMax) {
                    s.setFloat(p, floatMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getFloatVector(p, loop.simdIdx(), 0);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, floatMin);
                    a = a.blend(floatMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, floatMax);
                    a = a.blend(floatMax, m);
                }
                s.setFloatVector(a, p, loop.simdIdx(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getFloat(p) < floatMin) {
                    s.setFloat(p, floatMin);
                }
                if (hasMax && s.getFloat(p) > floatMax) {
                    s.setFloat(p, floatMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (hasMin && s.getFloat(p) < floatMin) {
                    s.setFloat(p, floatMin);
                }
                if (hasMax && s.getFloat(p) > floatMax) {
                    s.setFloat(p, floatMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getDoubleVector(p);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, doubleMin);
                    a = a.blend(doubleMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, doubleMax);
                    a = a.blend(doubleMax, m);
                }
                s.setDoubleVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getDouble(p) < doubleMin) {
                    s.setDouble(p, doubleMin);
                }
                if (hasMax && s.getDouble(p) > doubleMax) {
                    s.setDouble(p, doubleMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getDoubleVector(p, loop.simdIdx(), 0);
                if (hasMin) {
                    var m = a.compare(VectorOperators.LT, doubleMin);
                    a = a.blend(doubleMin, m);
                }
                if (hasMax) {
                    var m = a.compare(VectorOperators.GT, doubleMax);
                    a = a.blend(doubleMax, m);
                }
                s.setDoubleVector(a, p, loop.simdIdx(), 0);
                p += loop.simdLen * loop.step;
            }
            for (; i < loop.bound; i++) {
                if (hasMin && s.getDouble(p) < doubleMin) {
                    s.setDouble(p, doubleMin);
                }
                if (hasMax && s.getDouble(p) > doubleMax) {
                    s.setDouble(p, doubleMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (hasMin && s.getDouble(p) < doubleMin) {
                    s.setDouble(p, doubleMin);
                }
                if (hasMax && s.getDouble(p) > doubleMax) {
                    s.setDouble(p, doubleMax);
                }
                p += loop.step;
            }
        }
    }
}
