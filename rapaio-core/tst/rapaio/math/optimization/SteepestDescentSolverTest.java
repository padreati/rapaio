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

package rapaio.math.optimization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.math.linear.DVector;
import rapaio.math.optimization.functions.RDerivative;
import rapaio.math.optimization.functions.RFunction;
import rapaio.math.optimization.linesearch.BacktrackLineSearch;
import rapaio.math.optimization.linesearch.LearningRateLineSearch;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/31/21.
 */
public class SteepestDescentSolverTest {

    private static final double tol = 1e-8;

    @Test
    void smokeTest() {

        RFunction f = v -> v.dotBilinearDiag(DVector.wrap(1, 3));
        RDerivative d1f = v -> v.copy().mul(DVector.wrap(2, 6));

        SteepestDescentSolver solver1 = SteepestDescentSolver
                .newSolver()
                .lineSearch.set(BacktrackLineSearch.newSearch())
                .f.set(f)
                .d1f.set(d1f)
                .maxIt.set(100_000)
                .tol.set(1e-20)
                .x0.set(DVector.wrap(3, 20))
                .compute();

        SteepestDescentSolver solver2 = SteepestDescentSolver
                .newSolver()
                .lineSearch.set(LearningRateLineSearch.from(0.0001))
                .f.set(f)
                .d1f.set(d1f)
                .maxIt.set(100_000)
                .tol.set(1e-20)
                .x0.set(DVector.wrap(3, 20)).compute();

        assertTrue(solver1.solution().deepEquals(solver2.solution(), tol));
    }
}
