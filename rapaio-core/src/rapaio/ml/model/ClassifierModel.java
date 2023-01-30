/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.Capabilities;
import rapaio.util.param.ParamSet;
import rapaio.util.param.ValueParam;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.function.SConsumer;
import rapaio.util.function.SFunction;

/**
 * Abstract base class for all classifier implementations.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@SuppressWarnings("unchecked")
public abstract class ClassifierModel<M extends ClassifierModel<M, R, H>, R extends ClassifierResult, H extends RunInfo<M>>
        extends ParamSet<M> implements Printable, Serializable {

    @Serial
    private static final long serialVersionUID = -6866948033065091047L;

    /**
     * Method used to sample rows.
     */
    public final ValueParam<RowSampler, M> rowSampler = new ValueParam<>((M) this, RowSampler.identity(), "rowSampler", Objects::nonNull);

    /**
     * Number of threads for execution pool size. Negative values are considered
     * automatically as pool of number of available CPUs, zero means
     * no pooling and positive values means pooling with a specified
     * value.
     */
    public final ValueParam<Integer, M> poolSize = new ValueParam<>((M) this, 0, "poolSize", x -> true);

    /**
     * Specifies the runs / rounds of learning.
     * For various models composed of multiple sub-models
     * the runs often represent the number of sub-models.
     * <p>
     * For example for CForest the number of runs is used to specify
     * the number of decision trees to be built.
     */
    public final ValueParam<Integer, M> runs = new ValueParam<>((M) this, 100, "runs", x -> x > 0
    );

    /**
     * Lambda call hook called after each sub-component or iteration at training time.
     */
    public final ValueParam<SConsumer<H>, M> runningHook = new ValueParam<>((M) this, h -> {
    }, "runningHook", Objects::nonNull);

    /**
     * Lambda call hook which can be used to implement a criteria used to stop running
     * an iterative procedure. If the call hook returns false, the iterative procedure is
     * stopped, if true it continues until the algorithm stops itself.
     */
    public ValueParam<SFunction<H, Boolean>, M> stoppingHook = new ValueParam<>((M) this, h -> false, "stoppingHook", Objects::nonNull);

    public ValueParam<Long, M> seed = new ValueParam<>((M) this, 0L, "seed");

    // learning artifacts

    protected boolean learned = false;
    protected String[] inputNames;
    protected VarType[] inputTypes;
    protected String[] targetNames;
    protected VarType[] targetTypes;
    protected Map<String, List<String>> targetLevels;

    /**
     * Creates a new classifier instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    public abstract ClassifierModel<M, R, H> newInstance();

    /**
     * Returns the classifier name.
     *
     * @return classifier name
     */
    public abstract String name();

    /**
     * Builds a string which contains the classifier instance name and parameters.
     *
     * @return classifier algorithm name and parameters
     */
    public String fullName() {
        return name() + '{' + getStringParameterValues(true) + '}';
    }

    /**
     * Describes the classification algorithm
     *
     * @return capabilities of the classification algorithm
     */
    public Capabilities capabilities() {
        return new Capabilities();
    }

    /**
     * Returns input variable names built at learning time
     *
     * @return input variable names
     */
    public String[] inputNames() {
        return inputNames;
    }

    /**
     * Shortcut method which returns input variable name at the
     * given position
     *
     * @param pos given position
     * @return variable name
     */
    public String inputName(int pos) {
        return inputNames()[pos];
    }

    /**
     * Returns the types of input variables built at learning time
     *
     * @return array of input variable types
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
     * Returns levels used at learning times for target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    public Map<String, List<String>> targetLevels() {
        return targetLevels;
    }

    public List<String> targetLevels(String key) {
        return targetLevels().get(key);
    }

    /**
     * Returns levels used at learning times for first target variables
     *
     * @return map with target variable names as key and levels as variables
     */
    public List<String> firstTargetLevels() {
        return targetLevels().get(firstTargetName());
    }

    public String firstTargetLevel(int pos) {
        return targetLevels().get(firstTargetName()).get(pos);
    }

    /**
     * @return true if the classifier has learned from a sample
     */
    public boolean hasLearned() {
        return learned;
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights
     * equal to 1 and target specified by targetNames
     *
     * @param df         data set instances
     * @param targetVars target variables
     */
    public M fit(Frame df, String... targetVars) {
        VarDouble weights = VarDouble.fill(df.rowCount(), 1);
        return fit(df, weights, targetVars);
    }

    /**
     * Fit a classifier on instances specified by frame, with row weights and targetNames
     *
     * @param df         predict frame
     * @param weights    instance weights
     * @param targetVars target variables
     */
    public M fit(Frame df, Var weights, String... targetVars) {
        FitSetup setup = prepareFit(df, weights, targetVars);
        learned = coreFit(setup.df, setup.w);
        return (M) this;
    }

    /**
     * Predict classes for new data set instances, with
     * default options to compute classes and densities for classes.
     *
     * @param df data set instances
     */
    public final R predict(Frame df) {
        return predict(df, true, true);
    }

    /**
     * Predict classes for given instances, generating classes if specified and
     * distributions if specified.
     *
     * @param df                frame instances
     * @param withClasses       generate classes
     * @param withDistributions generate densities for classes
     */
    public final R predict(Frame df, boolean withClasses, boolean withDistributions) {
        PredSetup setup = preparePredict(df, withClasses, withDistributions);
        return corePredict(setup.df, setup.withClasses, setup.withDistributions);
    }

    /**
     * This method is prepares learning phase. It is a generic method which works
     * for all learners. It's tass includes initialization of target names,
     * input names, check the capabilities at learning phase, etc.
     *
     * @param df         data frame
     * @param weights    weights of instances
     * @param targetVars target variable names
     */
    protected FitSetup prepareFit(Frame df, final Var weights, final String... targetVars) {
        List<String> targets = VarRange.of(targetVars).parseVarNames(df);
        if (capabilities().minTargetCount() > targets.size()) {
            throw new IllegalArgumentException("Minimum number of targets (" +
                    capabilities().minTargetCount() + ") is not met. Targets specified: [" +
                    String.join(",", targetVars) + "]");
        }
        if (capabilities().maxTargetCount() < targets.size()) {
            throw new IllegalArgumentException("Maximum number of targets (" +
                    capabilities().maxTargetCount() + ") is not met. Targets specified: [" +
                    String.join(",", targetVars) + "]");
        }

        this.targetNames = targets.toArray(new String[0]);
        this.targetTypes = targets.stream().map(name -> df.rvar(name).type()).toArray(VarType[]::new);
        this.targetLevels = new HashMap<>();
        this.targetLevels.put(firstTargetName(), df.rvar(firstTargetName()).levels());

        HashSet<String> targetSet = new HashSet<>(targets);

        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targetSet.contains(varName)).toList();
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(name -> df.rvar(name).type()).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(df, weights, targetVars);
        return FitSetup.valueOf(df, weights, targetVars);
    }

    protected abstract boolean coreFit(Frame df, Var weights);

    protected PredSetup preparePredict(Frame df, boolean withClasses, boolean withDistributions) {
        return PredSetup.valueOf(df, withClasses, withDistributions);
    }

    protected abstract R corePredict(Frame df, boolean withClasses, boolean withDistributions);

    public String fullNameSummary() {
        return name() + " model\n"
                + "================\n\n"
                + "Description:\n"
                + fullName() + "\n\n";
    }

    public String capabilitiesSummary() {
        return "Capabilities:\n"
                + capabilities().toString() + "\n";
    }

    public String inputVarsSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("input vars: \n");

        int varCount = inputNames.length;
        TextTable tt = TextTable.empty(varCount, 5);
        for (int i = 0; i < varCount; i++) {
            tt.textRight(i, 0, i + ".");
            tt.textRight(i, 1, inputNames[i]);
            tt.textLeft(i, 2, ":");
            tt.textLeft(i, 3, inputTypes[i].name());
            tt.textRight(i, 4, " |");
        }
        sb.append(tt.getDynamicText(printer, options)).append("\n");
        return sb.toString();
    }

    public String targetVarsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("target vars:\n");
        IntStream.range(0, targetNames().length).forEach(i -> sb.append("> ")
                .append(targetName(i)).append(" : ")
                .append(targetType(i))
                .append(" [").append(String.join(",", targetLevels(targetName(i)))).append("]")
                .append("\n"));
        sb.append("\n");
        return sb.toString();
    }

    protected static class FitSetup {
        public Frame df;
        public Var w;
        public String[] targetVars;

        public static FitSetup valueOf(Frame df, Var w, String[] targetVars) {
            FitSetup setup = new FitSetup();
            setup.df = df;
            setup.w = w;
            setup.targetVars = Arrays.copyOf(targetVars, targetVars.length);
            return setup;
        }
    }

    protected static final class PredSetup {

        public Frame df;
        public boolean withClasses;
        public boolean withDistributions;

        public static PredSetup valueOf(Frame df, boolean withClasses, boolean withDistributions) {
            PredSetup setup = new PredSetup();
            setup.df = df;
            setup.withClasses = withClasses;
            setup.withDistributions = withDistributions;
            return setup;
        }
    }

    protected int computeThreads() {
        return (poolSize.get() < 0)
                ? Math.max(Runtime.getRuntime().availableProcessors() - 1, 1)
                : Math.max(1, poolSize.get());
    }

    protected Random getRandom() {
        return seed.get() == 0 ? new Random() : new Random(seed.get());
    }
}
