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

package rapaio.optimization;

import java.util.List;

import rapaio.data.VarDouble;
import rapaio.linear.DVector;

/**
 * An optimization algorithm for finding local minimum of a function. Any maximization problem can be casted as maximization without
 * difficulty.
 * <p>
 * The interface for optimizer does not define any particular aspect of the algorithm, like parameters, constraints or
 * any property of the function. It simply acts as a black box which wraps an optimization algorithm. Any aspect of the
 * optimization procedure is handled by the implementing classes.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/31/21.
 */
public interface Solver {

    /**
     * Runs the computation to obtain solutions.
     */
    Solver compute();

    /**
     * @return list of partial solution (if we have an iterative algorithm), otherwise it will contain only the final solution
     */
    List<DVector> solutions();

    /**
     * @return Vector of errors for each iteration
     */
    VarDouble errors();

    /**
     * @return final solution of the algorithm
     */
    DVector solution();

    /**
     * @return true if the computation converged to a solution or not
     */
    boolean hasConverged();
}