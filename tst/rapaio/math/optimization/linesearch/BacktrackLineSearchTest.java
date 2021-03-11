/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DVectorDense;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/31/21.
 */
public class BacktrackLineSearchTest {

    @Test
    void validationTest() {
        assertThrows(IllegalArgumentException.class, () -> BacktrackLineSearch.from(0));

        assertThrows(IllegalArgumentException.class, () -> BacktrackLineSearch.from(1, 0, 0.4));
        assertThrows(IllegalArgumentException.class, () -> BacktrackLineSearch.from(1, 1, 0.4));

        assertThrows(IllegalArgumentException.class, () -> BacktrackLineSearch.from(1, 0.1, 0));
        assertThrows(IllegalArgumentException.class, () -> BacktrackLineSearch.from(1, 0.1, 1));
    }

    @Test
    void smokeSquaredTest() {

        // here we test f(x) = x^2
        // after calculations for steepest descent alpha have a constant value
        RFunction f = (DVector x) -> x.get(0) * x.get(0);
        RDerivative df = (DVector x) -> x.copy().mult(2.0);

        for (int i = 0; i < 10_000; i++) {
            double next = (RandomSource.nextDouble() - 0.5) * 100;
            DVector x0 = DVectorDense.wrap(next);
            DVector p = df.apply(x0).mult(-1);
            double alpha = BacktrackLineSearch.fromDefaults().search(f, df, x0, p);
            double fx0 = f.apply(x0);
            double fx1 = f.apply(p.caxpy(alpha, x0));
            assertTrue(fx0 >= fx1);
            assertEquals(0.0625, alpha);
        }

    }

    @Test
    void smokeTest() {

        // here we test f(x) = -Math.exp(x^2)
        RFunction f = (DVector x) -> -Math.exp(-x.get(0) * x.get(0));
        RDerivative df = (DVector x) -> DVectorDense.wrap(Math.exp(-x.get(0) * x.get(0)) * 2 * x.get(0));

        for (int i = 0; i < 10_000; i++) {
            double next = (RandomSource.nextDouble() - 0.5);
            DVector x0 = DVectorDense.wrap(next);
            DVector p = df.apply(x0).mult(-1);
            double alpha = BacktrackLineSearch.from(100_000).search(f, df, x0, p);
            double fx0 = f.apply(x0);
            double fx1 = f.apply(p.caxpy(alpha, x0));
            assertTrue(fx0 >= fx1);
        }

    }
}
