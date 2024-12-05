/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import rapaio.core.param.ListParam;
import rapaio.core.param.MultiParam;
import rapaio.core.param.ValueParam;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.narray.NArray;
import rapaio.ml.common.Capabilities;
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

public class SvmClassifier extends ClassifierModel<SvmClassifier, ClassifierResult, RunInfo<SvmClassifier>> {

    public enum Penalty {
        C,
        NU
    }

    public final ValueParam<Penalty, SvmClassifier> type = new ValueParam<>(this, Penalty.C, "penalty");

    /**
     * Kernel function (default rbf(1)).
     */
    public final ValueParam<Kernel, SvmClassifier> kernel = new ValueParam<>(this, new RBFKernel(1), "kernel");

    /**
     * Parameter C of c-SVC, epsilon-SVR, and nu-SVR (default 1).
     */
    public final ValueParam<Double, SvmClassifier> c = new ValueParam<>(this, 1.0, "cost", value -> Double.isFinite(value) && value > 0);

    /**
     * Parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5).
     */
    public final ValueParam<Double, SvmClassifier> nu = new ValueParam<>(this, 0.5, "nu", v -> Double.isFinite(v) && v > 0 && v < 1);

    /**
     * Cache size in MB (default 100MB).
     */
    public final ValueParam<Long, SvmClassifier> cacheSize = new ValueParam<>(this, 100L, "cacheSize", size -> size > 0);

    /**
     * Tolerance of termination criterion (default 0.001).
     */
    public final ValueParam<Double, SvmClassifier> tolerance = new ValueParam<>(this, 0.001,
            "tolerance", value -> Double.isFinite(value) && value > 0);

    /**
     * Flag for use of shrinking heuristics (default true).
     */
    public final ValueParam<Boolean, SvmClassifier> shrinking = new ValueParam<>(this, true, "shrinking");

    /**
     * Whether to train a SVC or SVR model for probability estimates (default false).
     */
    public final ValueParam<Boolean, SvmClassifier> probability = new ValueParam<>(this, false, "probability");

    /**
     * Training levels used for classification. If this is not specified than the training levels are determined
     * automatically from the levels of the target variable, in the same order, excluding the missing level.
     * <p>
     * If one specifies explicitly the training levels here, only those levels will be used for classification.
     */
    public final ListParam<String, SvmClassifier> levels = new ListParam<>(this, List.of(), "levels", (l1, l2) -> l2 != null);

    /**
     * Weighting specific for each training level. The effect will be that the penalty coefficient for each target level
     * will be multiplied with corresponding weighting level. If a target label weighting is specified, but it is missing from
     * {@link #targetLevels} than the weighting will be ignored. If a target label weighting does not exist for a target label
     * specified in {@link #targetLevels}, than a default value of {@code 1} is considered. The default value for this
     * parameter is an empty map, which is equivalent with all weighting equal {@code 1}.
     */
    public final MultiParam<String, Double, SvmClassifier> wi = new MultiParam<>(this, Map.of(), "wi", Objects::nonNull);

    private SvmModel svm_model;
    private ProblemInfo problemInfo;
    private ModelInfo modelInfo;

    @Override
    public ClassifierModel<SvmClassifier, ClassifierResult, RunInfo<SvmClassifier>> newInstance() {
        return new SvmClassifier().copyParameterValues(this);
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

        NArray<Double> x = df.mapVars(inputNames).narray();
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
        NArray<Double> xs = df.mapVars(inputNames).narray();
        for (int i = 0; i < xs.dim(0); i++) {
            int k = problemInfo.levels().size();

            if (probability.get()) {
                double[] prob = new double[k];
                double score = Svm.svm_predict_probability(svm_model, xs.takesq(0, i), prob);

                result.firstClasses().setLabel(i, problemInfo.levels().get((int) score));
                for (int j = 0; j < k; j++) {
                    result.firstDensity().setDouble(i, j, prob[j]);
                }
            } else {
                double[] values = new double[k * (k - 1) / 2];
                double score = Svm.svm_predict_values(svm_model, xs.takesq(0, i), values);

                result.firstClasses().setLabel(i, problemInfo.levels().get((int) score));
                if(withDistributions) {
                    for (int j = 0; j < k; j++) {
                        result.firstDensity().setDouble(i, j, values[j]);
                    }
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
                if(withDistributions) {
                    for (int j = 0; j < k; j++) {
                        result.firstDensity().setDouble(i, j, dist[j]);
                    }
                }
            }
        }
        return result;
    }
}
