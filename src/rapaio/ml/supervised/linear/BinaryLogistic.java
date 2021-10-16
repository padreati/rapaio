/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.supervised.linear;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.supervised.ClassifierHookInfo;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.ClassifierResult;
import rapaio.ml.supervised.linear.binarylogistic.BinaryLogisticIRLS;
import rapaio.ml.supervised.linear.binarylogistic.BinaryLogisticNewton;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public class BinaryLogistic extends ClassifierModel<BinaryLogistic, ClassifierResult, ClassifierHookInfo> {

    public static BinaryLogistic newModel() {
        return new BinaryLogistic();
    }

    @Serial
    private static final long serialVersionUID = 1609956190070125059L;

    /**
     * Defines the scaling value of the intercept, by default being 1. If the configured value
     * for the intercept is 0, than no intercept is added to the input features.
     */
    public final ValueParam<Double, BinaryLogistic> intercept = new ValueParam<>(this, 1.0,
            "intercept",
            "Value of the intercept column, if zero than no intercept will be added to regression.");

    /**
     * Initialization method is used to give initial weights at the first iteration.
     */
    public final ValueParam<Initialize, BinaryLogistic> init = new ValueParam<>(this, Initialize.EXPECTED_LOG_VAR,
            "init",
            "Initialization method used at first iteration of the algorithm.");
    /**
     * Gets the nominal level used in one vs all strategy. If the target variable
     * is a nominal variable with more than 2 levels (ignoring missing value level),
     * then the logistic regression is transformed into a binary classification
     * problem by considering this specified target level as the positive case, and
     * all the other levels as the negative case.
     */
    public final ValueParam<String, BinaryLogistic> nominalLevel = new ValueParam<>(this, "",
            "nominalLevel",
            "Nominal level used in one vs all strategy. If the target variable is a nominal variable with more than 2 levels " +
                    "(ignoring missing value level) then the logistic regression is transformed into a binary classification problem" +
                    " by considering this specified target level as the positive case, and all the other levels as the negative case.");

    public final ValueParam<Method, BinaryLogistic> solver = new ValueParam<>(this, Method.IRLS,
            "solver",
            "Solver used to fit binary logistic regression.");

    public final ValueParam<Double, BinaryLogistic> l1Factor = new ValueParam<>(this, 0.0,
            "l1factor",
            "L1 regularization factor");

    public final ValueParam<Double, BinaryLogistic> l2Factor = new ValueParam<>(this, 0.0,
            "l2factor",
            "L2 regularization factor");

    public final ValueParam<Double, BinaryLogistic> eps = new ValueParam<>(this, 1e-10,
            "eps",
            "Tolerance threshold used to signal when fit iterative procedure converged");

    // learning artifacts

    /**
     * True if the model is trained and it has converged to a solution in less than
     * maximum number of iterations (runs), false otherwise.
     */
    private boolean converged = false;
    private VarDouble w;
    /**
     * List of coefficients computed at each iteration.
     */
    private List<DVector> iterationWeights;

    /**
     * List of loss function values evaluated after each iteration.
     */
    private List<Double> iterationLoss;

    private BinaryLogistic() {
    }

    @Override
    public BinaryLogistic newInstance() {
        return new BinaryLogistic().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "BinaryLogistic";
    }

    public boolean isConverged() {
        return converged;
    }

    public List<DVector> getIterationWeights() {
        return iterationWeights;
    }

    public List<Double> getIterationLoss() {
        return iterationLoss;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, 10_000, List.of(VarType.BINARY, VarType.INT, VarType.DOUBLE), false,
                1, 1, List.of(VarType.NOMINAL, VarType.BINARY), false);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        DMatrix x = computeInputMatrix(df, firstTargetName());
        DVector y = computeTargetVector(df.rvar(firstTargetName()));
        DVector w0 = DVector.fill(x.colCount(), init.get().getFunction().apply(y));

        switch (solver.get()) {
            case IRLS -> {
                BinaryLogisticIRLS.Result irlsResult = new BinaryLogisticIRLS()
                        .eps.set(eps.get())
                        .maxIter.set(runs.get())
                        .lambda.set(l2Factor.get())
                        .x.set(x)
                        .y.set(y)
                        .w0.set(w0)
                        .fit();
                w = irlsResult.getW().asVarDouble();
                iterationLoss = new ArrayList<>(irlsResult.nlls());
                iterationWeights = new ArrayList<>(irlsResult.ws());
                converged = irlsResult.converged();
            }
            case NEWTON -> {
                BinaryLogisticNewton.Result newtonResult = new BinaryLogisticNewton()
                        .eps.set(eps.get())
                        .maxIter.set(runs.get())
                        .lambda.set(l2Factor.get())
                        .x.set(x)
                        .y.set(y)
                        .w0.set(w0)
                        .fit();
                w = newtonResult.w().asVarDouble();
                iterationLoss = new ArrayList<>(newtonResult.nll());
                iterationWeights = new ArrayList<>(newtonResult.ws());
                converged = newtonResult.converged();
            }
        }
        return true;
    }

    private DVector computeTargetVector(Var target) {
        switch (target.type()) {
            case BINARY:
                return DVector.from(target);
            case NOMINAL:
                DVector result = DVector.zeros(target.size());
                if (targetLevels.get(firstTargetName()).size() == 3) {
                    for (int i = 0; i < target.size(); i++) {
                        result.set(i, target.getInt(i) - 1);
                    }
                } else {
                    for (int i = 0; i < target.size(); i++) {
                        result.set(i, target.getLabel(i).equals(nominalLevel.get()) ? 1 : 0);
                    }
                }
                return result;
        }
        return null;
    }

    private DMatrix computeInputMatrix(Frame df, String targetName) {
        List<Var> variables = new ArrayList<>();
        if (intercept.get() != 0) {
            variables.add(VarDouble.fill(df.rowCount(), intercept.get()).name("Intercept"));
        }
        df.varStream()
                .filter(v -> !firstTargetName().equals(v.name()))
                .forEach(variables::add);
        return DMatrix.copy(variables.toArray(Var[]::new));
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        if (!hasLearned()) {
            throw new IllegalArgumentException("Model has not been trained");
        }

        ClassifierResult cr = ClassifierResult.build(this, df, withClasses, withDistributions);

        int offset = intercept.get() == 0 ? 0 : 1;

        VarDouble p = VarDouble.fill(df.rowCount(), intercept.get() * w.getDouble(0));
        for (int i = 0; i < inputNames.length; i++) {
            double wvalue = w.getDouble(i + offset);
            VarDouble z = df.rvar(inputName(i)).op().capply(v -> 1 / (1 + Math.exp(-v * wvalue)));
            p.op().plus(z);
        }

        for (int r = 0; r < df.rowCount(); r++) {
            double pi = p.getDouble(r);
            if (withClasses) {
                cr.firstClasses().setInt(r, pi < 0.5 ? 1 : 2);
            }
            if (withDistributions) {
                cr.firstDensity().setDouble(r, 1, 1 - pi);
                cr.firstDensity().setDouble(r, 2, pi);
            }
        }
        return cr;
    }

    public enum Method {
        IRLS,
        NEWTON
    }

    public enum Initialize implements Serializable {
        ZERO(v -> 0.0),
        ONE(v -> 1.0),
        EXPECTED_LOG_VAR(v -> Math.log(v.mean() * (1 - v.mean())));

        private static final long serialVersionUID = 8945270404852488614L;
        private final Function<DVector, Double> function;

        Initialize(Function<DVector, Double> function) {
            this.function = function;
        }

        public Function<DVector, Double> getFunction() {
            return function;
        }
    }
}
