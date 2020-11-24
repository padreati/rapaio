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
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.function.SBiConsumer;
import rapaio.util.function.SBiFunction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract base class for all classifier implementations.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractClassifierModel<M extends AbstractClassifierModel<M, R>, R extends ClassifierResult>
        extends ParamSet<M> implements ClassifierModel {

    private static final long serialVersionUID = -6866948033065091047L;

    // parameters

    public final SBiFunction<ClassifierModel, Integer, Boolean> DEFAULT_STOPPING_HOOK = (classifier, integer) -> false;
    public final SBiConsumer<ClassifierModel, Integer> DEFAULT_RUNNING_HOOK = (m, i) -> {
    };

    public final ValueParam<RowSampler, M> rowSampler = new ValueParam<>((M) this, RowSampler.identity(),
            "rowSampler",
            "Method used to sample rows.",
            Objects::nonNull);

    /**
     * Number of threads for execution pool size. Negative values are considered
     * automatically as pool of number of available CPUs, zero means
     * no pooling and positive values means pooling with a specified
     * value.
     */
    public final ValueParam<Integer, M> poolSize = new ValueParam<>((M) this, 0,
            "poolSize",
            "Number of threads in execution pool to be used for fitting the model.",
            x -> true);
    /**
     * Specifies the runs / rounds of learning.
     * For various models composed of multiple sub-models
     * the runs represents often the number of sub-models.
     * <p>
     * For example for CForest the number of runs is used to specify
     * the number of decision trees to be built.
     */
    public final ValueParam<Integer, M> runs = new ValueParam<>((M) this, 1,
            "runs",
            "Number of iterations for iterative iterations or number of sub ensembles.",
            x -> x > 0
    );

    /**
     * Lambda call hook called after each sub-component or iteration at training time.
     */
    public final ValueParam<SBiConsumer<ClassifierModel, Integer>, M> runningHook = new ValueParam<>((M) this,
            DEFAULT_RUNNING_HOOK,
            "runningHook",
            "Hook executed at each iteration.",
            Objects::nonNull
    );

    /**
     * Lambda call hook which can be used to implement a criteria used to stop running
     * an iterative procedure. If the call hook returns false, the iterative procedure is
     * stopped, if true it continues until the algorithm stops itself.
     */
    public ValueParam<SBiFunction<ClassifierModel, Integer, Boolean>, M> stoppingHook = new ValueParam<>((M) this,
            DEFAULT_STOPPING_HOOK,
            "stopHook",
            "Hook queried at each iteration if execution should continue or not.",
            Objects::nonNull);

    // learning artifacts

    protected boolean learned = false;
    protected String[] inputNames;
    protected VType[] inputTypes;
    protected String[] targetNames;
    protected VType[] targetTypes;
    protected Map<String, List<String>> targetLevels;

    @Override
    public String fullName() {
        return name() + '{' + getStringParameterValues(true) + '}';
    }

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public VType[] inputTypes() {
        return inputTypes;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }

    @Override
    public VType[] targetTypes() {
        return targetTypes;
    }

    @Override
    public Map<String, List<String>> targetLevels() {
        return targetLevels;
    }

    public boolean hasLearned() {
        return learned;
    }

    @Override
    public final M fit(Frame df, String... targetVars) {
        VarDouble weights = VarDouble.fill(df.rowCount(), 1);
        return fit(df, weights, targetVars);
    }

    @Override
    public final M fit(Frame df, Var weights, String... targetVars) {
        FitSetup setup = prepareFit(df, weights, targetVars);
        learned = coreFit(setup.df, setup.w);
        return (M) this;
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
        List<String> targets = VRange.of(targetVars).parseVarNames(df);
        this.targetNames = targets.toArray(new String[0]);
        this.targetTypes = targets.stream().map(name -> df.rvar(name).type()).toArray(VType[]::new);
        this.targetLevels = new HashMap<>();
        this.targetLevels.put(firstTargetName(), df.rvar(firstTargetName()).levels());

        HashSet<String> targetSet = new HashSet<>(targets);

        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(name -> df.rvar(name).type()).toArray(VType[]::new);

        capabilities().checkAtLearnPhase(df, weights, targetVars);
        return FitSetup.valueOf(df, weights, targetVars);
    }

    protected abstract boolean coreFit(Frame df, Var weights);

    @Override
    public final R predict(Frame df) {
        return predict(df, true, true);
    }

    @Override
    public final R predict(Frame df, boolean withClasses, boolean withDistributions) {
        PredSetup setup = preparePredict(df, withClasses, withDistributions);
        return corePredict(setup.df, setup.withClasses, setup.withDistributions);
    }

    protected PredSetup preparePredict(Frame df, boolean withClasses, boolean withDistributions) {
        return PredSetup.valueOf(df, withClasses, withDistributions);
    }

    protected abstract R corePredict(Frame df, boolean withClasses, boolean withDistributions);

    public String fullNameSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append(" model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName()).append("\n\n");
        return sb.toString();
    }

    public String capabilitiesSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Capabilities:\n");
        sb.append(capabilities().toString()).append("\n");
        return sb.toString();
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
}
