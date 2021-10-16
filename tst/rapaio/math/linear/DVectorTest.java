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

package rapaio.math.linear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.math.linear.Algebra.*;

import org.junit.jupiter.api.Test;

import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorMap;
import rapaio.math.linear.dense.DVectorStride;

public class DVectorTest {

    private static final VType[] types = new VType[] {VType.DENSE, VType.STRIDE, VType.MAP};

    public DVector newSeq(VType type) {
        switch (type) {
            case DENSE -> {
                double[] values = new double[110];
                for (int i = 0; i < 100; i++) {
                    values[i + 10] = i;
                }
                return new DVectorDense(10, 100, values);
            }
            case STRIDE -> {
                double[] values = new double[210];
                for (int i = 0; i < 100; i++) {
                    values[10 + i * 2] = i;
                }
                return new DVectorStride(10, 100, 2, values);
            }
            case MAP -> {
                double[] values = new double[210];
                int[] indexes = new int[100];
                for (int i = 0; i < 100; i++) {
                    values[10 + i * 2] = i;
                    indexes[i] = 10 + i * 2;
                }
                return new DVectorMap(new DVectorDense(0, 210, values), indexes);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Test
    void testScalarOperations() {

        for (VType type1 : types) {
            DVector v1 = newSeq(type1);
            assertTrue(v1.add(10, copy()).deepEquals(v1.add(10)));
            assertTrue(v1.sub(10, copy()).deepEquals(v1.sub(10)));
            assertTrue(v1.mul(10, copy()).deepEquals(v1.mul(10)));
            assertTrue(v1.div(10, copy()).deepEquals(v1.div(10)));

            assertEquals(v1.pnorm(1), v1.sum());
            assertEquals(v1.pnorm(Double.POSITIVE_INFINITY), 99);
        }
    }

    @Test
    void testBinaryOperations() {
        for(VType type1 : types) {
            for(VType type2 : types) {
                DVector v1 = newSeq(type1);
                DVector v2 = newSeq(type2);

                String msg = String.format("type1: %s, type2: %s", type1.name(), type2.name());
                assertTrue(v1.add(v2, copy()).deepEquals(v1.add(v2)), msg);
                assertTrue(v1.sub(v2, copy()).deepEquals(v1.sub(v2)), msg);
                assertTrue(v1.mul(v2, copy()).deepEquals(v1.mul(v2)), msg);
                assertTrue(v1.div(v2, copy()).deepEquals(v1.div(v2)), msg);

                v1 = newSeq(type1);
                v2 = newSeq(type2);
                assertTrue(v1.addMul(10, v2, copy()).deepEquals(v1.addMul(10, v2)), msg);

                v1 = newSeq(type1);
                v2 = newSeq(type2);

                assertEquals(328350.0, v1.dot(v2));
                assertEquals(328350.0, v1.dotBilinear(DMatrix.identity(100), v2));
                assertEquals(328350.0, v1.dotBilinear(DMatrix.identity(100)));
                assertEquals(328350.0, v1.dotBilinearDiag(DMatrix.identity(100), v2));
                assertEquals(328350.0, v1.dotBilinearDiag(DVector.ones(100)));
            }
        }
    }
}
