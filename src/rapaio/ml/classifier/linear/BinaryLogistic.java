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

package rapaio.ml.classifier.linear;

import lombok.Getter;
import lombok.ToString;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.math.linear.dense.SolidDVector;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.classifier.linear.binarylogistic.BinaryLogisticIRLS;
import rapaio.ml.classifier.linear.binarylogistic.BinaryLogisticNewton;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public class BinaryLogistic extends AbstractClassifierModel<BinaryLogistic, ClassifierResult>
        implements Printable {

    public static BinaryLogistic newModel() {
        return new BinaryLogistic();
    }

    private static final long serialVersionUID = 1609956190070125059L;

    /**
     * Defines the scaling value of the intercept, by default being 1. If the configured value
     * for the intercept is 0, than no intercept is added to the input features.
     */
    public ValueParam<Double, BinaryLogistic> intercept = new ValueParam<>(this, 1.0,
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
    public ValueParam<String, BinaryLogistic> nominalLevel = new ValueParam<>(this, "",
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

    private boolean converged = false;
    private VarDouble w;
    private List<DVector> iterationWeights;
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

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .inputTypes(Arrays.asList(VType.BINARY, VType.INT, VType.DOUBLE))
                .minInputCount(1).maxInputCount(10000)
                .targetType(VType.NOMINAL)
                .targetType(VType.BINARY)
                .minTargetCount(1).maxTargetCount(1)
                .allowMissingInputValues(false)
                .allowMissingTargetValues(false)
                .build();
    }

    /**
     * Numbers of iterations used to fit the model.
     *
     * @return number of iterations used to fit the model
     */
    public int getIterations() {
        return iterationLoss != null ? iterationLoss.size() : 0;
    }

    /**
     * List of loss function values evaluated after each iteration.
     *
     * @return list loss function values
     */
    public List<Double> getIterationLoss() {
        return iterationLoss;
    }

    /**
     * List of coefficients computed at each iteration.
     *
     * @return coefficients from each iteration
     */
    public List<DVector> getIterationWeights() {
        return iterationWeights;
    }

    /**
     * True if the model is trained and it has converged to a solution in less than
     * maximum number of iterations (runs), false otherwise.
     *
     * @return true if the model is trained and has converged to a solution in less than maximum
     * number of iterations
     */
    public boolean isConverged() {
        return converged;
    }

    private SolidDVector computeTargetVector(Var target) {
        switch (target.type()) {
            case BINARY:
                return SolidDVector.from(target);
            case NOMINAL:
                SolidDVector result = SolidDVector.zeros(target.rowCount());
                if (targetLevels.get(firstTargetName()).size() == 3) {
                    for (int i = 0; i < target.rowCount(); i++) {
                        result.set(i, target.getInt(i) - 1);
                    }
                } else {
                    for (int i = 0; i < target.rowCount(); i++) {
                        result.set(i, target.getLabel(i).equals(nominalLevel.get()) ? 1 : 0);
                    }
                }
                return result;
            default:
                throw new IllegalArgumentException("Target variable must be nominal or binary.");
        }
    }

    private SolidDMatrix computeInputMatrix(Frame df, String targetName) {
        List<Var> variables = new ArrayList<>();
        if (intercept.get() != 0) {
            variables.add(VarDouble.fill(df.rowCount(), intercept.get()).withName("Intercept"));
        }
        df.varStream()
                .filter(v -> !firstTargetName().equals(v.name()))
                .forEach(variables::add);
        return SolidDMatrix.copy(variables.toArray(Var[]::new));
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        SolidDMatrix x = computeInputMatrix(df, firstTargetName());
        SolidDVector y = computeTargetVector(df.rvar(firstTargetName()));
        SolidDVector w0 = SolidDVector.fill(x.colCount(), init.get().getFunction().apply(y));

        switch (solver.get()) {
            case IRLS:
                BinaryLogisticIRLS.Result irlsResult = BinaryLogisticIRLS.builder()
                        .withEps(eps.get())
                        .withMaxIter(runs.get())
                        .withLambda(l2Factor.get())
                        .withX(x)
                        .withY(y)
                        .withW0(w0)
                        .build()
                        .fit();
                w = irlsResult.getW().asVarDouble();
                iterationLoss = new ArrayList<>(irlsResult.getNlls());
                iterationWeights = new ArrayList<>(irlsResult.getWs());
                converged = irlsResult.isConverged();
                break;
            case NEWTON:
                BinaryLogisticNewton.Result newtonResult = BinaryLogisticNewton.builder()
                        .withEps(eps.get())
                        .withMaxIter(runs.get())
                        .withLambda(l2Factor.get())
                        .withX(x)
                        .withY(y)
                        .withW0(w0)
                        .build()
                        .fit();
                w = newtonResult.getW().asVarDouble();
                iterationLoss = new ArrayList<>(newtonResult.getNll());
                iterationWeights = new ArrayList<>(newtonResult.getWs());
                converged = newtonResult.isConverged();
                break;

            default:
                throw new IllegalArgumentException("Method not implemented.");
        }
        return true;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        if (w == null) {
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

    @Getter
    @ToString(exclude = {"function"})
    public enum Initialize implements Serializable {
        ZERO("Zero", v -> 0.0),
        ONE("One", v -> 1.0),
        EXPECTED_LOG_VAR("ExpectedLogVariance", v -> Math.log(v.mean() * (1 - v.mean())));

        private final String name;
        private static final long serialVersionUID = 8945270404852488614L;

        private final Function<DVector, Double> function;

        Initialize(String name, Function<DVector, Double> function) {
            this.name = name;
            this.function = function;
        }
    }
}
