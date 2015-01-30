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

package rapaio.ml.classifier;

import rapaio.core.Printable;
import rapaio.core.sample.Sampler;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface for all classification model algorithms.
 * A classifier is able to classify multiple target columns, if implementation allows that.
 * If a classifier implements further learning it has to implement
 * {@link RunningClassifier}
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public interface Classifier extends Printable, Serializable {

    /**
     * Creates a new classifier instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    Classifier newInstance();

    /**
     * Returns the classifier name.
     *
     * @return classifier name
     */
    String name();

    /**
     * Builds a string which contains the classifier instance name and parameters.
     *
     * @return classifier algorithm name and parameters
     */
    String fullName();

    /**
     * @return the sampler instance used
     */
    Sampler sampler();

    /**
     * Specifies the sampler to be used at learning time.
     *
     * @param sampler instance of a new sampler
     */
    AbstractClassifier withSampler(Sampler sampler);

    /**
     * Returns input variable names built at learning time
     *
     * @return input variable names
     */
    String[] inputNames();

    default String inputNames(int pos) {
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
     * Returns dictionaries used at learning times for target variables
     *
     * @return map with target variable names as key and dictionaries as variables
     */
    Map<String, String[]> dictionaries();

    default String[] dictionary(String key) {
        return dictionaries().get(key);
    }

    /**
     * Returns dictionaries used at learning times for first target variables
     *
     * @return map with target variable names as key and dictionaries as variables
     */
    default String[] firstDict() {
        return dictionaries().get(firstTargetName());
    }

    default String firstDict(int pos) {
        return dictionaries().get(firstTargetName())[pos];
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

    /**
     * Predict classes for new data set instances
     *
     * @param df data set instances
     */
    default CResult predict(Frame df) {
        return predict(df, true, true);
    }

    /**
     * Predict classes for given instances, generating classes if specified and
     * generating densities if specified.
     *
     * @param df                frame instances
     * @param withClasses       generate classes
     * @param withDistributions generate densities for classes
     */
    CResult predict(Frame df, boolean withClasses, boolean withDistributions);
}
