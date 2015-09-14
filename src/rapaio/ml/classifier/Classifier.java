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

package rapaio.ml.classifier;

import rapaio.data.VarType;
import rapaio.data.sample.FrameSampler;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;
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
     * Describes the classification algorithm
     *
     * @return capabilities of the classification algorithm
     */
    default Capabilities capabilities() {
        return new Capabilities();
    }

    boolean debug();

    /**
     * Builds classifier with debug info on console.
     *
     * @param debug true if shows debug info on console
     * @return the classifier instance
     */
    Classifier withDebug(boolean debug);

    /**
     * @return true if the classifier has learned from a sample
     */
    boolean isLearned();

    /**
     * @return the sampler instance used
     */
    FrameSampler sampler();

    /**
     * Specifies the sampler to be used at learning time.
     * The sampler is responsible for selecting the instances to be learned.
     * The default implementation is {@link rapaio.data.sample.FrameSampler.Identity}
     * which gives all the original training instances.
     *
     * @param sampler instance of a new sampler
     */
    Classifier withSampler(FrameSampler sampler);

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
    VarType[] inputTypes();

    /**
     * Shortcut method which returns the type of the input variable at the given position
     *
     * @param pos given position
     * @return variable type
     */
    default VarType inputType(int pos) {
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
    VarType[] targetTypes();

    /**
     * Shortcut method which returns target variable type
     * at the given position
     *
     * @param pos given position
     * @return target variable type
     */
    default VarType targetType(int pos) {
        return targetTypes()[pos];
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

    default String firstDictTerm(int pos) {
        return dictionaries().get(firstTargetName())[pos];
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target specified by targetVars
     *
     * @param df         data set instances
     * @param targetVars target variables
     */
    default Classifier learn(Frame df, String... targetVars) {
        Numeric weights = Numeric.newFill(df.rowCount(), 1);
        return learn(df, weights, targetVars);
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetVars
     *
     * @param df             train frame
     * @param weights        instance weights
     * @param targetVars target variables
     */
    Classifier learn(Frame df, Var weights, String... targetVars);

    /**
     * Predict classes for new data set instances, with
     * default options to compute classes and densities for classes.
     *
     * @param df data set instances
     */
    default CFit fit(Frame df) {
        return fit(df, true, true);
    }

    /**
     * Predict classes for given instances, generating classes if specified and
     * distributions if specified.
     *
     * @param df                frame instances
     * @param withClasses       generate classes
     * @param withDistributions generate densities for classes
     */
    CFit fit(Frame df, boolean withClasses, boolean withDistributions);
}
