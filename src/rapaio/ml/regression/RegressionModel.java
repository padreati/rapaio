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

import rapaio.data.*;
import rapaio.data.sample.*;
import rapaio.ml.common.*;
import rapaio.printer.*;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Interface implemented by all regression algorithms
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/20/14.
 */
public interface RegressionModel<M extends RegressionModel<M, R>, R extends RegressionResult<M>> extends Printable, Serializable {
    /**
     * Creates a new regression instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    M newInstance();

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
     * @return instance of a sampling device used at training time
     */
    RowSampler sampler();

    /**
     * Specifies the sampler to be used at learning time.
     * The sampler is responsible for selecting the instances to be learned.
     * The default implementation is {@link rapaio.data.sample.Identity}
     * which gives all the original training instances.
     *
     * @param sampler instance to be used as sampling device
     */
    M withSampler(RowSampler sampler);

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
    default M fit(Frame df, String... targetVars) {
        return fit(df, VarDouble.fill(df.rowCount(), 1).withName("weights"), targetVars);
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetName
     *
     * @param df             predict frame
     * @param weights        instance weights
     * @param targetVarNames target variables
     */
    M fit(Frame df, Var weights, String... targetVarNames);

    /**
     * Predict results for given data set of instances
     * and also produce residuals and other derivatives.
     *
     * @param df input data frame
     * @return regression predict result
     */
    default R predict(final Frame df) {
        return predict(df, false);
    }

    /**
     * Predict results for new data set instances
     *
     * @param df            data set instances
     * @param withResiduals if residuals will be computed or not
     */
    R predict(Frame df, boolean withResiduals);

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
     * set the pool size for fork join tasks
     * - poolSize == 0 it is executed in a single non fork join thread
     * - poolSize < 0 pool size for fork join pool is the number of CPUs
     * - poolSize > 0, pool size for fork join pool is this value
     *
     * @param poolSize specified pool size
     */
    M withPoolSize(int poolSize);

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
    M withRuns(int runs);

    /**
     * Get the lambda call hook which will be called after
     * each sub-component or iteration specified by {@link #withRuns(int)}
     * is trained.
     *
     * @return lambda running hook
     */
    BiConsumer<M, Integer> runningHook();

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
    M withRunningHook(BiConsumer<M, Integer> runningHook);

    /**
     * Returns the stopping hook
     *
     * @return stopping hook instance
     */
    BiFunction<M, Integer, Boolean> stoppingHook();

    /**
     * Set up a lambda call hook which will be called after each iteration and
     * returns true if the iterative training has to stop or not (in which case
     * it will continue to train). The developer is free to implement early stopping
     * of the training iterative process and some standard early stop implementations
     * will be provided to avoid repetitive implementations.
     *
     * @param stoppingHook bi function method called at each iteration, first parameter
     *                     is the regression model and the second parameter is the
     *                     iteration counter.
     * @return self instance of the model
     */
    M withStoppingHook(BiFunction<M, Integer, Boolean> stoppingHook);

    String headerSummary();
}
