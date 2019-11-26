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
import rapaio.printer.format.TextTable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract base class for all classifier implementations.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifierModel<M extends ClassifierModel<M, R>, R extends ClassifierResult<M>>
        implements ClassifierModel<M, R> {

    private static final long serialVersionUID = -6866948033065091047L;

    // parameters

    private RowSampler sampler = RowSampler.identity();
    private boolean learned = false;
    private int poolSize = 0;
    private int runs = 1;
    private BiConsumer<M, Integer> runningHook;
    private BiFunction<M, Integer, Boolean> stoppingHook;

    // learning artifacts

    private String[] inputNames;
    private VType[] inputTypes;
    private String[] targetNames;
    private VType[] targetTypes;
    private Map<String, List<String>> targetLevels;

    public M newInstanceDecoration(M classifier) {
        return classifier
                .withSampler(sampler)
                .withPoolSize(poolSize)
                .withRuns(runs)
                .withRunningHook(runningHook)
                .withStoppingHook(stoppingHook);
    }

    @Override
    public RowSampler sampler() {
        return sampler;
    }

    @Override
    public M withSampler(RowSampler sampler) {
        this.sampler = sampler;
        return (M) this;
    }

    @Override
    public int runPoolSize() {
        return poolSize;
    }

    @Override
    public M withPoolSize(int poolSize) {
        this.poolSize = poolSize < 0 ? Runtime.getRuntime().availableProcessors() : poolSize;
        return (M) this;
    }

    @Override
    public int runs() {
        return runs;
    }

    @Override
    public M withRuns(int runs) {
        this.runs = runs;
        return (M) this;
    }

    @Override
    public BiConsumer<M, Integer> runningHook() {
        return runningHook;
    }

    @Override
    public M withRunningHook(BiConsumer<M, Integer> runningHook) {
        this.runningHook = runningHook;
        return (M) this;
    }

    public BiFunction<M, Integer, Boolean> stoppingHook() {
        return stoppingHook;
    }

    public M withStoppingHook(BiFunction<M, Integer, Boolean> stoppingHook) {
        this.stoppingHook = stoppingHook;
        return (M) this;
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

    public String baseSummary() {
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
        sb.append(tt.getDynamicText()).append("\n");

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
