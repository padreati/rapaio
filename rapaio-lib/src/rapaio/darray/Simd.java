/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.darray;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;

public final class Simd {

    public static final VectorSpecies<Byte> vsByte = ByteVector.SPECIES_PREFERRED;
    public static final VectorSpecies<Integer> vsInt = IntVector.SPECIES_PREFERRED;
    public static final VectorSpecies<Float> vsFloat = FloatVector.SPECIES_PREFERRED;
    public static final VectorSpecies<Double> vsDouble = DoubleVector.SPECIES_PREFERRED;

    public static ByteVector zeroByte() {
        return ByteVector.zero(vsByte);
    }

    public static ByteVector broadcast(byte value) {
        return ByteVector.broadcast(vsByte, value);
    }

    public static IntVector zeroInt() {
        return IntVector.zero(vsInt);
    }

    public static IntVector broadcast(int value) {
        return IntVector.broadcast(vsInt, value);
    }

    public static FloatVector zeroFloat() {
        return FloatVector.zero(vsFloat);
    }

    public static FloatVector broadcast(float value) {
        return FloatVector.broadcast(vsFloat, value);
    }

    public static DoubleVector zeroDouble() {
        return DoubleVector.zero(vsDouble);
    }

    public static DoubleVector broadcast(double value) {
        return DoubleVector.broadcast(vsDouble, value);
    }

    /**
     * Lanewise {@code t * a + c}. Floating point vectors use the fused operation (single rounding);
     * integer vectors have no fused variant and use a multiply followed by an add.
     */
    public static DoubleVector fma(DoubleVector t, DoubleVector a, DoubleVector c) {
        return t.fma(a, c);
    }

    public static FloatVector fma(FloatVector t, FloatVector a, FloatVector c) {
        return t.fma(a, c);
    }

    public static IntVector fma(IntVector t, IntVector a, IntVector c) {
        return t.mul(a).add(c);
    }

    public static ByteVector fma(ByteVector t, ByteVector a, ByteVector c) {
        return t.mul(a).add(c);
    }

    /**
     * Scalar counterparts of the lanewise {@code fma}, so that the scalar tail of a kernel produces exactly the
     * same values as its vector body: fused for floating point, wrapping multiply-add for integer types.
     */
    public static double fma(double t, double a, double c) {
        return Math.fma(t, a, c);
    }

    public static float fma(float t, float a, float c) {
        return Math.fma(t, a, c);
    }

    public static int fma(int t, int a, int c) {
        return t * a + c;
    }

    public static byte fma(byte t, byte a, byte c) {
        return (byte) (t * a + c);
    }
}
