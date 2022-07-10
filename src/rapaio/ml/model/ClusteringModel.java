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
import java.util.List;
import java.util.Objects;
import java.util.Random;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.param.ParamSet;
import rapaio.ml.common.param.ValueParam;
import rapaio.printer.Printable;
import rapaio.util.function.SConsumer;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/31/20.
 */
public abstract class ClusteringModel<M extends ClusteringModel<M, R, H>, R extends ClusteringResult<M>, H>
        extends ParamSet<M> implements Printable, Serializable {

    @Serial
    private static final long serialVersionUID = 1917244313225596517L;

    /**
     * Specifies the runs / rounds of learning.
     * For various models composed of multiple sub-models
     * the runs represents the number of sub-models.
     */
    @SuppressWarnings("unchecked")
    public final ValueParam<Integer, M> runs = new ValueParam<>((M) this, 1_000, "runs", x -> x > 0);

    public final ValueParam<Long, M> seed = new ValueParam<>((M) this, 0L, "seed", Objects::nonNull);

    /**
     * Running hook.
     */
    @SuppressWarnings("unchecked")
    public final ValueParam<SConsumer<H>, M> runningHook = new ValueParam<>((M) this, h -> {}, "runningHook");

    protected String[] inputNames;
    protected VarType[] inputTypes;
    protected boolean learned = false;

    /**
     * Creates a new clustering instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    public abstract ClusteringModel<M, R, H> newInstance();

    /**
     * Returns the clustering algorithm name.
     *
     * @return clustering name
     */
    public abstract String name();

    /**
     * Builds a string which contains the clustering instance name and parameters.
     *
     * @return clustering algorithm name and parameters
     */
    public String fullName() {
        return name() + '{' + getStringParameterValues(true) + '}';
    }

    /**
     * Describes the clustering algorithm.
     *
     * @return capabilities of the clustering algorithm
     */
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 1_000_000, false, VarType.DOUBLE, VarType.INT, VarType.BINARY)
                .targets(0, 0, true);
    }

    /**
     * Returns input variable names built at learning time.
     *
     * @return input variable names
     */
    public String[] inputNames() {
        return inputNames;
    }

    /**
     * Shortcut method which returns input variable name at the given position.
     *
     * @param pos given position
     * @return variable name
     */
    public String inputName(int pos) {
        return inputNames()[pos];
    }

    /**
     * Returns the types of input variables built at learning time.
     *
     * @return array of input variable types
     */
    public VarType[] inputTypes() {
        return inputTypes;
    }

    /**
     * Shortcut method which returns the type of the input variable at the given position.
     *
     * @param pos given position
     * @return variable type
     */
    public VarType inputType(int pos) {
        return inputTypes()[pos];
    }

    /**
     * Informs  if the algorithm was fitted successfully.
     */
    public boolean hasLearned() {
        return learned;
    }

    /**
     * Fit a clustering model on instances specified by frame, with instance weights
     * equal to 1 and target specified by targetNames.
     *
     * @param df data set instances
     */
    public M fit(Frame df) {
        return fit(df, VarDouble.fill(df.rowCount(), 1));
    }

    /**
     * Fit a clustering on instances specified by frame, with row weights and targetNames.
     *
     * @param df      predict frame
     * @param weights instance weights
     */
    public M fit(Frame df, Var weights) {
        FitSetup fitSetup = prepareFit(df, weights);
        return coreFit(fitSetup.df, fitSetup.weights);
    }


    /**
     * Predict clusters for new data set instances, with
     * default option to compute probability scores if they are available.
     *
     * @param df data set instances
     */
    public R predict(Frame df) {
        return predict(df, true);
    }

    public R predict(DVector x) {
        return predict(x, true);
    }

    public R predict(DVector x, boolean withScores) {
        if (!hasLearned()) {
            throw new IllegalStateException("Model was not fit on data, cannot predict.");
        }
        Var[] vars = new Var[inputTypes.length];
        for (int i = 0; i < inputTypes.length; i++) {
            vars[i] = inputType(i).newInstance(1).name(inputName(i));
            vars[i].setDouble(0, x.get(i));
        }
        return predict(SolidFrame.byVars(vars), withScores);
    }

    /**
     * Predict clusters for given instances, generating also scores if it is specified.
     *
     * @param df         frame instances
     * @param withScores generate classes
     */
    public R predict(Frame df, boolean withScores) {
        return corePredict(df, withScores);
    }

    private FitSetup prepareFit(Frame df, Var weights) {
        List<String> inputs = Arrays.asList(df.varNames());
        this.inputNames = inputs.toArray(new String[0]);
        this.inputTypes = inputs.stream().map(name -> df.rvar(name).type()).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(df, weights);
        return new FitSetup(df, weights);
    }

    protected abstract M coreFit(Frame df, Var weights);

    protected abstract R corePredict(Frame df, boolean withScores);

    private record FitSetup(Frame df, Var weights) {
    }

    protected Random getRandom() {
        return seed.get() == 0 ? new Random() : new Random(seed.get());
    }
}
