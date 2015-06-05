/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.regressor;

import rapaio.core.sample.Sampler;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.printer.Printable;

import java.io.Serializable;

/**
 * Interface implemented by all regression algorithms
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
@Deprecated
public interface Regressor extends Printable, Serializable {
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

    Sampler sampler();

    /**
     * Specifies the sampler to be used at learning time.
     *
     * @param sampler instance of a new sampler
     */
    AbstractRegressor withSampler(Sampler sampler);

    /**
     * Returns input variable names built at learning time
     *
     * @return input variable names
     */
    String[] inputNames();

    default String inputName(int pos) {
        return inputNames()[pos];
    }

    /**
     * Returns target variables names built at learning time
     *
     * @return target variable names
     */
    String[] targetNames();

    /**
     * Returns first target variable built at learning time
     *
     * @return target variable names
     */
    default String firstTargetName() {
        return targetNames()[0];
    }

    /**
     * Returns the name of the target variable at the given position
     *
     * @param pos position of the target variable name
     * @return name of the target variable
     */
    default String targetName(int pos) {
        return targetNames()[pos];
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target as targetName.
     *
     * @param df         data set instances
     * @param targetVars target variables
     */
    default void learn(Frame df, String... targetVars) {
        Numeric weights = Numeric.newFill(df.rowCount(), 1);
        learn(df, weights, targetVars);
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetName
     *
     * @param df             train frame
     * @param weights        instance weights
     * @param targetVarNames target variables
     */
    void learn(Frame df, Var weights, String... targetVarNames);

    default RegressorFit predict(final Frame df) {
        return predict(df, true);
    }

    /**
     * Predict classes for new data set instances
     *
     * @param df            data set instances
     * @param withResiduals if residuals will be computed or not
     */
    RegressorFit predict(Frame df, boolean withResiduals);

    default String summary() {
        throw new IllegalArgumentException("not implemented");
    }
}
