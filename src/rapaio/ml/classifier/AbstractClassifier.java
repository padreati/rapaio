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
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.data.sample.RowSampler;
import rapaio.printer.format.TextTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract base class for all classifiers.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifier implements Classifier {

    private static final long serialVersionUID = -6866948033065091047L;
    private List<FFilter> inputFilters = new ArrayList<>();
    private String[] inputNames;
    private VarType[] inputTypes;
    private String[] targetNames;
    private VarType[] targetTypes;
    private Map<String, List<String>> dict;
    private RowSampler sampler = RowSampler.identity();
    private boolean learned = false;
    private int poolSize = 0;
    private int runs = 1;
    private BiConsumer<Classifier, Integer> runningHook;

    @Override
    public RowSampler sampler() {
        return sampler;
    }

    @Override
    public AbstractClassifier withSampler(RowSampler sampler) {
        this.sampler = sampler;
        return this;
    }

    @Override
    public List<FFilter> inputFilters() {
        return inputFilters;
    }

    @Override
    public Classifier withInputFilters(List<FFilter> filters) {
        inputFilters = new ArrayList<>();
        for (FFilter filter : filters)
            inputFilters.add(filter.newInstance());
        return this;
    }

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public VarType[] inputTypes() {
        return inputTypes;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }

    @Override
    public VarType[] targetTypes() {
        return targetTypes;
    }

    @Override
    public Map<String, List<String>> targetLevels() {
        return dict;
    }

    public boolean hasLearned() {
        return learned;
    }

    @Override
    public final Classifier fit(Frame df, String... targetVars) {
        VarDouble weights = VarDouble.fill(df.rowCount(), 1);
        return fit(df, weights, targetVars);
    }

    @Override
    public final Classifier fit(Frame df, Var weights, String... targetVars) {
        BaseTrainSetup setup = baseFit(df, weights, targetVars);
        Frame workDf = prepareFit(setup.df, setup.w, setup.targetVars);
        learned = coreFit(workDf, setup.w);
        return this;
    }

    /**
     * This method is prepares learning phase. It is a generic method which works
     * for all learners. It's tass includes initialization of target names,
     * input names, check the capabilities at learning phase, etc.
     *
     * @param dfOld      data frame
     * @param weights    weights of instances
     * @param targetVars target variable names
     */
    protected Frame prepareFit(Frame dfOld, final Var weights, final String... targetVars) {
        Frame df = dfOld;
        for (FFilter filter : inputFilters) {
            df = filter.fitApply(df);
        }
        Frame result = df;
        List<String> targets = VRange.of(targetVars).parseVarNames(result);
        this.targetNames = targets.stream().toArray(String[]::new);
        this.targetTypes = targets.stream().map(name -> result.rvar(name).type()).toArray(VarType[]::new);
        this.dict = new HashMap<>();
        this.dict.put(firstTargetName(), result.rvar(firstTargetName()).levels());

        HashSet<String> targetSet = new HashSet<>(targets);
        List<String> inputs = Arrays.stream(result.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.stream().toArray(String[]::new);
        this.inputTypes = inputs.stream().map(name -> result.rvar(name).type()).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(result, weights, targetVars);
        return result;
    }

    protected BaseTrainSetup baseFit(Frame df, Var weights, String... targetVars) {
        return BaseTrainSetup.valueOf(df, weights, targetVars);
    }

    protected abstract boolean coreFit(Frame df, Var weights);

    @Override
    public final CPrediction predict(Frame df) {
        return predict(df, true, true);
    }

    @Override
    public final CPrediction predict(Frame df, boolean withClasses, boolean withDistributions) {
        BaseFitSetup setup = basePredict(df, withClasses, withDistributions);
        Frame workDf = preparePredict(setup.df);
        return corePredict(workDf, setup.withClasses, setup.withDistributions);
    }

    // by default do nothing, it is only for two stage training
    protected BaseFitSetup basePredict(Frame df, boolean withClasses, boolean withDistributions) {
        return BaseFitSetup.valueOf(df, withClasses, withDistributions);
    }

    protected Frame preparePredict(Frame df) {
        Frame result = df;
        for (FFilter filter : inputFilters) {
            result = filter.apply(result);
        }
        return result;
    }

    protected abstract CPrediction corePredict(Frame df, boolean withClasses, boolean withDistributions);

    @Override
    public String summary() {
        return "not implemented";
    }

    public String baseSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("input vars: \n");

        int varCount = inputNames.length;
        TextTable tt = TextTable.newEmpty(varCount, 5);
        for (int i = 0; i < varCount; i++) {
            tt.set(i, 0, i + ".", 1);
            tt.set(i, 1, inputNames[i], 1);
            tt.set(i, 2, ":", -1);
            tt.set(i, 3, inputTypes[i].name(), -1);
            tt.set(i, 4, " |", 1);
        }
        tt.withMerge();
        sb.append(tt.summary()).append("\n");

        sb.append("target vars:\n");
        IntStream.range(0, targetNames().length).forEach(i -> sb.append("> ")
                .append(targetName(i)).append(" : ")
                .append(targetType(i))
                .append(" [").append(String.join(",", targetLevels(targetName(i)))).append("]")
                .append("\n"));
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public AbstractClassifier withRunPoolSize(int poolSize) {
        this.poolSize = poolSize < 0 ? Runtime.getRuntime().availableProcessors() : poolSize;
        return this;
    }

    @Override
    public int runPoolSize() {
        return poolSize;
    }

    @Override
    public int runs() {
        return runs;
    }

    @Override
    public Classifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public BiConsumer<Classifier, Integer> runningHook() {
        return runningHook;
    }

    @Override
    public Classifier withRunningHook(BiConsumer<Classifier, Integer> runningHook) {
        this.runningHook = runningHook;
        return this;
    }

    protected static class BaseTrainSetup {
        public final Frame df;
        public final Var w;
        public final String[] targetVars;

        private BaseTrainSetup(Frame df, Var w, String[] targetVars) {
            this.df = df;
            this.w = w;
            this.targetVars = targetVars;
        }

        public static BaseTrainSetup valueOf(Frame df, Var w, String[] targetVars) {
            return new BaseTrainSetup(df, w, targetVars);
        }
    }

    protected static final class BaseFitSetup {

        public final Frame df;
        public final boolean withClasses;
        public final boolean withDistributions;

        private BaseFitSetup(Frame df, boolean withClasses, boolean withDistributions) {
            this.df = df;
            this.withClasses = withClasses;
            this.withDistributions = withDistributions;
        }

        public static BaseFitSetup valueOf(Frame df, boolean withClasses, boolean withDistributions) {
            return new BaseFitSetup(df, withClasses, withDistributions);
        }
    }
}
