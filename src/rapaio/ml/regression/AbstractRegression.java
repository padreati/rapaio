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

package rapaio.ml.regression;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.data.sample.RowSampler;
import rapaio.printer.format.TextTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public abstract class AbstractRegression implements Regression {

    private static final long serialVersionUID = 5544999078321108408L;

    protected String[] inputNames;
    protected VarType[] inputTypes;
    protected String[] targetNames;
    protected VarType[] targetTypes;
    protected RowSampler sampler = RowSampler.identity();
    protected boolean hasLearned;
    protected int poolSize = -1;
    protected int runs = 1;
    protected List<FFilter> inputFilters = new ArrayList<>();

    protected BiConsumer<Regression, Integer> runningHook;


    @Override
    public List<FFilter> inputFilters() {
        return inputFilters;
    }

    @Override
    public Regression withInputFilters(FFilter... filters) {
        inputFilters = new ArrayList<>();
        addInputFilters(filters);
        return this;
    }

    @Override
    public Regression addInputFilters(FFilter... filters) {
        for (FFilter filter : filters)
            inputFilters.add(filter.newInstance());
        return this;
    }

    @Override
    public Regression cleanInputFilters() {
        inputFilters.clear();
        return this;
    }

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }

    @Override
    public RowSampler sampler() {
        return sampler;
    }

    @Override
    public AbstractRegression withSampler(RowSampler sampler) {
        this.sampler = sampler;
        return this;
    }

    @Override
    public int runs() {
        return runs;
    }

    public Regression withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public Regression fit(Frame df, Var weights, String... targetVarNames) {
        TrainSetup setup = prepareFitSetup(df, weights, targetVarNames);
        setup = prepareFit(setup);
        hasLearned = coreFit(setup.df, setup.w);
        return this;
    }

    protected TrainSetup prepareFit(TrainSetup trainSetup) {
        Frame df = trainSetup.df;
        for (FFilter filter : inputFilters) {
            df = filter.fitApply(df);
        }
        Frame result = df;
        List<String> targets = VRange.of(trainSetup.targetVars).parseVarNames(result);
        this.targetNames = targets.toArray(new String[0]);
        this.targetTypes = targets.stream().map(result::type).toArray(VarType[]::new);

        HashSet<String> targetSet = new HashSet<>(targets);
        List<String> inputs = Arrays.stream(result.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(result::type).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(result, trainSetup.w, trainSetup.targetVars);
        return TrainSetup.valueOf(df, trainSetup.w);
    }

    protected TrainSetup prepareFitSetup(Frame df, Var weights, String... targetVarNames) {
        return TrainSetup.valueOf(df, weights, targetVarNames);
    }

    protected abstract boolean coreFit(Frame df, Var weights);

    @Override
    public RPrediction predict(Frame df, boolean withResiduals) {
        FitSetup setup = preparePredictSetup(df, withResiduals);
        setup = preparePredict(setup);
        return corePredict(setup.df, setup.withResiduals);
    }

    // by default do nothing, it is only for two stage training

    protected FitSetup preparePredictSetup(Frame df, boolean withResiduals) {
        return FitSetup.valueOf(df, withResiduals);
    }

    protected FitSetup preparePredict(FitSetup fitSetup) {
        Frame result = fitSetup.df;
        for (FFilter filter : inputFilters) {
            result = filter.apply(result);
        }
        return FitSetup.valueOf(result, fitSetup.withResiduals);
    }

    protected abstract RPrediction corePredict(Frame df, boolean withResiduals);

    @Override
    public boolean isFitted() {
        return hasLearned;
    }

    @Override
    public VarType[] inputTypes() {
        return inputTypes;
    }

    @Override
    public VarType[] targetTypes() {
        return targetTypes;
    }

    @Override
    public Regression withPoolSize(int poolSize) {
        this.poolSize = poolSize < 0 ? Runtime.getRuntime().availableProcessors() : poolSize;
        return this;
    }

    @Override
    public int poolSize() {
        return poolSize;
    }

    @Override
    public BiConsumer<Regression, Integer> runningHook() {
        return runningHook;
    }

    @Override
    public Regression withRunningHook(BiConsumer<Regression, Integer> runningHook) {
        this.runningHook = runningHook;
        return this;
    }

    protected static class TrainSetup {
        public final Frame df;
        public final Var w;
        public final String[] targetVars;

        private TrainSetup(Frame df, Var w, String[] targetVars) {
            this.df = df;
            this.w = w;
            this.targetVars = targetVars;
        }

        public static TrainSetup valueOf(Frame df, Var w, String[] targetVars) {
            return new TrainSetup(df, w, targetVars);
        }

        public static TrainSetup valueOf(Frame df, Var w) {
            return new TrainSetup(df, w, null);
        }
    }

    protected static final class FitSetup {

        public final Frame df;
        public final boolean withResiduals;

        private FitSetup(Frame df, boolean withResiduals) {
            this.df = df;
            this.withResiduals = withResiduals;
        }

        public static FitSetup valueOf(Frame df, boolean withResiduals) {
            return new FitSetup(df, withResiduals);
        }
    }

    public String headerSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("Regression predict summary").append("\n");
        sb.append("=======================\n");
        sb.append("\n");

        sb.append("Model class: ").append(name()).append("\n");
        sb.append("Model instance: ").append(fullName()).append("\n");
        sb.append("\n");

        if (!hasLearned) {
            sb.append("> model not trained.\n");
            return sb.toString();
        }

        // inputs

        sb.append("> input variables: \n");

        TextTable tt = TextTable.newEmpty(inputNames().length, 3);
        for (int i = 0; i < inputNames().length; i++) {
            tt.set(i, 0, String.valueOf(i + 1) + ".", 1);
            tt.set(i, 1, inputName(i), -1);
            tt.set(i, 2, inputType(i).code(), -1);
        }
        tt.withHeaderRows(0);
        tt.withMerge();
        sb.append(tt.summary());

        // targets

        sb.append("> target variables: \n");

        tt = TextTable.newEmpty(targetNames().length, 3);
        for (int i = 0; i < targetNames().length; i++) {
            tt.set(i, 0, String.valueOf(i + 1) + ".", 1);
            tt.set(i, 1, targetName(i), -1);
            tt.set(i, 2, targetType(i).code(), -1);
        }
        tt.withHeaderRows(0);
        tt.withMerge();
        sb.append(tt.summary());

        return sb.toString();
    }
}
