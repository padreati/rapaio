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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.math.linear.DMatrix;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ListParam;
import rapaio.ml.common.MultiParam;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.svm.libsvm.ModelInfo;
import rapaio.ml.model.svm.libsvm.ProblemInfo;
import rapaio.ml.model.svm.libsvm.Svm;
import rapaio.ml.model.svm.libsvm.SvmModel;
import rapaio.util.collection.DoubleArrays;

public class SVMClassifier extends ClassifierModel<SVMClassifier, ClassifierResult, RunInfo<SVMClassifier>> {

    private static final Logger LOGGER = Logger.getLogger(SVMClassifier.class.getName());

    public enum Penalty {
        C,
        NU
    }

    public final ValueParam<Penalty, SVMClassifier> type = new ValueParam<>(this, Penalty.C, "penalty");

    /**
     * Kernel function.
     */
    public final ValueParam<Kernel, SVMClassifier> kernel = new ValueParam<>(this, new RBFKernel(1), "kernel");

    /**
     * -c cost : set the parameter c of c-SVC, epsilon-SVR, and nu-SVR (default 1).
     */
    public final ValueParam<Double, SVMClassifier> c = new ValueParam<>(this, 1.0, "cost", value -> Double.isFinite(value) && value > 0);

    /**
     * -n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
     */
    public final ValueParam<Double, SVMClassifier> nu = new ValueParam<>(this, 0.5, "nu", v -> Double.isFinite(v) && v > 0 && v < 1);

    /**
     * -p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)
     */
    public final ValueParam<Double, SVMClassifier> p = new ValueParam<>(this, 0.1, "p", v -> Double.isFinite(v) && v >= 0);

    public final ValueParam<Long, SVMClassifier> cacheSize = new ValueParam<>(this, 100L, "cacheSize", size -> size > 0);

    /**
     * -e epsilon : set tolerance of termination criterion (default 0.001)
     */
    public final ValueParam<Double, SVMClassifier> eps = new ValueParam<>(this, 0.001, "eps", value -> Double.isFinite(value) && value > 0);

    /**
     * -h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1).
     */
    public final ValueParam<Boolean, SVMClassifier> shrinking = new ValueParam<>(this, true, "shrinking");

    /**
     * -b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0).
     */
    public final ValueParam<Boolean, SVMClassifier> probability = new ValueParam<>(this, false, "probability");

    /**
     * Training levels used for classification. If this is not specified than the training levels are determined
     * automatically from the levels of the target variable, in the same order, excluding the missing level.
     * <p>
     * If one specifies explicitly the training levels here, only those levels will be used for classification.
     */
    public final ListParam<String, SVMClassifier> levels = new ListParam<>(this, List.of(), "levels", (l1, l2) -> l2 != null);

    /**
     * Weighting specific for each training level. The effect will be that the penalty coefficient for each target level
     * will be multiplied with corresponding weighting level. If a target label weighting is specified, but it is missing from
     * {@link #targetLevels} than the weighting will be ignored. If a target label weighting does not exist for a target label
     * specified in {@link #targetLevels}, than a default value of {@code 1} is considered. The default value for this
     * parameter is an empty map, which is equivalent with all weighting equal {@code 1}.
     */
    public final MultiParam<String, Double, SVMClassifier> wi = new MultiParam<>(this, Map.of(), "wi", Objects::nonNull);

    private SvmModel svm_model;
    private ProblemInfo problemInfo;
    private ModelInfo modelInfo;

    @Override
    public ClassifierModel<SVMClassifier, ClassifierResult, RunInfo<SVMClassifier>> newInstance() {
        return new SVMClassifier().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "SVMClassifier";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 10_000, false, VarType.DOUBLE, VarType.INT, VarType.BINARY)
                .targets(1, 1, false, VarType.NOMINAL, VarType.BINARY, VarType.INT);
    }

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
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {

        ClassifierResult result = ClassifierResult.build(this, df, withClasses, withDistributions);
        DMatrix xs = DMatrix.copy(df.mapVars(inputNames));
        for (int i = 0; i < xs.rows(); i++) {
            int k = problemInfo.levels().size();

            if (probability.get()) {
                double[] prob = new double[k];
                double score = Svm.svm_predict_probability(svm_model, xs.mapRow(i), prob);

                LOGGER.finest("i:%d, score:%f, values:%s".formatted(
                        i, score, Arrays.stream(prob).mapToObj(String::valueOf).collect(Collectors.joining(","))));

                result.firstClasses().setLabel(i, problemInfo.levels().get((int) score));
                for (int j = 0; j < k; j++) {
                    result.firstDensity().setDouble(i, j + 1, prob[j]);
                }
            } else {
                double[] values = new double[k * (k - 1) / 2];
                double score = Svm.svm_predict_values(svm_model, xs.mapRow(i), values);

                LOGGER.finest("i:%d, score:%f, values:%s".formatted(
                        i, score, Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","))));

                result.firstClasses().setLabel(i, problemInfo.levels().get((int) score));
                for (int j = 0; j < k; j++) {
                    result.firstDensity().setDouble(i, j + 1, values[j]);
                }

                double[] dist = DoubleArrays.newFill(k, Double.NEGATIVE_INFINITY);
                int pos = 0;
                for (int j = 0; j < k; j++) {
                    for (int l = j + 1; l < k; l++) {
                        double val = values[pos++];
                        dist[j] = Math.max(val, dist[j]);
                        dist[l] = Math.max(-val, dist[l]);
                    }
                }
                result.firstClasses().setLabel(i, problemInfo.levels().get((int) score));
                for (int j = 0; j < k; j++) {
                    result.firstDensity().setDouble(i, j + 1, dist[j]);
                }
            }
        }
        return result;
    }
}
