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

package rapaio.experiment.math.optimization;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.RV;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/26/15.
 */
public interface Optimizer {

    /**
     * Performs optimization on the given inputs to find the minima of the function.
     *
     * @param eps            the desired accuracy of the result.
     * @param iterationLimit the maximum number of iteration steps to allow
     * @param fn             the function to optimize
     * @param fg             the derivative of the function to optimize
     * @param x0             contains the initial estimate of the minima. The length should be equal to the number of variables being solved for. This value may be altered.
     * @param inputs         a list of input data point values to learn from
     * @param outputs        a vector containing the true values for each data point in <tt>inputs</tt>
     * @return the compute value for the optimization.
     */
    VarDouble optimize(double eps, int iterationLimit,
                       BiFunction<RV, RV, Double> fn,
                       BiFunction<RV, RV, Double> fg,
                       RV x0, List<Var> inputs, VarDouble outputs);
}
