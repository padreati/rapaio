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

package rapaio.experiment.ml.svm;

import static rapaio.sys.With.fill;
import static rapaio.sys.With.pch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.mapping.ArrayMapping;
import rapaio.experiment.ml.svm.svm.Svm;
import rapaio.experiment.ml.svm.svm.svm_model;
import rapaio.experiment.ml.svm.svm.svm_parameter;
import rapaio.experiment.ml.svm.svm.svm_problem;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ListParam;
import rapaio.ml.common.MultiParam;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;

public class SVMClassifier extends ClassifierModel<SVMClassifier, ClassifierResult, RunInfo<SVMClassifier>> {

    private static final Logger LOGGER = Logger.getLogger(SVMClassifier.class.getName());

    public enum SvmType {
        C_SVC,
        NU_SVC
    }

    public final ValueParam<SvmType, SVMClassifier> type = new ValueParam<>(this, SvmType.C_SVC, "type");

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


    private svm_model model;
    private ProblemInfo problemInfo;

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
        return new Capabilities(1, 10_000, List.of(VarType.DOUBLE, VarType.INT, VarType.BINARY), false,
                1, 1, List.of(VarType.NOMINAL, VarType.BINARY, VarType.INT), false);
    }

    private record ProblemInfo(DVector[] xs, double[] y,
                               List<String> levels, Map<String, Integer> index, Map<String, Mapping> map,
                               SvmType type, Kernel kernel, long cacheSize, double eps,
                               double c, Map<String, Double> weighting, double nu, double p, boolean shrinking, boolean probability) {

        public static ProblemInfo from(DMatrix x, Var target, SVMClassifier parent) {
            DVector[] xs = new DVector[x.rowCount()];
            for (int i = 0; i < xs.length; i++) {
                xs[i] = x.mapRow(i);
            }
            double[] y = new double[x.rowCount()];
            for (int i = 0; i < x.rowCount(); i++) {
                switch (target.type()) {
                    case BINARY, INT -> y[i] = target.getInt(i);
                    case NOMINAL -> y[i] = target.getInt(i) - 1;
                    default -> throw new IllegalArgumentException("Not implemented");
                }
            }
            List<String> levels = new ArrayList<>();
            Map<String, Integer> index = new HashMap<>();
            Map<String, Mapping> map = new HashMap<>();

            if (parent.levels.get().isEmpty()) {
                // specified by the dictionary from target variable
                target.levels().stream().skip(1).forEach(label -> {
                    index.put(label, levels.size());
                    levels.add(label);
                    map.put(label, new ArrayMapping());
                });
            } else {
                for (String level : parent.levels.get()) {
                    index.put(level, levels.size());
                    levels.add(level);
                    map.put(level, new ArrayMapping());
                }
            }

            for (int i = 0; i < target.size(); i++) {
                String label = target.getLabel(i);
                // count only for specified levels
                if (index.containsKey(label)) {
                    map.get(label).add(i);
                }
            }

            // default parameter values taken from classifier

            return new ProblemInfo(xs, y, levels, index, map,
                    parent.type.get(), parent.kernel.get(), parent.cacheSize.get(),
                    parent.eps.get(), parent.c.get(), new HashMap<>(parent.wi.get()),
                    parent.nu.get(), parent.p.get(), parent.shrinking.get(), parent.probability.get());
        }

        private svm_parameter computeParameters() {
            svm_parameter param = new svm_parameter();
            param.svm_type = type == SvmType.C_SVC ? 0 : 1;
            param.kernel = kernel;

            param.cache_size = cacheSize;
            param.eps = eps;
            param.C = c;    // for C_SVC, EPSILON_SVR and NU_SVR

            param.nr_weight = weighting.size();        // for C_SVC
            param.weight_label = new int[param.nr_weight];    // for C_SVC
            param.weight = new double[param.nr_weight];        // for C_SVC
            int pos = 0;
            for (var w : weighting.entrySet()) {
                if (index.containsKey(w.getKey())) {
                    param.weight_label[pos] = index.get(w.getKey());
                    param.weight[pos] = w.getValue();
                    pos++;
                }
            }

            if (param.nr_weight == 2) {
                param.weight_label[0] = +1;
                param.weight_label[1] = -1;
            }

            param.nu = nu;    // for NU_SVC, ONE_CLASS, and NU_SVR
            param.p = p;    // for EPSILON_SVR
            param.shrinking = shrinking ? 1 : 0;    // use the shrinking heuristics
            param.probability = probability ? 1 : 0; // do probability estimates

            return param;
        }

        private svm_problem computeProblem() {
            svm_problem prob = new svm_problem();
            prob.len = xs.length;
            prob.xs = xs;
            prob.y = y;
            return prob;
        }

        public int size() {
            return xs.length;
        }

        public Map<String, Mapping> getMappings() {
            return map;
        }
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        DMatrix x = DMatrix.copy(df.mapVars(inputNames));
        Var target = df.rvar(firstTargetName());

        ProblemInfo pi = ProblemInfo.from(x, target, this);

        model = Svm.svm_train(pi.computeProblem(), pi.computeParameters());
        problemInfo = pi;
        return true;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {

        ClassifierResult result = ClassifierResult.build(this, df, withClasses, withDistributions);
        DMatrix xs = DMatrix.copy(df.mapVars(inputNames));
        for (int i = 0; i < xs.rowCount(); i++) {
            int k = problemInfo.levels.size();
            double[] values = new double[k];

            double score = (probability.get()) ? Svm.svm_predict_probability(model, xs.mapRow(i), values)
                    : Svm.svm_predict_values(model, xs.mapRow(i), values);

            LOGGER.finest("i:%d, score:%f, values:%s".formatted(
                    i, score, Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","))));

            result.firstClasses().setLabel(i, problemInfo.levels.get((int) score));
            for (int j = 0; j < k; j++) {
                result.firstDensity().setDouble(i, j + 1, values[j]);
            }
        }
        return result;
    }
}
