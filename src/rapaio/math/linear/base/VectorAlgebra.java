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

package rapaio.math.linear.base;


import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

/**
 * Utility class to handle the manipulation of arrays of double 64 floating point values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
public final class VectorAlgebra {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final int SPECIES_LEN = SPECIES.length();

    public static void binaryOpTo(DOperators.Binary op, double[] x, int xOff, double s, double[] t, int tOff, int len) {
        int bound = SPECIES.loopBound(len);
        int i = 0;
        var sv = DoubleVector.broadcast(SPECIES, s);
        for (; i < bound; i += SPECIES_LEN) {
            var tv = DoubleVector.fromArray(SPECIES, x, xOff + i);
            tv.lanewise(op.op(), sv).intoArray(t, tOff + i);
        }
        for (; i < len; i++) {
            t[tOff + i] = op.apply(x[xOff + i], s);
        }
    }

    public static void binaryOpTo(DOperators.Binary op, double[] x, int xOff, double[] y, int yOff, double[] t, int tOff, int len) {
        int bound = SPECIES.loopBound(len);
        int i = 0;
        for (; i < bound; i += SPECIES_LEN) {
            var xv = DoubleVector.fromArray(SPECIES, x, xOff + i);
            var yv = DoubleVector.fromArray(SPECIES, y, yOff + i);
            xv.lanewise(op.op(), yv).intoArray(t, tOff + i);
        }
        for (; i < len; i++) {
            t[tOff + i] = op.apply(x[xOff + i], y[yOff + i]);
        }
    }
}
