/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.regressor;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.ml.classifier.varselect.VarSelector;

/**
 * Interface implemented by all regression algorithms
 *
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public interface Regressor {
    /**
     * Creates a new regressor instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    Regressor newInstance();

    /**
     * Returns the regressor name.
     *
     * @return regressor name
     */
    String name();

    /**
     * Builds a string which contains the regressor instance name and parameters.
     *
     * @return regressor algorithm name and parameters
     */
    String fullName();


    VarSelector getVarSelector();

    Regressor withVarSelector(VarSelector varSelector);

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target as targetVars.
     *
     * @param df         data set instances
     * @param targetVars target variables
     */
    default void learn(Frame df, String... targetVars) {
        Numeric weights = Numeric.newFill(df.rowCount(), 1);
        learn(df, weights, targetVars);
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetVars
     *
     * @param df             train frame
     * @param weights        instance weights
     * @param targetVarNames target variables
     */
    void learn(Frame df, Numeric weights, String... targetVarNames);

    /**
     * Predict classes for new data set instances
     *
     * @param df data set instances
     */
    default RPrediction predict(Frame df) {
        return predict(df, true);
    }

    /**
     * Predict classes for given instances, generating classes if specified and
     * generating densities if specified.
     *
     * @param df            frame instances
     * @param withResiduals compute residuals
     */
    RPrediction predict(Frame df, boolean withResiduals);

    /**
     * Returns target variables built at learning time
     *
     * @return target variable names
     */
    String[] targetNames();

    /**
     * Returns first target variable built at learning time
     *
     * @return target variable names
     */
    default String firstTargetVarName() {
        return targetNames()[0];
    }
}
