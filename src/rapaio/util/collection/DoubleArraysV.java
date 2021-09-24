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

package rapaio.util.collection;

import static jdk.incubator.vector.VectorOperators.ADD;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

public final class DoubleArraysV {

    public static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    public static final int SPECIES_LEN = SPECIES.length();

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

    public static double sum(double[] x, int xOff, int len) {
        int loopBound = SPECIES.loopBound(len) + xOff;
        int i = xOff;
        var vsum = DoubleVector.zero(SPECIES);
        for (; i < loopBound; i += SPECIES_LEN) {
            var vx = DoubleVector.fromArray(SPECIES, x, i);
            vsum = vsum.add(vx);
        }
        int xLen = xOff + len;
        double sum = vsum.reduceLanes(ADD);
        for (; i < xLen; i++) {
            sum += x[i];
        }
        return sum;
    }

    /**
     * Adds one vector values to another vector.
     *
     * @param x    vector to be added from
     * @param xOff starting position on vector to be added from
     * @param y    vector to be added to in place
     * @param yOff starting position of the vector to be added to
     * @param len  length of the values
     */
    public static void add(double[] x, int xOff, double[] y, int yOff, int len) {
        int loopBound = SPECIES.loopBound(len) + xOff;
        int i = xOff;
        int delta = yOff - xOff;
        for (; i < loopBound; i += SPECIES_LEN) {
            var vx = DoubleVector.fromArray(SPECIES, x, i);
            var vy = DoubleVector.fromArray(SPECIES, y, i + delta);
            vx.add(vy).intoArray(y, i + delta);
        }
        for (; i < len + xOff; i++) {
            y[i + delta] += x[i];
        }
    }

    public static void add(double[] x, double[] y) {
        int len = Math.min(x.length, y.length);
        int loopBound = SPECIES.loopBound(len);
        int i = 0;
        for (; i < loopBound; i += SPECIES_LEN) {
            var vx = DoubleVector.fromArray(SPECIES, x, i);
            var vy = DoubleVector.fromArray(SPECIES, y, i);
            vx.add(vy).intoArray(y, i);
        }
        for (; i < len; i++) {
            y[i] += x[i];
        }
    }

    public static void accAXPY(double[] to, double[] from, int pos, int len, double factor) {
        int i = 0;
        int loopBound = SPECIES.loopBound(len);
        var bv = DoubleVector.broadcast(SPECIES, factor);
        for (; i < loopBound; i += SPECIES.length()) {
            var vc = DoubleVector.fromArray(SPECIES, from, pos + i);
            var vs = DoubleVector.fromArray(SPECIES, to, i);
            vc.fma(bv, vs).intoArray(to, i);
        }
        for (; i < len; i++) {
            to[i] += from[pos + i] * factor;
        }
    }
}
