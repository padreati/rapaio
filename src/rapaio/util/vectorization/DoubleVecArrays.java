/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.util.vectorization;


import static jdk.incubator.vector.VectorOperators.ADD;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

/**
 * Utility class to handle the manipulation of arrays of double 64 floating point values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
public final class DoubleVecArrays {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final int SPECIES_LEN = SPECIES.length();

    public static void binaryOp(DoubleBinaryOp op, double[] t, int tOff, double s, int len) {
        binaryOpTo(op, t, tOff, s, t, tOff, len);
    }

    /**
     * Adds a scalar value to elements of an array (vectorized version).
     *
     * @param t    destination where the scalar will be added
     * @param tOff destination offset
     * @param s    scalar value to be added
     * @param len  length
     */
    public static void add(double[] t, int tOff, double s, int len) {
        binaryOp(DoubleBinaryOp.ADD, t, tOff, s, len);
    }

    public static void binaryOpTo(DoubleBinaryOp op, double[] x, int xOff, double s, double[] to, int toOff, int len) {
        int bound = SPECIES.loopBound(len);
        int i = 0;
        var sv = DoubleVector.broadcast(SPECIES, s);
        for (; i < bound; i += SPECIES_LEN) {
            var tv = DoubleVector.fromArray(SPECIES, x, xOff + i);
            tv.lanewise(op.operator(), sv).intoArray(to, toOff + i);
        }
        for (; i < toOff + len; i++) {
            to[toOff + i] = op.apply(x[xOff + i], s);
        }
    }

    /**
     * Adds a scalar value to elements of an array and store the result into another array (vectorized).
     *
     * @param x     first operand which is a vector
     * @param xOff  offset of the first operand
     * @param s     second operand which is a scalar value
     * @param to    array where to store the results
     * @param toOff offset of the array where to store results
     * @param len   number of elements to be processed
     */
    public static void addTo(double[] x, int xOff, double s, double[] to, int toOff, int len) {
        binaryOpTo(DoubleBinaryOp.ADD, x, xOff, s, to, toOff, len);
    }

    /**
     * Add multiple of a vector. The equation of the operation is x <- x + a * y
     *
     * @param x
     * @param xOff
     * @param a
     * @param y
     * @param yOff
     * @param len
     */
    public static void addMul(double[] x, int xOff, double a, double[] y, int yOff, int len) {
        int bound = SPECIES.loopBound(len);
        int i = 0;
        DoubleVector av = DoubleVector.broadcast(SPECIES, a);
        for (; i < bound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, x, i + xOff);
            DoubleVector yv = DoubleVector.fromArray(SPECIES, y, i + yOff);
            yv.fma(av, xv).intoArray(x, i + xOff);
        }
        for (; i < len; i++) {
            x[xOff + i] += a * y[yOff + i];
        }
    }

    public static double dotSum(double[] x, int xOff, double[] y, int yOff, int len) {
        int loopBound = SPECIES.loopBound(len) + xOff;
        int i = xOff;
        int delta = yOff - xOff;
        var vsum = DoubleVector.zero(SPECIES);
        for (; i < loopBound; i += SPECIES_LEN) {
            var vx = DoubleVector.fromArray(SPECIES, x, i);
            var vy = DoubleVector.fromArray(SPECIES, y, i + delta);
            vsum = vx.fma(vy, vsum);
        }
        double sum = vsum.reduceLanes(ADD);
        int xLen = len + xOff;
        for (; i < xLen; i++) {
            sum += x[i] * y[i + delta];
        }
        return sum;
    }

    /**
     * Computes sum of all elements from array starting at position {@code start}
     * with given {@code length).
     *
     * @param a     vector of values
     * @param start first element
     * @param len   number of elements to be summed
     * @return sum of elements from specified range
     */
    public static double nanSum(double[] a, int start, int len) {
        int bound = SPECIES.loopBound(len);
        int i = start;
        DoubleVector sv = DoubleVector.broadcast(SPECIES, 0.0);
        for (; i < bound + start; i += SPECIES_LEN) {
            DoubleVector av = DoubleVector.fromArray(SPECIES, a, i);
            VectorMask<Double> mask = av.test(VectorOperators.IS_FINITE);
            sv = sv.add(av, mask);
        }
        double sum = sv.reduceLanes(ADD);
        for (; i < len + start; i++) {
            if (Double.isNaN(a[i])) {
                continue;
            }
            sum += a[i];
        }
        return sum;
    }
}
