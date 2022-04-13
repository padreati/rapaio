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

package rapaio.ml.model.svm;

import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.sys.Experimental;

@Deprecated
@Experimental
public class SvmRegression extends RegressionModel<SvmRegression, RegressionResult, RunInfo<SvmRegression>> {

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
     * -p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)
     */
    public final ValueParam<Double, SvmRegression> p = new ValueParam<>(this, 0.1, "p", v -> Double.isFinite(v) && v >= 0);

    public final ValueParam<Long, SvmRegression> cacheSize = new ValueParam<>(this, 100L, "cacheSize", size -> size > 0);

    /**
     * -e epsilon : set tolerance of termination criterion (default 0.001)
     */
    public final ValueParam<Double, SvmRegression> eps = new ValueParam<>(this, 0.001, "eps", value -> Double.isFinite(value) && value > 0);

    /**
     * -h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1).
     */
    public final ValueParam<Boolean, SvmRegression> shrinking = new ValueParam<>(this, true, "shrinking");

    /**
     * -b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0).
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

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        return false;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, double[] quantiles) {
        return null;
    }
}
