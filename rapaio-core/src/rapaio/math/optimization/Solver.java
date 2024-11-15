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

package rapaio.math.optimization;

import java.util.List;

import rapaio.data.VarDouble;
import rapaio.math.narrays.NArray;

/**
 * An optimization algorithm for finding local minimum of a function. Any maximization problem can be casted as maximization without
 * difficulty.
 * <p>
 * The interface for optimizer does not define any particular aspect of the algorithm, like parameters, constraints or
 * any property of the function. It simply acts as a black box which wraps an optimization algorithm. Any aspect of the
 * optimization procedure is handled by the implementing classes.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/31/21.
 */
public interface Solver {

    /**
     * Runs the computation to obtain solutions.
     */
    Solver compute();

    /**
     * @return list of partial solution (if we have an iterative algorithm), otherwise it will contain only the final solution
     */
    List<NArray<Double>> solutions();

    /**
     * @return Vector of errors for each iteration
     */
    VarDouble errors();

    /**
     * @return final solution of the algorithm
     */
    NArray<Double> solution();

    /**
     * @return true if the computation converged to a solution or not
     */
    boolean hasConverged();
}