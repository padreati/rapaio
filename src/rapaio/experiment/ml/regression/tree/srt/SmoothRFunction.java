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

package rapaio.experiment.ml.regression.tree.srt;

import rapaio.data.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/19/19.
 */
public interface SmoothRFunction {

    /**
     * Builds a new instance of the function, useful because each function
     * is a model which can be fittted on data. As such, when one wants to fit a new
     * function then it creates a new instance and fit that instance on data.
     *
     * @return new function model instance
     */
    SmoothRFunction newInstance();

    /**
     * Fit function on given data
     *
     * @param df          input data
     * @param weights     weights of the observations
     * @param y           target data (usually residuals)
     * @param testVarName test variable name
     * @return predicted values for each observations
     */
    VarDouble fit(Frame df, Var weights, Var y, String testVarName);

    /**
     * Compute prediction value for the given observation
     *
     * @param df  observations data frame
     * @param row observation row in the given frame
     * @return function value
     */
    double predict(Frame df, int row);

    /**
     * Compute left weight for given observation on split
     *
     * @param df observations data frame
     * @param row observation row
     * @return left weight
     */
    double leftWeight(Frame df, int row);

    /**
     * Compute right weight for given observation on split
     * @param df observations data frame
     * @param row observation row
     * @return right weight
     */
    double rightWeight(Frame df, int row);

    /**
     * Compute left residual for given observation on split
     * @param df observations data frame
     * @param row observation row
     * @return left residual
     */
    double leftResidual(Frame df, Var y, int row);

    /**
     * Compute right residual for given observation on split
     * @param df observations data frame
     * @param row observation row
     * @return right residual
     */
    double rightResidual(Frame df, Var y, int row);
}
