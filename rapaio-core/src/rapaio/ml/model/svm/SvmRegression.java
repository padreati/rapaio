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

package rapaio.ml.model.svm;

import java.util.logging.Logger;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.linear.DMatrix;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.core.param.ValueParam;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.svm.libsvm.ModelInfo;
import rapaio.ml.model.svm.libsvm.ProblemInfo;
import rapaio.ml.model.svm.libsvm.Svm;
import rapaio.ml.model.svm.libsvm.SvmModel;

public class SvmRegression extends RegressionModel<SvmRegression, RegressionResult, RunInfo<SvmRegression>> {

    private static final Logger LOGGER = Logger.getLogger(SvmRegression.class.getName());

    public enum Penalty {
        C,
        NU
    }

    public final ValueParam<Penalty, SvmRegression> type = new ValueParam<>(this, Penalty.C, "penalty");

    /**
     * Kernel function.
     */
    public final ValueParam<Kernel, SvmRegression> kernel = new ValueParam<>(this, new RBFKernel(1), "kernel");

    /**
     * -c cost : set the parameter c of c-SVC, epsilon-SVR, and nu-SVR (default 1).
     */
    public final ValueParam<Double, SvmRegression> c = new ValueParam<>(this, 1.0, "cost", value -> Double.isFinite(value) && value > 0);

    /**
     * -n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
     */
    public final ValueParam<Double, SvmRegression> nu = new ValueParam<>(this, 0.5, "nu", v -> Double.isFinite(v) && v > 0 && v < 1);

    /**
     * Parameter epsilon in loss function of epsilon-SVR (default 0.1)
     */
    public final ValueParam<Double, SvmRegression> epsilon = new ValueParam<>(this, 0.1, "epsilon", v -> Double.isFinite(v) && v >= 0);

    /**
     * Cache size in MB (default 100MB).
     */
    public final ValueParam<Long, SvmRegression> cacheSize = new ValueParam<>(this, 100L, "cacheSize", size -> size > 0);

    /**
     * Tolerance of termination criterion (default 0.001).
     */
    public final ValueParam<Double, SvmRegression> tolerance = new ValueParam<>(this, 0.001,
            "tolerance", value -> Double.isFinite(value) && value > 0);

    /**
     * Flag for use of shrinking heuristics (default true).
     */
    public final ValueParam<Boolean, SvmRegression> shrinking = new ValueParam<>(this, true, "shrinking");

    /**
     * Whether to train a SVC or SVR model for probability estimates (default false).
     */
    public final ValueParam<Boolean, SvmRegression> probability = new ValueParam<>(this, false, "probability");

    @Override
    public RegressionModel<SvmRegression, RegressionResult, RunInfo<SvmRegression>> newInstance() {
        SvmRegression copy = new SvmRegression();
        copy.copyParameterValues(this);
        return copy;
    }

    @Override
    public String name() {
        return "SvmRegression";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 1_000_000, false, VarType.DOUBLE, VarType.BINARY, VarType.INT)
                .targets(1, 1, false, VarType.DOUBLE, VarType.BINARY, VarType.INT);
    }

    private SvmModel svm_model;
    private ProblemInfo problemInfo;
    private ModelInfo modelInfo;

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        DMatrix x = DMatrix.copy(df.mapVars(inputNames));
        Var target = df.rvar(firstTargetName());

        ProblemInfo pi = ProblemInfo.from(x, target, this);

        pi.checkValidProblem();

        svm_model = Svm.svm_train(pi.computeProblem(), pi.computeParameters());
        problemInfo = pi;

        modelInfo = new ModelInfo(pi);

        return true;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, double[] quantiles) {
        RegressionResult result = RegressionResult.build(this, df, withResiduals, quantiles);
        DMatrix xs = DMatrix.copy(df.mapVars(inputNames));
        for (int i = 0; i < xs.rows(); i++) {
            double score = Svm.svm_predict(svm_model, xs.mapRow(i));

            LOGGER.finest("i:%d, score:%f".formatted(i, score));

            result.firstPrediction().setDouble(i, score);
        }
        return result;
    }
}
