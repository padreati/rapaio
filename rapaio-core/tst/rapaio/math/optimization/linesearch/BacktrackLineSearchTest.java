/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.math.optimization.linesearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.math.optimization.functions.RDerivative;
import rapaio.math.optimization.functions.RFunction;
import rapaio.narray.NArray;
import rapaio.narray.NArrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/31/21.
 */
public class BacktrackLineSearchTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void validationTest() {
        assertThrows(IllegalArgumentException.class, () -> BacktrackLineSearch.newSearch().alpha.set(0.0));
    }

    @Test
    void smokeSquaredTest() {

        // here we test f(x) = x^2
        // after calculations for steepest descent alpha have a constant value
        RFunction f = (NArray<Double> x) -> x.getDouble(0) * x.getDouble(0);
        RDerivative df = (NArray<Double> x) -> x.copy().mul(2.0);

        for (int i = 0; i < 1_000; i++) {
            double next = (random.nextDouble() - 0.5) * 100;
            NArray<Double> x0 = NArrays.stride(next);
            NArray<Double> p = df.apply(x0).mul(-1.);
            double t = BacktrackLineSearch.newSearch().search(f, df, x0, p);
            double fx0 = f.apply(x0);
            double fx1 = f.apply(x0.fma(t, p));
            assertTrue(fx0 >= fx1);
            assertEquals(0.7, t);
        }
    }

    @Test
    void smokeTest() {

        // here we test f(x) = -Math.exp(x^2)
        RFunction f = (NArray<Double> x) -> -Math.exp(-x.getDouble(0) * x.getDouble(0));
        RDerivative df = (NArray<Double> x) -> NArrays.stride(Math.exp(-x.getDouble(0) * x.getDouble(0)) * 2 * x.getDouble(0));

        for (int i = 0; i < 1_000; i++) {
            double next = (random.nextDouble() - 0.5);
            NArray<Double> x0 = NArrays.stride(next);
            NArray<Double> p = df.apply(x0).mul(-1.);
            double alpha = BacktrackLineSearch.newSearch().search(f, df, x0, p, 100_000.0);
            double fx0 = f.apply(x0);
            double fx1 = f.apply(x0.fma(alpha, p));
            assertTrue(fx0 >= fx1);
        }
    }

    @Test
    void documentedTests() {

        T[] tests = new T[] {
                new T(
                        // this test is taken from Algorithms for Optimization, p.58
                        v -> v.getDouble(0) * v.getDouble(0) + v.getDouble(0) * v.getDouble(1) + v.getDouble(1) * v.getDouble(1),
                        v -> NArrays.stride(2 * v.getDouble(0), 2 * v.getDouble(1)),
                        NArrays.stride(-1, -1),
                        NArrays.stride(1, 2),
                        1e-4,
                        0.5,
                        10,
                        NArrays.stride(-1.5, -0.5)
                )
        };

        for (T test : tests) {
            double alpha = BacktrackLineSearch.newSearch()
                    .alpha.set(test.alpha)
                    .beta.set(test.beta)
                    .search(test.f, test.d1f, test.x0, test.d, test.t);
            NArray<Double> x1 = test.x0.copy().add(test.d.mul(alpha));
            assertTrue(x1.deepEquals(test.x1));
        }

    }

    record T(RFunction f, RDerivative d1f, NArray<Double> d, NArray<Double> x0, double alpha, double beta, double t, NArray<Double> x1) {
    }
}
