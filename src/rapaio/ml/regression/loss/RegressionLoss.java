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

package rapaio.ml.regression.loss;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/6/18.
 */
public interface RegressionLoss {

    /**
     * @return name of the loss function
     */
    String name();

    /**
     * Compute value which minimizes the loss function on weighted samples
     * @param y target values
     * @param w weights of target values
     * @return computed minimum value
     */
    double findWeightedMinimum(Var y, Var w);

    /**
     * Compute value which minimizes the loss function on weighted samples
     * @param df frame which contains the variable
     * @param varName variable name
     * @param w weights of target values
     * @return computed minimum value
     */
    double findWeightedMinimum(Frame df, String varName, Var w);

    /**
     * Compute vector of gradients
     *
     * @param y vector target values
     * @param y_hat vector of fitted values
     * @return vector of computed gradients
     */
    VarDouble computeGradient(Var y, Var y_hat);
}
