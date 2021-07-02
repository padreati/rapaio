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

package rapaio.math.optimization;

import org.junit.jupiter.api.BeforeEach;
import rapaio.core.RandomSource;
import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.linear.DVector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/31/21.
 */
public class SteepestDescentSolverTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    //    @Test
    void smokeTest() {

        RFunction f = v -> v.dot(v);
        RDerivative d1f = v -> v.mult(2);

        SteepestDescentSolver optimizer = SteepestDescentSolver
                .newMinimizer()
                .f.set(f)
                .d1f.set(d1f)
                .x0.set(DVector.wrap(1, 2, 3));
        optimizer.compute();

        optimizer.solution().printFullContent();
    }
}
