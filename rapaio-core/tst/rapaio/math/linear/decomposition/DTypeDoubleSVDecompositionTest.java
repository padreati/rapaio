/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.linear.decomposition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DVectorDense;

public class DTypeDoubleSVDecompositionTest {

    private static final double TOL = 1e-14;
    private static final int ROUNDS = 100;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(1234);
    }

    @Test
    void testBuilder() {

        for (int round = 0; round < ROUNDS; round++) {

            int n = random.nextInt(5) + 1;
            int m = random.nextInt(5) + n;

            DMatrix a = DMatrix.random(m, n);

            DoubleSVDecomposition svd = a.svd();

            DMatrix u = svd.u();
            DMatrix s = svd.s();
            DMatrix v = svd.v();

            assertTrue(a.deepEquals(u.dot(s).dot(v.t()), TOL));

            DVector sv = svd.singularValues();
            for (int i = 0; i < n - 1; i++) {
                assertEquals(s.get(i, i), sv.get(i), TOL);
                assertTrue(sv.get(i) >= sv.get(i + 1));
            }

            assertEquals(sv.get(0), svd.norm2(), TOL);
            assertEquals(n, svd.rank());
        }
    }

    @Test
    void testDimension() {
        assertThrows(IllegalArgumentException.class, () -> DMatrix.random(random, 10, 50).svd());
    }

    @Test
    void conditionNumberTest() {

        // for random matrices we expect a low condition number

        for (int i = 0; i < ROUNDS; i++) {
            var svd = DMatrix.random(random, 10, 10).svd();
            double c = svd.conditionNumber();
            assertTrue(Math.log10(c) < 4);
            assertEquals(1 / c, svd.inverseConditionNumber(), 1e-12);
        }

        // for ill conditioned the condition number explodes

        Normal norm = Normal.of(0, 0.000001);

        for (int i = 0; i < ROUNDS; i++) {
            DMatrix a = DMatrix.random(random, 10, 10);

            // we create the first column as a slightly modified
            // version of the second column, thus we have linearity
            for (int j = 0; j < 10; j++) {
                a.set(j, 0, a.get(j, 1) + norm.sampleNext());
            }

            double c = a.svd().conditionNumber();
            assertTrue(Math.log10(c) > 5);
            assertEquals(1 / c, a.svd().inverseConditionNumber(), 1e-12);
        }
    }

    @Test
    void testProjectors() {
        for (int i = 0; i < ROUNDS; i++) {
            DVector v = DVectorDense.random(random, 3);
            DMatrix p = v.outer(v);
            assertEquals(1, p.svd().rank());
        }

        for (int i = 0; i < ROUNDS; i++) {
            DVector v = DVectorDense.random(random, 3);
            DMatrix p = v.outer(v);
            v = DVectorDense.random(random, 3);
            p.add(v.outer(v));
            var svd = p.svd();
            assertEquals(2, svd.rank());
        }
    }
}
