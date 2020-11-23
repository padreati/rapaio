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

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.printer.TextTable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/20/14.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRegressionModel<M extends AbstractRegressionModel<M, R>, R extends RegressionResult>
        extends ParamSet<M>
        implements RegressionModel {

    private static final long serialVersionUID = 5544999078321108408L;

    public final BiFunction<RegressionModel, Integer, Boolean> DEFAULT_STOPPING_HOOK = (regression, integer) -> false;

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

    public final ValueParam<BiConsumer<RegressionModel, Integer>, M> runningHook = new ValueParam<>((M) this, (m, i) -> {
    },
            "runningHook",
            "Hook executed at each iteration.",
            Objects::nonNull
    );

    public ValueParam<BiFunction<RegressionModel, Integer, Boolean>, M> stoppingHook = new ValueParam<>((M) this,
            DEFAULT_STOPPING_HOOK,
            "stopHook",
            "Hook queried at each iteration if execution should continue or not.",
            Objects::nonNull);

    // model artifacts

    protected boolean hasLearned;
    protected String[] inputNames;
    protected VType[] inputTypes;
    protected String[] targetNames;
    protected VType[] targetTypes;

    @Override
    public String fullName() {
        return name() + '{' + getStringParameterValues(true) + '}';
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
    public M fit(Frame df, String... targetVarNames) {
        VarDouble weights = VarDouble.fill(df.rowCount(), 1).name("weights");
        return fit(df, weights, targetVarNames);
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

    @Override
    public R predict(Frame df) {
        PredSetup setup = preparePredict(df, false);
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
