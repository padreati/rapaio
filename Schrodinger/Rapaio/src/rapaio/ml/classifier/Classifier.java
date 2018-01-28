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

package rapaio.ml.classifier;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Interface for all classification model algorithms.
 * A classifier is able to classify multiple target columns, if implementation allows that.
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

    /**
     * @return the sampler instance used
     */
    RowSampler sampler();

    /**
     * Specifies the sampler to be used at learning time.
     * The sampler is responsible for selecting the instances to be learned.
     * The default implementation is {@link rapaio.data.sample.Identity}
     * which gives all the original training instances.
     *
     * @param sampler instance of a new sampler
     */
    Classifier withSampler(RowSampler sampler);

    /**
     * Filters which will be applied on input variables
     * for various transformations, before the data is learned.
     * <p>
     * Thus, input variables learned by a model are not derived
     * directly from the data frame used by removing target
     * variables, but by pre-processing them with filters.
     * <p>
     * Filters will be applied always in sequence.
     * The filtering process has the following steps:
     * <p>
     * <ol>
     * <li>consider data frame as draft data frame</li>
     * <li>take in order the filters from input filter list</li>
     * <li>apply each filter to draft data frame and dessignate the result as draft data frame</li>
     * <li>after all filters are executed designate draft data frame as the workable data frame</li>
     * <li>parse all the target variable names from pattern strings and workable data frame</li>
     * <li>collect all the variable names from workable data frame</li>
     * <li>collect target variable names from the list of available variable names</li>
     * <li>collect input variable as all the variables which are not considered target variables</li>
     * </ol>
     * <p>
     * This algorithm is executed each time for {@link #train(Frame, Var, String...)},
     * {@link #train(Frame, String...)}, {@link #fit(Frame)} and {@link #fit(Frame, boolean, boolean)} methods.
     *
     * @return list of filter to transform data into input variables.
     */
    List<FFilter> inputFilters();

    /**
     * Specifies which filters will be used to transform data
     * before learning and fitting.
     *
     * @param filters list of filters applied in chain
     * @return self instance
     */
    default Classifier withInputFilters(FFilter... filters) {
        return withInputFilters(Arrays.stream(filters).collect(Collectors.toList()));
    }

    /**
     * Specifies which filters will be used to transform data
     * before learning and fitting.
     *
     * @param filters list of filters applied in chain
     * @return self instance
     */
    Classifier withInputFilters(List<FFilter> filters);

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
     * Returns levels used at learning times for target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    Map<String, String[]> targetLevels();

    default String[] targetLevels(String key) {
        return targetLevels().get(key);
    }

    /**
     * Returns levels used at learning times for first target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    default String[] firstTargetLevels() {
        return targetLevels().get(firstTargetName());
    }

    default String firstTargetLevel(int pos) {
        return targetLevels().get(firstTargetName())[pos];
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
    Classifier train(Frame df, String... targetVars);

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetNames
     *
     * @param df         train frame
     * @param weights    instance weights
     * @param targetVars target variables
     */
    Classifier train(Frame df, Var weights, String... targetVars);

    /**
     * Predict classes for new data set instances, with
     * default options to compute classes and densities for classes.
     *
     * @param df data set instances
     */
    CFit fit(Frame df);

    /**
     * Predict classes for given instances, generating classes if specified and
     * distributions if specified.
     *
     * @param df                frame instances
     * @param withClasses       generate classes
     * @param withDistributions generate densities for classes
     */
    CFit fit(Frame df, boolean withClasses, boolean withDistributions);

    /**
     * set the pool size for fork join tasks
     * - poolSize == 0 it is executed in a single non fork join thread
     * - poolSize < 0 pool size for fork join pool is the number of CPUs
     * - poolSize > 0, pool size for fork join pool is this value
     *
     * @param poolSize specified pool size
     */
    Classifier withRunPoolSize(int poolSize);

    /**
     * Gets the configured pool size. Negative values are considered
     * automatically as pool of number of available CPUs, zero means
     * no pooling and positive values means pooling with a specified
     * value.
     *
     * @return pool size to be used
     */
    int runPoolSize();

    /**
     * @return the number of runs
     */
    int runs();

    /**
     * Specifies the runs / rounds of learning.
     * For various models composed of multiple sub-models
     * the runs represents often the number of sub-models.
     * <p>
     * For example for CForest the number of runs is used to specify
     * the number of decision trees to be built.
     *
     * @param runs number of runs
     * @return self-instance, used for builder pattern
     **/
    Classifier withRuns(int runs);

    /**
     * Get the lambda call hook which will be called after
     * each sub-component or iteration specified by {@link #withRuns(int)}
     * is trained.
     *
     * @return lambda running hook
     */
    BiConsumer<Classifier, Integer> runningHook();

    /**
     * Set up a lambda call hook which will be called after
     * each sub-component or iteration specified by {@link #withRuns(int)}
     * is trained.
     *
     * @param runningHook bi consumer method to be called at each iteration, first
     *                    parameter is the model built at the time and the second
     *                    parameter value is the run value
     * @return self-instance of the model
     */
    Classifier withRunningHook(BiConsumer<Classifier, Integer> runningHook);
}
