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

package rapaio.ml.regression;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.io.Serializable;

/**
 * Interface implemented by all regression algorithms
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/20/14.
 */
public interface RegressionModel extends Printable, Serializable {

    /**
     * Creates a new regression instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    RegressionModel newInstance();

    /**
     * @return regression model name
     */
    String name();

    /**
     * @return regression algorithm name and parameters description
     */
    String fullName();

    /**
     * Describes the learning algorithm
     *
     * @return capabilities of the learning algorithm
     */
    Capabilities capabilities();

    /**
     * Returns input variable names built at learning time
     *
     * @return input variable names
     */
    String[] inputNames();

    /**
     * Returns the variable name at a given position
     *
     * @param pos position of the variable
     * @return variable name
     */
    default String inputName(int pos) {
        return inputNames()[pos];
    }

    /**
     * @return array with types of the variables used for training
     */
    VType[] inputTypes();

    /**
     * Shortcut method which returns the type of the input variable at the given position
     *
     * @param pos given position
     * @return variable type
     */
    default VType inputType(int pos) {
        return inputTypes()[pos];
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
     * Returns target variable types built at learning time
     *
     * @return array of target types
     */
    VType[] targetTypes();

    /**
     * Shortcut method which returns target variable type
     * at the given position
     *
     * @param pos given position
     * @return target variable type
     */
    default VType targetType(int pos) {
        return targetTypes()[pos];
    }

    /**
     * Shortcut method which returns the variable type
     * of the first target
     *
     * @return first target variable type
     */
    default VType firstTargetType() {
        return targetTypes()[0];
    }

    /**
     * @return true if the learning method was called and the model was fitted on data
     */
    boolean isFitted();

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target as targetName.
     *
     * @param df         data set instances
     * @param targetVars target variables
     */
    <M extends RegressionModel> M fit(Frame df, String... targetVars);

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetName
     *
     * @param df             predict frame
     * @param weights        instance weights
     * @param targetVarNames target variables
     */
    <M extends RegressionModel> M fit(Frame df, Var weights, String... targetVarNames);

    /**
     * Predict results for given data set of instances
     * and also produce residuals and other derivatives.
     *
     * @param df input data frame
     * @return regression predict result
     */
    default <R extends RegressionResult> R predict(final Frame df) {
        return predict(df, false);
    }

    /**
     * Predict results for new data set instances
     *
     * @param df            data set instances
     * @param withResiduals if residuals will be computed or not
     */
    <R extends RegressionResult> R predict(Frame df, boolean withResiduals);

    String headerSummary();
}
