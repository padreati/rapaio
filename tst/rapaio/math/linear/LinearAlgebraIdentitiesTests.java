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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class LinearAlgebraIdentitiesTests {

    private static final MType[] mTypes = new MType[] {
            MType.RDENSE, MType.CDENSE, MType.RSTRIPE, MType.CSTRIPE
    };

    DMatrix randomMatrix(MType type, int rows, int cols) {
        return DMatrix.random(type, rows, cols);
    }

    DVector randomVector(int size) {
        Random random = new Random(42);
        return DVector.from(size, row -> random.nextDouble());
    }

    @Test
    void additiveAssociationTest() {
        for (MType type1 : mTypes) {
            var a = randomMatrix(type1, 1000, 80);
            for (MType type2 : mTypes) {
                var b = randomMatrix(type2, 1000, 80);
                for(MType type3 : mTypes) {
                    var c = randomMatrix(type3, 80, 120);
                    assertTrue(a.dot(c).add(b.dot(c))
                            .deepEquals(a.copy().add(b).dot(c)));
                }

                var v = randomVector(80);
                var v1 = a.dot(v).add(b.dot(v));
                var v2 = a.copy().add(b).dot(v);
                assertTrue(v1.deepEquals(v2));
            }
        }
    }

}
