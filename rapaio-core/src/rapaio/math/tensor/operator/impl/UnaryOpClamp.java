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

package rapaio.math.tensor.operator.impl;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.iterators.LoopDescriptor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public class UnaryOpClamp<N extends Number> extends TensorUnaryOp {

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
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public byte applyByte(byte v) {
        if (hasMin) {
            v = v < byteMin ? byteMin : v;
        }
        if (hasMax) {
            v = v > byteMax ? byteMax : v;
        }
        return v;
    }

    @Override
    public int applyInt(int v) {
        if (hasMin) {
            v = Math.max(v, intMin);
        }
        if (hasMax) {
            v = Math.min(v, intMax);
        }
        return v;
    }

    @Override
    public float applyFloat(float v) {
        if (hasMin) {
            v = Math.max(v, floatMin);
        }
        if (hasMax) {
            v = Math.min(v, floatMax);
        }
        return v;
    }

    @Override
    public double applyDouble(double v) {
        if (hasMin) {
            v = Math.max(v, doubleMin);
        }
        if (hasMax) {
            v = Math.min(v, doubleMax);
        }
        return v;
    }

    @Override
    protected void applyUnitByte(LoopDescriptor<Byte> loop, byte[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = (byte) Math.max(array[p], byteMin);
                }
                if (hasMax) {
                    array[p] = (byte) Math.min(array[p], byteMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepByte(LoopDescriptor<Byte> loop, byte[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = (byte) Math.max(array[p], byteMin);
                }
                if (hasMax) {
                    array[p] = (byte) Math.min(array[p], byteMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitInt(LoopDescriptor<Integer> loop, int[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = Math.max(array[p], intMin);
                }
                if (hasMax) {
                    array[p] = Math.min(array[p], intMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepInt(LoopDescriptor<Integer> loop, int[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = Math.max(array[p], intMin);
                }
                if (hasMax) {
                    array[p] = Math.min(array[p], intMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitFloat(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = Math.max(array[p], floatMin);
                }
                if (hasMax) {
                    array[p] = Math.min(array[p], floatMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = Math.max(array[p], floatMin);
                }
                if (hasMax) {
                    array[p] = Math.min(array[p], floatMax);
                }
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = Math.max(array[p], doubleMin);
                }
                if (hasMax) {
                    array[p] = Math.min(array[p], doubleMax);
                }
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                if (hasMin) {
                    array[p] = Math.max(array[p], doubleMin);
                }
                if (hasMax) {
                    array[p] = Math.min(array[p], doubleMax);
                }
                p += loop.step;
            }
        }
    }
}
