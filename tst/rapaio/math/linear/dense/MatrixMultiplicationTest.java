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

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.MatrixMultiplication;
import rapaio.math.linear.dense.SolidRM;
import rapaio.util.Util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class MatrixMultiplicationTest {

    private static double TOL = 1e-12;

    @Test
    public void basicTestMM() {

        Normal normal = new Normal();
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

        Normal normal = new Normal();
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
        Normal norm = new Normal(1, 12);

        System.out.println("create matrix A");
        RM A = Util.measure(() -> SolidRM.fill(N, N, (r, c) -> norm.sampleNext()))._1;

        System.out.println("create matrix B");
        RM B = Util.measure(() -> SolidRM.fill(N, N, (r, c) -> norm.sampleNext()))._1;

        System.out.println("...");

        Map<String, Numeric> times = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {

            System.out.println("=========================");
            System.out.println("Iteration " + (i + 1));
            System.out.println("=========================");

            System.out.println("jama");
            put(times, "jama", Util.measure(() -> MatrixMultiplication.jama(A, B))._2);
            System.out.println("ijkAlgorithm");
            put(times, "ijkAlgorithm", Util.measure(() -> MatrixMultiplication.ijkAlgorithm(A, B))._2);
            System.out.println("ikjAlgorithm");
            put(times, "ikjAlgorithm", Util.measure(() -> MatrixMultiplication.ikjAlgorithm(A, B))._2);
            System.out.println("tiledAlgorithm");
            put(times, "tiledAlgorithm", Util.measure(() -> MatrixMultiplication.tiledAlgorithm(A, B))._2);
            System.out.println("ijkParallel");
            put(times, "ijkParallel", Util.measure(() -> MatrixMultiplication.ijkParallel(A, B))._2);
            System.out.println("ikjParallel");
            put(times, "ikjParallel", Util.measure(() -> MatrixMultiplication.ikjParallel(A, B))._2);
            System.out.println("strassen");
            put(times, "strassen", Util.measure(() -> MatrixMultiplication.strassen(A, B, 1024))._2);
        }

        SolidFrame.byVars(new ArrayList<>(times.values())).printSummary();
    }

    private void put(Map<String, Numeric> map, String key, Long value) {
        if (!map.containsKey(key)) {
            map.put(key, Numeric.empty().withName(key));
        }
        map.get(key).addValue(value);
    }
}
