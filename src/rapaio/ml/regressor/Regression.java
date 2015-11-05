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

import rapaio.data.VarType;
import rapaio.data.sample.FrameSampler;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * Interface implemented by all regression algorithms
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public interface Regression extends Printable, Serializable {
    /**
     * Creates a new regression instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    Regression newInstance();

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
    default Capabilities capabilities() {
        return new Capabilities();
    }

    /**
     * @return true if the learning method was called and algorithm was built the model
     */
    boolean hasLearned();

    /**
     * @return instance of a sampling device used at training time
     */
    FrameSampler sampler();

    /**
     * Specifies the sampler to be used at learning time.
     * The sampler is responsible for selecting the instances to be learned.
     * The default implementation is {@link rapaio.data.sample.FrameSampler.Identity}
     * which gives all the original training instances.
     *
     * @param sampler instance to be used as sampling device
     */
    Regression withSampler(FrameSampler sampler);

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

    default RegressionFit fit(final Frame df) {
        return fit(df, true);
    }

    /**
     * Predict classes for new data set instances
     *
     * @param df            data set instances
     * @param withResiduals if residuals will be computed or not
     */
    RegressionFit fit(Frame df, boolean withResiduals);

    /**
     * set the pool size for fork join tasks
     * - poolSize == 0 it is executed in a single non fork join thread
     * - poolSize < 0 pool size for fork join pool is the number of CPUs
     * - poolSize > 0, pool size for fork join pool is this value
     *
     * @param poolSize specified pool size
     */
    Regression withPoolSize(int poolSize);

    /**
     * Gets the configured pool size. Negative values are considered
     * automatically as pool of number of available CPUs, zero means
     * no pooling and positive values means pooling with a specified
     * value.
     *
     * @return pool size to be used
     */
    int poolSize();

    /**
     * @return number of runs
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
     */
    Regression withRuns(int runs);

    /**
     * Get the lambda call hook which will be called after
     * each sub-component or iteration specified by {@link #withRuns(int)}
     * is trained.
     *
     * @return lambda running hook
     */
    BiConsumer<Regression, Integer> runningHook();

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
    Regression withRunningHook(BiConsumer<Regression, Integer> runningHook);

    default String summary() {
        throw new IllegalArgumentException("not implemented");
    }
}
