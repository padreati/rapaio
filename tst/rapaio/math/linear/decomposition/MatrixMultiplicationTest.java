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

package rapaio.math.linear.decomposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class MatrixMultiplicationTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(1234);
    }

    @Test
    void basicTestMM() {

        Normal normal = Normal.std();
        DMatrix A = DMatrix.fill(100, 100, (r, c) -> normal.sampleNext());
        DMatrix B = DMatrix.fill(100, 100, (r, c) -> normal.sampleNext());

        DMatrix c1 = A.dot(B);
        DMatrix c2 = MatrixMultiplication.ikjAlgorithm(A, B);

        assertTrue(c1.deepEquals(c2));
        assertFalse(c1.deepEquals(c2.t()));
    }

    @Test
    void testDifferentMethods() {

        Normal normal = Normal.std();
        DMatrix A = DMatrix.fill(100, 100, (r, c) -> normal.sampleNext());
        DMatrix B = DMatrix.fill(100, 100, (r, c) -> normal.sampleNext());

        DMatrix c = A.dot(B);

        assertTrue(c.deepEquals(MatrixMultiplication.ijkAlgorithm(A, B), TOL));
        assertTrue(c.deepEquals(MatrixMultiplication.ijkParallel(A, B), TOL));
        assertTrue(c.deepEquals(MatrixMultiplication.ikjAlgorithm(A, B), TOL));
        assertTrue(c.deepEquals(MatrixMultiplication.ikjParallel(A, B), TOL));
        assertTrue(c.deepEquals(MatrixMultiplication.tiledAlgorithm(A, B), TOL));
        assertTrue(c.deepEquals(MatrixMultiplication.jama(A, B), TOL));
        assertTrue(c.deepEquals(MatrixMultiplication.strassen(A, B, 8), TOL));
    }

    private void put(Map<String, VarDouble> map, String key, Long value) {
        if (!map.containsKey(key)) {
            map.put(key, VarDouble.empty().name(key));
        }
        map.get(key).addDouble(value);
    }
}
