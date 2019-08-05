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
import rapaio.math.linear.*;
import rapaio.printer.format.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public abstract class AbstractRegressionModel<M extends AbstractRegressionModel<M, R>, R extends RegressionResult<M>>
        implements RegressionModel<M, R> {

    private static final long serialVersionUID = 5544999078321108408L;

    public BiFunction<M, Integer, Boolean> DEFAULT_STOPPING_HOOK = (regression, integer) -> false;

    // parameters

    protected RowSampler sampler = RowSampler.identity();
    protected int poolSize = -1;
    protected int runs = 1;
    protected boolean hasLearned;
    protected BiFunction<M, Integer, Boolean> stoppingHook = DEFAULT_STOPPING_HOOK;
    protected BiConsumer<M, Integer> runningHook;

    // model artifacts

    protected String[] inputNames;
    protected VType[] inputTypes;
    protected String[] targetNames;
    protected VType[] targetTypes;

    public M newInstanceDecoration(M regression) {
        return regression
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
    public M withSampler(RowSampler rowSampler) {
        this.sampler = rowSampler;
        return (M) this;
    }

    @Override
    public int poolSize() {
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

    @Override
    public BiFunction<M, Integer, Boolean> stoppingHook() {
        return stoppingHook;
    }

    @Override
    public M withStoppingHook(BiFunction<M, Integer, Boolean> stoppingHook) {
        this.stoppingHook = stoppingHook;
        return (M) this;
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
    public VType[] inputTypes() {
        return inputTypes;
    }

    @Override
    public VType[] targetTypes() {
        return targetTypes;
    }

    @Override
    public boolean isFitted() {
        return hasLearned;
    }

    @Override
    public M fit(Frame df, Var weights, String... targetVarNames) {
        FitSetup setup = prepareFit(df, weights, targetVarNames);
        hasLearned = coreFit(setup.df, setup.w);
        return (M) this;
    }

    protected FitSetup prepareFit(Frame df, Var weights, String... targetVarNames) {
        // we extract target and input names and types

        List<String> targets = VRange.of(targetVarNames).parseVarNames(df);
        this.targetNames = targets.toArray(new String[0]);
        this.targetTypes = targets.stream().map(df::type).toArray(VType[]::new);

        HashSet<String> targetSet = new HashSet<>(targets);
        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(df::type).toArray(VType[]::new);

        // we then check for compatibilities

        capabilities().checkAtLearnPhase(df, weights, targetNames);

        // if everything is conform, we return the training setup

        return FitSetup.valueOf(df, weights);
    }

    protected abstract boolean coreFit(Frame df, Var weights);

    @Override
    public R predict(Frame df, boolean withResiduals) {
        PredSetup setup = preparePredict(df, withResiduals);
        return corePredict(setup.df, setup.withResiduals);
    }

    protected PredSetup preparePredict(Frame df, boolean withResiduals) {
        return PredSetup.valueOf(df, withResiduals);
    }

    protected abstract R corePredict(Frame df, boolean withResiduals);

    protected static class FitSetup {
        public Frame df;
        public Var w;
        public String[] targetVars;

        public static FitSetup valueOf(Frame df, Var w, String[] targetVars) {
            FitSetup setup = new FitSetup();
            setup.df = df;
            setup.w = w;
            setup.targetVars = targetVars;
            return setup;
        }

        public static FitSetup valueOf(Frame df, Var w) {
            return valueOf(df, w, null);
        }
    }

    protected static final class PredSetup {

        public Frame df;
        public boolean withResiduals;

        public static PredSetup valueOf(Frame df, boolean withResiduals) {
            PredSetup setup = new PredSetup();
            setup.df = df;
            setup.withResiduals = withResiduals;
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
