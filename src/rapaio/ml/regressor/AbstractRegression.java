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

import rapaio.data.*;
import rapaio.data.filter.FFilter;
import rapaio.data.sample.FrameSampler;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public abstract class AbstractRegression<T extends RFit> implements Regression<T> {

    private static final long serialVersionUID = 5544999078321108408L;

    private String[] inputNames;
    private VarType[] inputTypes;
    private String[] targetNames;
    private VarType[] targetTypes;
    private FrameSampler sampler = new FrameSampler.Identity();
    private boolean hasLearned;
    private int poolSize = Runtime.getRuntime().availableProcessors();
    private int runs = 1;
    private List<FFilter> inputFilters = new ArrayList<>();

    private BiConsumer<Regression, Integer> runningHook;


    @Override
    public List<FFilter> inputFilters() {
        return inputFilters;
    }

    @Override
    public Regression<T> withInputFilters(FFilter... filters) {
        inputFilters.clear();
        Collections.addAll(inputFilters, filters);
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
    public FrameSampler sampler() {
        return sampler;
    }

    @Override
    public AbstractRegression withSampler(FrameSampler sampler) {
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
    public final Regression train(Frame df, String... targetVarNames) {
        return train(df, Numeric.newFill(df.rowCount(), 1), targetVarNames);
    }

    @Override
    public final Regression train(Frame df, Var weights, String... targetVarNames) {
        TrainSetup setup = baseTrain(df, weights, targetVarNames);
        setup = prepareTraining(setup.df, setup.w, setup.targetVars);
        hasLearned = coreTrain(setup.df, setup.w);
        return this;
    }

    protected TrainSetup prepareTraining(Frame dfOld, Var weights, String... targetVarNames) {
        Frame df = dfOld;
        for (FFilter filter : inputFilters) {
            df = filter.filter(df);
        }

        Frame result = df;
        List<String> targets = new VarRange(targetVarNames).parseVarNames(result);
        this.targetNames = targets.stream().toArray(String[]::new);
        this.targetTypes = targets.stream().map(name -> result.var(name).type()).toArray(VarType[]::new);

        HashSet<String> targetSet = new HashSet<>(targets);
        List<String> inputs = Arrays.stream(result.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.stream().toArray(String[]::new);
        this.inputTypes = inputs.stream().map(name -> result.var(name).type()).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(result, weights, targetVarNames);
        return TrainSetup.valueOf(df, weights);
    }

    protected TrainSetup baseTrain(Frame df, Var weights, String... targetVarNames) {
        return TrainSetup.valueOf(df, weights, targetVarNames);
    }

    protected abstract boolean coreTrain(Frame df, Var weights);


    @Override
    public final T fit(Frame df) {
        return fit(df, true);
    }

    @Override
    public final T fit(Frame df, boolean withResiduals) {
        FitSetup setup = baseFit(df, withResiduals);
        setup = prepareFit(setup.df, withResiduals);
        return coreFit(setup.df, setup.withResiduals);
    }

    // by default do nothing, it is only for two stage training
    protected FitSetup baseFit(Frame df, boolean withResiduals) {
        return FitSetup.valueOf(df, withResiduals);
    }

    protected FitSetup prepareFit(Frame df, boolean withResiduals) {
        Frame result = df;
        for (FFilter filter : inputFilters) {
            result = filter.apply(result);
        }
        return FitSetup.valueOf(result, withResiduals);
    }

    protected abstract T coreFit(Frame df, boolean withResiduals);

    @Override
    public boolean hasLearned() {
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
}
