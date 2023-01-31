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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.ml.model.linear;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.math.MathTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.ml.common.Capabilities;
import rapaio.util.param.ValueParam;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.linear.binarylogistic.BinaryLogisticIRLS;
import rapaio.ml.model.linear.binarylogistic.BinaryLogisticNewton;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.printer.opt.POtpionFloatFormat;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public class BinaryLogistic extends ClassifierModel<BinaryLogistic, ClassifierResult, RunInfo<BinaryLogistic>> {

    public static BinaryLogistic newModel() {
        return new BinaryLogistic();
    }

    @Serial
    private static final long serialVersionUID = 1609956190070125059L;

    /**
     * Defines the scaling value of the intercept, by default being 1. If the configured value
     * for the intercept is 0, then no intercept is added to the input features.
     */
    public final ValueParam<Double, BinaryLogistic> intercept = new ValueParam<>(this, 1.0, "intercept");

    /**
     * Initialization method is used to give initial weights at the first iteration.
     */
    public final ValueParam<Initialize, BinaryLogistic> init = new ValueParam<>(this, Initialize.ZERO, "init");
    /**
     * Gets the nominal level used in one vs all strategy. If the target variable
     * is a nominal variable with more than 2 levels (ignoring missing value level),
     * then the logistic regression is transformed into a binary classification
     * problem by considering this specified target level as the positive case, and
     * all the other levels as the negative case.
     */
    public final ValueParam<String, BinaryLogistic> nominalLevel = new ValueParam<>(this, "", "nominalLevel");

    /**
     * Solver used to fit binary logistic regression.
     */
    public final ValueParam<Method, BinaryLogistic> solver = new ValueParam<>(this, Method.IRLS, "solver");

    /**
     * L1 regularization factor
     */
    public final ValueParam<Double, BinaryLogistic> l1penalty = new ValueParam<>(this, 0.0, "l1penalty");

    /**
     * L2 regularization factor
     */
    public final ValueParam<Double, BinaryLogistic> l2penalty = new ValueParam<>(this, 0.0, "l2penalty");

    /**
     * Tolerance threshold used to signal when fit iterative procedure converged
     */
    public final ValueParam<Double, BinaryLogistic> eps = new ValueParam<>(this, 1e-10, "eps");

    // learning artifacts

    // True if the model is trained and has converged to a solution in less than maximum number of iterations (runs), false otherwise.
    private boolean converged = false;

    // true if the fitted model had an intercept
    private boolean hasIntercept;

    // fitted labels
    private String positiveLabel;
    private String negativeLabel;

    // Coefficients for the model
    private VarDouble w;

    // List of coefficients computed at each iteration.
    private List<DVector> iterationWeights;

    // List of loss function values evaluated after each iteration.
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

    public List<DVector> iterationWeights() {
        return iterationWeights;
    }

    public List<Double> iterationLoss() {
        return iterationLoss;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 10_000, false, VarType.BINARY, VarType.INT, VarType.DOUBLE)
                .targets(1, 1, false, VarType.NOMINAL, VarType.BINARY);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        DMatrix x = computeInputMatrix(df, firstTargetName());
        DVector y = computeTargetVector(df.rvar(firstTargetName()));
        DVector w0 = DVector.fill(x.cols(), init.get().getFunction().apply(y));

        switch (solver.get()) {
            case IRLS -> {
                BinaryLogisticIRLS.Result irlsResult = new BinaryLogisticIRLS()
                        .eps.set(eps.get())
                        .maxIter.set(runs.get())
                        .lambdap.set(l2penalty.get())
                        .xp.set(x)
                        .yp.set(y)
                        .w0.set(w0)
                        .fit();
                w = irlsResult.w().dv();
                iterationLoss = new ArrayList<>(irlsResult.nlls());
                iterationWeights = new ArrayList<>(irlsResult.ws());
                converged = irlsResult.converged();
            }
            case NEWTON -> {
                BinaryLogisticNewton.Result newtonResult = new BinaryLogisticNewton()
                        .eps.set(eps.get())
                        .maxIter.set(runs.get())
                        .lambdap.set(l2penalty.get())
                        .xp.set(x)
                        .yp.set(y)
                        .w0.set(w0)
                        .fit();
                w = newtonResult.w().dv();
                iterationLoss = new ArrayList<>(newtonResult.nlls());
                iterationWeights = new ArrayList<>(newtonResult.ws());
                converged = newtonResult.converged();
            }
        }
        return true;
    }

    private DVector computeTargetVector(Var target) {
        switch (target.type()) {
            case BINARY -> {
                positiveLabel = "1";
                negativeLabel = "0";
                return target.dv();
            }
            case NOMINAL -> {
                DVector result = DVector.zeros(target.size());
                positiveLabel = !Objects.equals(nominalLevel.get(), "") ? nominalLevel.get() : targetLevels.get(firstTargetName()).get(1);
                negativeLabel = firstTargetLevels().stream()
                        .filter(label -> !label.equals(positiveLabel))
                        .filter(label -> !label.equals("?")).findFirst().orElse("?");
                for (int i = 0; i < target.size(); i++) {
                    if (target.isMissing(i)) {
                        throw new IllegalArgumentException("Target variable does not allow missing values.");
                    }
                    result.set(i, positiveLabel.equals(target.getLabel(i)) ? 1 : 0);
                }
                return result;
            }
        }
        return null;
    }

    private DMatrix computeInputMatrix(Frame df, String targetName) {
        List<Var> variables = new ArrayList<>();
        if (intercept.get() != 0) {
            hasIntercept = true;
            variables.add(VarDouble.fill(df.rowCount(), intercept.get()).name("Intercept"));
        } else {
            hasIntercept = false;
        }
        df.varStream()
                .filter(v -> !targetName.equals(v.name()))
                .forEach(variables::add);
        return DMatrixDenseC.copy(variables.toArray(Var[]::new));
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        if (!hasLearned()) {
            throw new IllegalArgumentException("Model has not been trained");
        }

        ClassifierResult cr = ClassifierResult.build(this, df, withClasses, withDistributions);

        int offset = hasIntercept ? 1 : 0;

        DVector p = DVector.fill(df.rowCount(), hasIntercept ? intercept.get() * w.getDouble(0) : 0);
        for (int i = 0; i < inputNames.length; i++) {
            p.fma(w.getDouble(i + offset), df.rvar(inputName(i)).dv());
        }
        p.apply(MathTools::logistic);

        for (int r = 0; r < df.rowCount(); r++) {
            double pi = p.get(r);
            if (withClasses) {
                cr.firstClasses().setLabel(r, pi > 0.5 ? positiveLabel : negativeLabel);
            }
            if (withDistributions) {
                cr.firstDensity().setDouble(r, 1, pi);
                cr.firstDensity().setDouble(r, 2, 1 - pi);
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
        EXPECTED_LOG_VAR(v -> Math.log(Math.min(1e-12, v.mean() * (1 - v.mean()))));

        private static final long serialVersionUID = 8945270404852488614L;
        private final Function<DVector, Double> function;

        Initialize(Function<DVector, Double> function) {
            this.function = function;
        }

        public Function<DVector, Double> getFunction() {
            return function;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName());
        sb.append(", hasLearned=").append(hasLearned());
        if (hasLearned()) {
            sb.append(", converged=").append(converged);
            sb.append(", iterations=").append(iterationLoss.size());
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {

        var opts = printer.getOptions();
        opts.setFloatFormat(new POtpionFloatFormat(Format.floatFlex()));
        opts.bind(options);

        StringBuilder sb = new StringBuilder();
        sb.append(fullNameSummary());
        sb.append(capabilitiesSummary());
        sb.append(inputVarsSummary(printer, options));
        sb.append(targetVarsSummary());

        sb.append("Learning data:\n");
        sb.append("> has learned: ").append(hasLearned()).append("\n");
        if (hasLearned()) {
            sb.append("> has intercept: ").append(hasIntercept).append("\n");
            if (hasIntercept) {
                sb.append("> intercept factor: ").append(opts.floatFormat().format(intercept.get())).append("\n");
            }
            sb.append("> coefficients:\n");
            TextTable tt = TextTable.empty(w.size() + 1, 2, 1, 1);
            tt.textCenter(0, 0, "Name");
            tt.textCenter(0, 1, "Value");
            for (int i = 0; i < w.size(); i++) {
                tt.textRight(i, 0, hasIntercept ? (i == 0 ? "intercept" : inputName(i - 1)) : inputName(i));
                tt.floatString(i, 1, opts.floatFormat().format(w.getDouble(i)));
            }
            sb.append(tt.getDynamicText(printer, options));
            sb.append("> converged: ").append(converged).append("\n");
            sb.append("> iterations: ").append(iterationLoss.size()).append("\n");


        }
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }
}
