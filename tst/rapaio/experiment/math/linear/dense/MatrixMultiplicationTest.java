/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.experiment.math.linear.dense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.experiment.math.linear.RM;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class MatrixMultiplicationTest {

    private static double TOL = 1e-12;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(1234);
    }

    @Test
    void basicTestMM() {

        Normal normal = Normal.std();
        RM A = SolidRM.fill(100, 100, (r, c) -> normal.sampleNext());
        RM B = SolidRM.fill(100, 100, (r, c) -> normal.sampleNext());

        RM c1 = A.dot(B);
        RM c2 = MatrixMultiplication.ikjAlgorithm(A, B);

        assertTrue(c1.isEqual(c2));
        assertFalse(c1.isEqual(c2.t()));
    }

    @Test
    void testDifferentMethods() {

        Normal normal = Normal.std();
        RM A = SolidRM.fill(100, 100, (r, c) -> normal.sampleNext());
        RM B = SolidRM.fill(100, 100, (r, c) -> normal.sampleNext());

        RM c = A.dot(B);

        assertTrue(c.isEqual(MatrixMultiplication.ijkAlgorithm(A, B), TOL));
        assertTrue(c.isEqual(MatrixMultiplication.ijkParallel(A, B), TOL));
        assertTrue(c.isEqual(MatrixMultiplication.ikjAlgorithm(A, B), TOL));
        assertTrue(c.isEqual(MatrixMultiplication.ikjParallel(A, B), TOL));
        assertTrue(c.isEqual(MatrixMultiplication.tiledAlgorithm(A, B), TOL));
        assertTrue(c.isEqual(MatrixMultiplication.jama(A, B), TOL));
        assertTrue(c.isEqual(MatrixMultiplication.strassen(A, B, 8), TOL));
    }

    private void put(Map<String, VarDouble> map, String key, Long value) {
        if (!map.containsKey(key)) {
            map.put(key, VarDouble.empty().withName(key));
        }
        map.get(key).addDouble(value);
    }
}
