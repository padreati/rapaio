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

package rapaio.math.optimization.scalar;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import rapaio.math.optimization.scalar.RootFind1D;
import rapaio.util.function.Double2DoubleFunction;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/26/21.
 */
public class RootFind1DTest {

    private final double[] as = new double[] {1, 1, 0, 10 * Math.PI / 180};
    private final double[] bs = new double[] {2, 2, 3, 280 * Math.PI / 180};
    private final Double2DoubleFunction[] fs = new Double2DoubleFunction[] {
            x -> x * x * x - x - 2,
            x -> x * x * x - x - 3,
            x -> x * x - x - 2,
            Math::sin
    };

    @Test
    void testBracketingMethods() {
        RootFind1D.Method[] methods = new RootFind1D.Method[] {
                RootFind1D.Method.Bisection,
                RootFind1D.Method.RegulaFalsi,
                RootFind1D.Method.Bisection,
                RootFind1D.Method.ITP,
                RootFind1D.Method.Brent,
                RootFind1D.Method.Ridder
        };
        for (var method : methods) {
            for (int i = 0; i < as.length; i++) {
                RootFind1D model = RootFind1D.newModel()
                        .method.set(method)
                        .x0.set(as[i])
                        .x1.set(bs[i])
                        .eps.set(1e-15)
                        .maxIter.set(1_000_000);

                model.optimize(fs[i]);

//                System.out.println("method: " + method + ", root: " + model.getX() + ", it: " + model.getIterations());
                assertTrue(Math.abs(fs[i].apply(model.getX())) <= 1e-12);
                assertTrue(model.isConverged());
            }
        }
    }

    @Test
    void testSecant() {
        Double2DoubleFunction f = x -> 1.0 * x * x - 612;
        RootFind1D secant = RootFind1D.newModel()
                .method.set(RootFind1D.Method.Secant)
                .maxIter.set(100)
                .x0.set(10.)
                .x1.set(30.);
        secant.optimize(f);

        assertTrue(Math.abs(f.apply(secant.getX())) <= 1e20);
        assertTrue(secant.isConverged());
    }
}
