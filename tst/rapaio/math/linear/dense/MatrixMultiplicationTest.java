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

package rapaio.math.linear.dense;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.math.linear.RM;
import rapaio.util.Time;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MatrixMultiplicationTest {

    private static double TOL = 1e-12;

    @Test
    public void basicTestMM() {

        Normal normal = Normal.std();
        RM A = SolidRM.fill(100, 100, (r, c) -> normal.sampleNext());
        RM B = SolidRM.fill(100, 100, (r, c) -> normal.sampleNext());

        RM c1 = A.dot(B);
        RM c2 = MatrixMultiplication.ikjAlgorithm(A, B);

        assertTrue(c1.isEqual(c2));
        assertTrue(!c1.isEqual(c2.t()));
    }

    @Test
    public void testDifferentMethods() {

        RandomSource.setSeed(1234);

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

    @Test
    public void largeMatrices() {

        RandomSource.setSeed(1234);

        int N = 100;
        Normal norm = Normal.of(1, 12);

        System.out.println("create matrix A");
        RM A = Time.showRun(() -> SolidRM.fill(N, N, (r, c) -> norm.sampleNext()));

        System.out.println("create matrix B");
        RM B = Time.showRun(() -> SolidRM.fill(N, N, (r, c) -> norm.sampleNext()));

        System.out.println("...");

        Map<String, VarDouble> times = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {

            System.out.println("=========================");
            System.out.println("Iteration " + (i + 1));
            System.out.println("=========================");

            System.out.println("jama");
            put(times, "jama", Time.measure(() -> MatrixMultiplication.jama(A, B)));
            System.out.println("ijkAlgorithm");
            put(times, "ijkAlgorithm", Time.measure(() -> MatrixMultiplication.ijkAlgorithm(A, B)));
            System.out.println("ikjAlgorithm");
            put(times, "ikjAlgorithm", Time.measure(() -> MatrixMultiplication.ikjAlgorithm(A, B)));
            System.out.println("tiledAlgorithm");
            put(times, "tiledAlgorithm", Time.measure(() -> MatrixMultiplication.tiledAlgorithm(A, B)));
            System.out.println("ijkParallel");
            put(times, "ijkParallel", Time.measure(() -> MatrixMultiplication.ijkParallel(A, B)));
            System.out.println("ikjParallel");
            put(times, "ikjParallel", Time.measure(() -> MatrixMultiplication.ikjParallel(A, B)));
            System.out.println("strassen");
            put(times, "strassen", Time.measure(() -> MatrixMultiplication.strassen(A, B, 1024)));
        }

        SolidFrame.byVars(new ArrayList<>(times.values())).printSummary();
    }

    private void put(Map<String, VarDouble> map, String key, Long value) {
        if (!map.containsKey(key)) {
            map.put(key, VarDouble.empty().withName(key));
        }
        map.get(key).addDouble(value);
    }
}
