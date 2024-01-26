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

package rapaio.math.tensor.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;

public class SVDecompositionTest {

    private static final double TOL = 1e-14;
    private static final int ROUNDS = 100;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(1234);
    }

    @Test
    void testAll() {
        testSuite(TensorManager.base().ofDouble());
    }

    <N extends Number> void testSuite(TensorManager.OfType<N> tmt) {
        testBuilder(tmt);
        testDimension(tmt);
        testConditionNumber(tmt);
        testProjectors(tmt);
    }

    <N extends Number> void testBuilder(TensorManager.OfType<N> tmt) {

        for (int round = 0; round < ROUNDS; round++) {

            int n = random.nextInt(5) + 1;
            int m = random.nextInt(5) + n;

            Tensor<N> a = tmt.random(Shape.of(m, n), random);

            SVDecomposition<N> svd = a.svd();

            Tensor<N> u = svd.u();
            Tensor<N> s = svd.s();
            Tensor<N> v = svd.v();

            assertTrue(a.deepEquals(u.mm(s).mm(v.t()), TOL));

            Tensor<N> sv = svd.singularValues();
            for (int i = 0; i < n - 1; i++) {
                assertEquals(s.getDouble(i, i), sv.getDouble(i), TOL);
                assertTrue(sv.getDouble(i) >= sv.getDouble(i + 1));
            }

            assertEquals(sv.getDouble(0), svd.norm2(), TOL);
            assertEquals(n, svd.rank());
        }
    }

    <N extends Number> void testDimension(TensorManager.OfType<N> tmt) {
        var e = assertThrows(IllegalArgumentException.class, () -> tmt.random(Shape.of(10, 50), random).svd());
        assertEquals("This SVD implementation only works for m >= n", e.getMessage());
    }

    <N extends Number> void testConditionNumber(TensorManager.OfType<N> tmt) {

        // for random matrices we expect a low condition number

        for (int i = 0; i < ROUNDS; i++) {
            var svd = tmt.random(Shape.of(10, 10), random).svd();
            double c = svd.conditionNumber();
            assertTrue(Math.log10(c) < 4);
            assertEquals(1 / c, svd.inverseConditionNumber(), 1e-12);
        }

        // for ill conditioned the condition number explodes

        Normal norm = Normal.of(0, 0.000001);

        for (int i = 0; i < ROUNDS; i++) {
            var a = tmt.random(Shape.of(10, 10), random);

            // we create the first column as a slightly modified
            // version of the second column, thus we have linearity
            for (int j = 0; j < 10; j++) {
                a.setDouble(a.getDouble(j, 1) + norm.sampleNext(), j, 0);
            }

            double c = a.svd().conditionNumber();
            assertTrue(Math.log10(c) > 5);
            assertEquals(1 / c, a.svd().inverseConditionNumber(), 1e-12);
        }
    }

    <N extends Number> void testProjectors(TensorManager.OfType<N> tmt) {
        for (int i = 0; i < ROUNDS; i++) {
            var v = tmt.random(Shape.of(3), random);
            var p = v.outer(v);
            assertEquals(1, p.svd().rank());
        }

        for (int i = 0; i < ROUNDS; i++) {
            var v = tmt.random(Shape.of(3), random);
            var p = v.outer(v);
            v = tmt.random(Shape.of(3), random);
            p.add_(v.outer(v));
            var svd = p.svd();
            assertEquals(2, svd.rank());
        }
    }
}
