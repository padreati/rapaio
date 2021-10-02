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

package rapaio.ml.loss;

import java.io.Serializable;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.common.ParametricEquals;

/**
 * Regression loss / objective function.
 * <p>
 * Interface which describes a regression loss function and connected operations with it.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/6/18.
 */
public interface Loss extends ParametricEquals<Loss>, Serializable {

    /**
     * @return name of the loss function
     */
    String name();

    /**
     * Computes constant value which minimizes the loss function on samples.
     * <p>
     * argmax_{c} sum loss(y_i, c)
     *
     * @param y target values
     * @return computed minimum value
     */
    double scalarMinimizer(Var y);

    /**
     * Computes constant value which minimizes the loss function on weighted samples.
     * <p>
     * argmax_{c} sum weight_i * loss(y_i, c)
     *
     * @param y      target values
     * @param weight weights of target values
     * @return computed minimum value
     */
    double scalarMinimizer(Var y, Var weight);

    /**
     * Computes additive constant value which minimize the loss function
     * given an already partial fit.
     * <p>
     * argmax_{c} sum loss(y_i, y^hat_i + c)
     */
    double additiveScalarMinimizer(Var y, Var fx);

    /**
     * Computes vector of values for the gradient of the loss function
     * as a loss derivative of fit function
     *
     * @param y     true target values
     * @param y_hat fitted values
     * @return vector of computed gradients for each observation
     */
    VarDouble gradient(Var y, Var y_hat);

    /**
     * Computes loss errors.
     * <p>
     * err_i = loss(y_i, y^hat_i)
     *
     * @param y     true target values
     * @param y_hat fitted values
     * @return vector with loss for each observation
     */
    VarDouble error(Var y, Var y_hat);

    /**
     * Compute a single loss score .
     *
     * @param y     vector of target values
     * @param y_hat vector of fitted values
     * @return aggregate loss score for all observations
     */
    double errorScore(Var y, Var y_hat);

    /**
     * Compute a single loss score.
     *
     * @param residual vector of target values
     * @return aggregate loss score for all observations
     */
    double residualErrorScore(Var residual);
}
