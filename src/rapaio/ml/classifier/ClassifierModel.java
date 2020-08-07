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

package rapaio.ml.classifier;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Interface for all classification model algorithms.
 * A classifier is able to classify multiple target columns, if implementation allows that.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface ClassifierModel extends Printable, Serializable {

    /**
     * Creates a new classifier instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    ClassifierModel newInstance();

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
     * Describes the classification algorithm
     *
     * @return capabilities of the classification algorithm
     */
    default Capabilities capabilities() {
        return Capabilities.builder().build();
    }

    /**
     * Returns input variable names built at learning time
     *
     * @return input variable names
     */
    String[] inputNames();

    /**
     * Shortcut method which returns input variable name at the
     * given position
     *
     * @param pos given position
     * @return variable name
     */
    default String inputName(int pos) {
        return inputNames()[pos];
    }

    /**
     * Returns the types of input variables built at learning time
     *
     * @return array of input variable types
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
     * Returns levels used at learning times for target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    Map<String, List<String>> targetLevels();

    default List<String> targetLevels(String key) {
        return targetLevels().get(key);
    }

    /**
     * Returns levels used at learning times for first target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    default List<String> firstTargetLevels() {
        return targetLevels().get(firstTargetName());
    }

    default String firstTargetLevel(int pos) {
        return targetLevels().get(firstTargetName()).get(pos);
    }

    /**
     * @return true if the classifier has learned from a sample
     */
    boolean hasLearned();

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target specified by targetNames
     *
     * @param df         data set instances
     * @param targetVars target variables
     */
    ClassifierModel fit(Frame df, String... targetVars);

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetNames
     *
     * @param df         predict frame
     * @param weights    instance weights
     * @param targetVars target variables
     */
    ClassifierModel fit(Frame df, Var weights, String... targetVars);

    /**
     * Predict classes for new data set instances, with
     * default options to compute classes and densities for classes.
     *
     * @param df data set instances
     */
    <R extends ClassifierResult> R predict(Frame df);

    /**
     * Predict classes for given instances, generating classes if specified and
     * distributions if specified.
     *
     * @param df                frame instances
     * @param withClasses       generate classes
     * @param withDistributions generate densities for classes
     */
    <R extends ClassifierResult> R predict(Frame df, boolean withClasses, boolean withDistributions);
}
