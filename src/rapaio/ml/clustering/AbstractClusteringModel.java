/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.clustering;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.util.function.SBiConsumer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/31/20.
 */
public abstract class AbstractClusteringModel<M extends AbstractClusteringModel<M, R>, R extends ClusteringResult>
        extends ParamSet<M> implements ClusteringModel, Serializable {

    @Serial
    private static final long serialVersionUID = 1917244313225596517L;

    public final SBiConsumer<ClusteringModel, Integer> DEFAULT_RUNNING_HOOK = (m, i) -> {
    };

    /**
     * Specifies the runs / rounds of learning.
     * For various models composed of multiple sub-models
     * the runs represents often the number of sub-models.
     * <p>
     * For example for CForest the number of runs is used to specify
     * the number of decision trees to be built.
     */
    @SuppressWarnings("unchecked")
    public final ValueParam<Integer, M> runs = new ValueParam<>((M) this, 1_000,
            "runs",
            "Number of iterations for iterative iterations or number of sub ensembles.",
            x -> x > 0
    );

    @SuppressWarnings("unchecked")
    public final ValueParam<SBiConsumer<ClusteringModel, Integer>, M> runningHook = new ValueParam<>((M) this, DEFAULT_RUNNING_HOOK,
            "runningHook", "Running hook");

    protected String[] inputNames;
    protected VarType[] inputTypes;
    protected boolean learned = false;

    @Override
    public String fullName() {
        return name() + '{' + getStringParameterValues(true) + '}';
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
    public boolean hasLearned() {
        return learned;
    }

    @Override
    public M fit(Frame df) {
        return fit(df, VarDouble.fill(df.rowCount(), 1));
    }

    @Override
    @SuppressWarnings("unchecked")
    public M fit(Frame df, Var weights) {
        FitSetup fitSetup = prepareFit(df, weights);
        return (M) coreFit(fitSetup.df, fitSetup.weights);
    }

    public FitSetup prepareFit(Frame df, Var weights) {
        List<String> inputs = Arrays.asList(df.varNames());
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(name -> df.rvar(name).type()).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(df, weights);
        return new FitSetup(df, weights);
    }

    public abstract ClusteringModel coreFit(Frame df, Var weights);

    @Override
    @SuppressWarnings("unchecked")
    public R predict(Frame df, boolean withScores) {
        return corePredict(df, withScores);
    }

    public abstract R corePredict(Frame df, boolean withScores);

    private record FitSetup(Frame df, Var weights) {
    }
}
