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

package rapaio.ml.regression;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printable;
import rapaio.printer.TextTable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/20/14.
 */
@SuppressWarnings("unchecked")
public abstract class RegressionModel<M extends RegressionModel<M, R, H>, R extends RegressionResult, H>
        extends ParamSet<M>
        implements Printable, Serializable {

    @Serial
    private static final long serialVersionUID = 5544999078321108408L;

    public final Function<H, Boolean> DEFAULT_STOPPING_HOOK = h -> false;

    // parameters

    public final ValueParam<RowSampler, M> rowSampler = new ValueParam<>((M) this, RowSampler.identity(),
            "rowSampler",
            "Row sampler",
            Objects::nonNull);

    public final ValueParam<Integer, M> poolSize = new ValueParam<>((M) this, -1,
            "poolSize",
            "Number of threads in execution pool to be used for fitting the model.",
            x -> true
    );

    public final ValueParam<Integer, M> runs = new ValueParam<>((M) this, 1,
            "runs",
            "Number of iterations for iterative iterations or number of sub ensembles.",
            x -> x > 0
    );

    public final ValueParam<Consumer<H>, M> runningHook = new ValueParam<>((M) this, h -> {},
            "runningHook",
            "Hook executed at each iteration.",
            Objects::nonNull
    );

    public ValueParam<Function<H, Boolean>, M> stoppingHook = new ValueParam<>((M) this,
            DEFAULT_STOPPING_HOOK,
            "stopHook",
            "Hook queried at each iteration if execution should continue or not.",
            Objects::nonNull);

    // model artifacts

    protected boolean hasLearned;
    protected String[] inputNames;
    protected VarType[] inputTypes;
    protected String[] targetNames;
    protected VarType[] targetTypes;

    /**
     * Creates a new regression instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    public abstract RegressionModel<M, R, H> newInstance();

    /**
     * @return regression model name
     */
    public abstract String name();

    /**
     * @return regression algorithm name and parameters description
     */
    public String fullName() {
        return name() + '{' + getStringParameterValues(true) + '}';
    }

    /**
     * Describes the learning algorithm
     *
     * @return capabilities of the learning algorithm
     */
    public abstract Capabilities capabilities();

    /**
     * Returns input variable names built at learning time
     *
     * @return input variable names
     */
    public String[] inputNames() {
        return inputNames;
    }

    /**
     * Returns the variable name at a given position
     *
     * @param pos position of the variable
     * @return variable name
     */
    public String inputName(int pos) {
        return inputNames()[pos];
    }

    /**
     * @return array with types of the variables used for training
     */
    public VarType[] inputTypes() {
        return inputTypes;
    }

    /**
     * Shortcut method which returns the type of the input variable at the given position
     *
     * @param pos given position
     * @return variable type
     */
    public VarType inputType(int pos) {
        return inputTypes()[pos];
    }

    /**
     * Returns target variables names built at learning time
     *
     * @return target variable names
     */
    public String[] targetNames() {
        return targetNames;
    }

    /**
     * Returns first target variable built at learning time
     *
     * @return target variable names
     */
    public String firstTargetName() {
        return targetNames()[0];
    }

    /**
     * Returns the name of the target variable at the given position
     *
     * @param pos position of the target variable name
     * @return name of the target variable
     */
    public String targetName(int pos) {
        return targetNames()[pos];
    }

    /**
     * Returns target variable types built at learning time
     *
     * @return array of target types
     */
    public VarType[] targetTypes() {
        return targetTypes;
    }

    /**
     * Shortcut method which returns target variable type
     * at the given position
     *
     * @param pos given position
     * @return target variable type
     */
    public VarType targetType(int pos) {
        return targetTypes()[pos];
    }

    /**
     * Shortcut method which returns the variable type
     * of the first target
     *
     * @return first target variable type
     */
    public VarType firstTargetType() {
        return targetTypes()[0];
    }

    /**
     * @return true if the learning method was called and the model was fitted on data
     */
    public boolean isFitted() {
        return hasLearned;
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target as targetName.
     *
     * @param df         data set instances
     * @param targetVarNames target variables
     */
    public M fit(Frame df, String... targetVarNames) {
        VarDouble weights = VarDouble.fill(df.rowCount(), 1).name("weights");
        return fit(df, weights, targetVarNames);
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetName
     *
     * @param df             predict frame
     * @param weights        instance weights
     * @param targetVarNames target variables
     */
     public M fit(Frame df, Var weights, String... targetVarNames) {
         FitSetup setup = prepareFit(df, weights, targetVarNames);
         hasLearned = coreFit(setup.df, setup.w);
         return (M) this;
     }

    /**
     * Predict results for given data set of instances
     * and also produce residuals and other derivatives.
     *
     * @param df input data frame
     * @return regression predict result
     */
    public R predict(final Frame df, double... quantiles) {
        return predict(df, false, quantiles);
    }

    /**
     * Predict results for new data set instances
     *
     * @param df            data set instances
     * @param withResiduals if residuals will be computed or not
     * @param quantiles     prediction quantiles if the model allows a prediction distribution, ignored otherwise
     */
    public R predict(Frame df, boolean withResiduals, double... quantiles) {
        PredSetup setup = preparePredict(df, withResiduals, quantiles);
        return corePredict(setup.df, setup.withResiduals, quantiles);
    }

    protected FitSetup prepareFit(Frame df, Var weights, String... targetVarNames) {
        // we extract target and input names and types

        List<String> targets = VarRange.of(targetVarNames).parseVarNames(df);
        this.targetNames = targets.toArray(new String[0]);
        this.targetTypes = targets.stream().map(df::type).toArray(VarType[]::new);

        HashSet<String> targetSet = new HashSet<>(targets);
        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(df::type).toArray(VarType[]::new);

        // we then check for compatibilities

        capabilities().checkAtLearnPhase(df, weights, targetNames);
        return FitSetup.valueOf(df, weights);
    }

    protected abstract boolean coreFit(Frame df, Var weights);


    protected PredSetup preparePredict(Frame df, boolean withResiduals, double[] quantiles) {
        return PredSetup.valueOf(df, withResiduals, quantiles);
    }

    protected abstract R corePredict(Frame df, boolean withResiduals, double[] quantiles);

    protected static class FitSetup {
        public Frame df;
        public Var w;
        public String[] targetVars;

        public static FitSetup valueOf(Frame df, Var w, String[] targetVars) {
            FitSetup setup = new FitSetup();
            setup.df = df;
            setup.w = w;
            setup.targetVars = targetVars == null ? null : Arrays.copyOf(targetVars, targetVars.length);
            return setup;
        }

        public static FitSetup valueOf(Frame df, Var w) {
            return valueOf(df, w, null);
        }
    }

    protected static final class PredSetup {

        public Frame df;
        public boolean withResiduals;
        public double[] quantiles;

        public static PredSetup valueOf(Frame df, boolean withResiduals, double... quantiles) {
            PredSetup setup = new PredSetup();
            setup.df = df;
            setup.withResiduals = withResiduals;
            setup.quantiles = quantiles;
            return setup;
        }
    }

    public String headerSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("Regression predict summary").append("\n");
        sb.append("=======================\n");
        sb.append("Model class: ").append(name()).append("\n");
        sb.append("Model instance: ").append(fullName()).append("\n");

        if (!hasLearned) {
            sb.append("> model not trained.\n");
            return sb.toString();
        } else {
            sb.append("> model is trained.\n");
        }

        // inputs

        sb.append("> input variables: \n");

        TextTable tt = TextTable.empty(inputNames().length, 3);
        for (int i = 0; i < inputNames().length; i++) {
            tt.textRight(i, 0, (i + 1) + ".");
            tt.textLeft(i, 1, inputName(i));
            tt.textLeft(i, 2, inputType(i).code());
        }
        sb.append(tt.getRawText());

        // targets

        sb.append("> target variables: \n");

        tt = TextTable.empty(targetNames().length, 3);
        for (int i = 0; i < targetNames().length; i++) {
            tt.textRight(i, 0, (i + 1) + ".");
            tt.textLeft(i, 1, targetName(i));
            tt.textLeft(i, 2, targetType(i).code());
        }
        sb.append(tt.getRawText());

        return sb.toString();
    }
}
