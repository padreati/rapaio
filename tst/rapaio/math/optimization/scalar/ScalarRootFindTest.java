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

package rapaio.math.optimization.scalar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.util.function.Double2DoubleFunction;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/26/21.
 */
public class ScalarRootFindTest {

    @BeforeEach
    public void beforeEach() {
        RandomSource.setSeed(42);
    }

    private final double[] as = new double[]{1, 1, 0};
    private final double[] bs = new double[]{2, 2, 3};
    private final Double2DoubleFunction[] fs = new Double2DoubleFunction[]{
            x -> x * x * x - x - 2,
            x -> x * x * x - x - 3,
            x -> x * x - x - 2
    };

    @Test
    void testBisection() {
        for (int i = 0; i < as.length; i++) {
            BisectionScalarRootFind method = BisectionScalarRootFind.newMethod()
                    .a.set(as[i])
                    .b.set(bs[i])
                    .eps.set(1e-50)
                    .maxIter.set(100);

            method.optimize(fs[i]);

            assertTrue(Math.abs(fs[i].apply(method.getX())) <= 1e-50);
            assertTrue(method.isConverged());
        }
    }

    @Test
    void testRegulaFalsi() {
        for (int i = 0; i < as.length; i++) {
            RegulaFalsiScalarRootFind method = RegulaFalsiScalarRootFind.newMethod()
                    .a.set(as[i])
                    .b.set(bs[i])
                    .eps.set(1e-50)
                    .maxIter.set(100);

            method.optimize(fs[i]);

            assertTrue(Math.abs(fs[i].apply(method.getX())) <= 1e-50);
            assertTrue(method.isConverged());
        }
    }

    @Test
    void testITP() {
        for (int i = 0; i < as.length; i++) {
            ITPScalarRootFind method = ITPScalarRootFind.newMethod()
                    .a.set(as[i])
                    .b.set(bs[i])
                    .eps.set(1e-50)
                    .maxIter.set(100);

            method.optimize(fs[i]);

            assertTrue(Math.abs(fs[i].apply(method.getX())) <= 1e-50);
            assertTrue(method.isConverged());
        }
    }

}
