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
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.math.linear.DMatrix;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.util.param.ValueParam;
import rapaio.ml.model.ClusteringModel;
import rapaio.ml.model.ClusteringResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.svm.libsvm.ModelInfo;
import rapaio.ml.model.svm.libsvm.ProblemInfo;
import rapaio.ml.model.svm.libsvm.Svm;
import rapaio.ml.model.svm.libsvm.SvmModel;

public class OneClassSvm extends ClusteringModel<OneClassSvm, ClusteringResult<OneClassSvm>, RunInfo<ClusteringResult<OneClassSvm>>> {

    private static final Logger LOGGER = Logger.getLogger(OneClassSvm.class.getName());

    public static OneClassSvm newModel() {
        return new OneClassSvm();
    }

    /**
     * Kernel function (default rbf(1)).
     */
    public final ValueParam<Kernel, OneClassSvm> kernel = new ValueParam<>(this, new RBFKernel(1), "kernel");

    /**
     * Tolerance of termination criterion (default 0.001).
     */
    public final ValueParam<Double, OneClassSvm> tolerance =
            new ValueParam<>(this, 0.001, "tolerance", value -> Double.isFinite(value) && value > 0);

    /**
     * Parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5).
     */
    public final ValueParam<Double, OneClassSvm> nu = new ValueParam<>(this, 0.5, "nu", v -> Double.isFinite(v) && v > 0 && v < 1);

    /**
     * Cache size in MB (default 100MB).
     */
    public final ValueParam<Long, OneClassSvm> cacheSize = new ValueParam<>(this, 100L, "cacheSize", size -> size > 0);

    /**
     * Flag for using shrinking heuristics (default true).
     */
    public final ValueParam<Boolean, OneClassSvm> shrinking = new ValueParam<>(this, true, "shrinking");

    private OneClassSvm() {
    }

    @Override
    public ClusteringModel<OneClassSvm, ClusteringResult<OneClassSvm>, RunInfo<ClusteringResult<OneClassSvm>>> newInstance() {
        return new OneClassSvm().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "OneClassSvm";
    }

    private SvmModel model;
    private ProblemInfo problemInfo;
    private ModelInfo modelInfo;

    @Override
    protected OneClassSvm coreFit(Frame df, Var weights) {
        DMatrix x = DMatrix.copy(df.mapVars(inputNames));
        ProblemInfo pi = ProblemInfo.from(x, VarDouble.empty(df.rowCount()), this);
        pi.checkValidProblem();
        model = Svm.svm_train(pi.computeProblem(), pi.computeParameters());
        problemInfo = pi;
        modelInfo = new ModelInfo(pi);
        learned = true;
        return this;
    }

    @Override
    protected ClusteringResult<OneClassSvm> corePredict(Frame df, boolean withScores) {
        DMatrix xs = DMatrix.copy(df.mapVars(inputNames));

        VarInt assign = VarInt.empty(df.rowCount()).name("clusters");
        VarDouble scores = VarDouble.empty(df.rowCount()).name("scores");

        double[] svCoef = model.svCoef[0];
        Kernel k = kernel.get();
        for (int i = 0; i < assign.size(); i++) {
            double score = -model.rho[0];
            for (int j = 0; j < model.l; j++) {
                score += svCoef[j] * k.compute(xs.mapRow(i), model.SV[j]);
            }
            assign.setInt(i, score > 0 ? 1 : 0);
            scores.setDouble(i, score);
        }
        return new ClusteringResult<>(this, df, assign, scores);
    }
}
