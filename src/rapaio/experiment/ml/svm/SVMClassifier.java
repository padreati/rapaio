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
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import rapaio.core.RandomSource;
import rapaio.core.tools.ChannelMeshGrid;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.mapping.ArrayMapping;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.svm.svm.Solver;
import rapaio.experiment.ml.svm.svm.Solver_NU;
import rapaio.experiment.ml.svm.svm.SvcKernelMatrix;
import rapaio.experiment.ml.svm.svm.Svm;
import rapaio.experiment.ml.svm.svm.svm_model;
import rapaio.experiment.ml.svm.svm.svm_parameter;
import rapaio.experiment.ml.svm.svm.svm_problem;
import rapaio.graphics.plot.Plot;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ListParam;
import rapaio.ml.common.MultiParam;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.common.kernel.WaveletKernel;
import rapaio.ml.eval.metric.Confusion;
import rapaio.ml.supervised.ClassifierHookInfo;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.ClassifierResult;
import rapaio.sys.WS;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;
import rapaio.util.collection.TArrays;

public class SVMClassifier extends ClassifierModel<SVMClassifier, ClassifierResult, ClassifierHookInfo> {

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
    public final ValueParam<Double, SVMClassifier> c = new ValueParam<>(this, 1.0, "cost");

    /**
     * -n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
     */
    public final ValueParam<Double, SVMClassifier> nu = new ValueParam<>(this, 0.5, "nu");

    /**
     * -p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)
     */
    public final ValueParam<Double, SVMClassifier> p = new ValueParam<>(this, 0.1, "p");

    public final ValueParam<Long, SVMClassifier> cacheSize = new ValueParam<>(this, 100L, "cacheSize");

    /**
     * -e epsilon : set tolerance of termination criterion (default 0.001)
     */
    public final ValueParam<Double, SVMClassifier> epsilon = new ValueParam<>(this, 0.001, "epsilon");

    /**
     * -h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)
     */
    public final ValueParam<Boolean, SVMClassifier> shrinking = new ValueParam<>(this, true, "shrinking");

    /**
     * -b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
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
    public ClassifierModel<SVMClassifier, ClassifierResult, ClassifierHookInfo> newInstance() {
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
                    parent.epsilon.get(), parent.c.get(), new HashMap<>(parent.wi.get()),
                    parent.nu.get(), parent.p.get(), parent.shrinking.get(), parent.probability.get());
        }

        /**
         * Selects two labels from the training data set and builds a new problem using only those data sets.
         * The two data sets are already computet and given as parameters in the form of {@code xsi} and {@code xsj}.
         *
         * @param pi  original problem
         * @param xsi
         * @param xsj
         * @param i
         * @param j
         * @return
         */
        public static ProblemInfo binaryProblem(ProblemInfo pi, DVector[] xsi, DVector[] xsj, int i, int j) {
            String li = pi.levels.get(i);
            String lj = pi.levels.get(j);

            List<String> levels = List.of(li, lj);
            Map<String, Integer> index = Map.of(li, 0, lj, 1);
            Map<String, Mapping> map = Map.of(
                    li, Mapping.range(0, xsi.length),
                    lj, Mapping.range(xsi.length, xsi.length + xsj.length)
            );

            DVector[] xs = TArrays.concat(xsi, xsj);
            double[] y = DoubleArrays.newFrom(0, xsi.length + xsj.length, row -> row < xsi.length ? +1 : -1);

            return new ProblemInfo(xs, y, levels, index, map,
                    pi.type, pi.kernel, pi.cacheSize, pi.eps, pi.c, pi.weighting, pi.nu, pi.p, pi.shrinking, pi.probability);
        }

        public static ProblemInfo binarySubsetProblem(ProblemInfo pi, int[] perm, int start, int end, double cp, double cn) {

            DVector[] xs = new DVector[pi.size() - end + start];
            double[] y = new double[pi.size() - end + start];
            Mapping mi = Mapping.empty();
            Mapping mj = Mapping.empty();
            int pos = 0;
            for (int i = 0; i < pi.size(); i++) {
                if (i >= start && i < end) {
                    continue;
                }
                xs[pos] = pi.xs[perm[i]];
                y[pos] = pi.y[perm[i]];
                if (y[pos] > 0) {
                    mi.add(pos);
                } else {
                    mj.add(pos);
                }
                pos++;
            }

            String li = pi.levels.get(0);
            String lj = pi.levels.get(1);

            return new ProblemInfo(xs, y, pi.levels, pi.index, Map.of(li, mi, lj, mj), pi.type, pi.kernel, pi.cacheSize, pi.eps,
                    1.0, Map.of(li, cp, lj, cn), pi.nu, pi.p, pi.shrinking, false);
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
            prob.l = xs.length;
            prob.x = xs;
            prob.y = y;
            return prob;
        }

        public int size() {
            return xs.length;
        }

        public double[] computeWeightedC() {
            double[] weightedC = DoubleArrays.newFill(levels.size(), c);
            for (var levelWeight : weighting.entrySet()) {
                if (index.containsKey(levelWeight.getKey())) {
                    weightedC[index.get(levelWeight.getKey())] *= levelWeight.getValue();
                }
            }
            return weightedC;
        }

        public DVector[][] splitDataByMappings() {
            DVector[][] xss = new DVector[levels.size()][];
            for (int i = 0; i < levels.size(); i++) {
                Mapping mapping = map.get(levels.get(i));
                xss[i] = new DVector[mapping.size()];
                for (int j = 0; j < mapping.size(); j++) {
                    xss[i][j] = xs[mapping.get(j)];
                }
            }
            return xss;
        }
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        DMatrix x = DMatrix.copy(df.mapVars(inputNames));
        Var target = df.rvar(firstTargetName());

        ProblemInfo pi = ProblemInfo.from(x, target, this);

        model = svm_train(pi);
        problemInfo = pi;
        return true;
    }

    private svm_model svm_train(ProblemInfo pi) {

        model = new svm_model();
        model.param = pi.computeParameters();

        int l = pi.size();

        int levelCount = pi.levels.size();

        // define training sets for each label
        DVector[][] xs = pi.splitDataByMappings();

        // calculate weighted c
        double[] weightedC = pi.computeWeightedC();

        // train k*(k-1)/2 models

        boolean[] svFlag = new boolean[l];
        Decision[] f = new Decision[levelCount * (levelCount - 1) / 2];

        double[] probA = null;
        double[] probB = null;
        if (pi.probability) {
            probA = new double[levelCount * (levelCount - 1) / 2];
            probB = new double[levelCount * (levelCount - 1) / 2];
        }

        int pos = 0;
        for (int i = 0; i < levelCount; i++) {
            for (int j = i + 1; j < levelCount; j++) {

                ProblemInfo spi = ProblemInfo.binaryProblem(pi, xs[i], xs[j], i, j);
                if (pi.probability) {
                    double[] probAB = svm_binary_svc_probability(spi, weightedC[i], weightedC[j]);
                    probA[pos] = probAB[0];
                    probB[pos] = probAB[1];
                }

                f[pos] = switch (spi.type) {
                    case C_SVC -> solve_c_svc(spi, weightedC[i], weightedC[j]);
                    case NU_SVC -> solve_nu_svc(spi);
                    default -> null;
                };

                String li = pi.levels.get(i);
                for (int k = 0; k < pi.map.get(li).size(); k++) {
                    if (Math.abs(f[pos].alpha[k]) > 0) {
                        svFlag[pi.map.get(li).get(k)] = true;
                    }
                }
                String lj = pi.levels.get(j);
                for (int k = 0; k < pi.map.get(lj).size(); k++) {
                    if (Math.abs(f[pos].alpha[xs[i].length + k]) > 0) {
                        svFlag[pi.map.get(lj).get(k)] = true;
                    }
                }
                pos++;
            }
        }

        // build output

        model.nr_class = levelCount;

        model.label = levelCount == 2 ? new int[] {+1, -1} : IntArrays.newSeq(0, pi.levels.size());

        model.rho = new double[levelCount * (levelCount - 1) / 2];
        for (int i = 0; i < levelCount * (levelCount - 1) / 2; i++) {
            model.rho[i] = f[i].rho;
        }

        if (pi.probability) {
            model.probA = new double[levelCount * (levelCount - 1) / 2];
            model.probB = new double[levelCount * (levelCount - 1) / 2];
            for (int i = 0; i < levelCount * (levelCount - 1) / 2; i++) {
                model.probA[i] = probA[i];
                model.probB[i] = probB[i];
            }
        } else {
            model.probA = null;
            model.probB = null;
        }

        int totalSV = 0;
        int[] nzCount = new int[levelCount];
        model.nSV = new int[levelCount];
        for (int i = 0; i < levelCount; i++) {
            String level = pi.levels.get(i);
            int nSV = 0;
            for (int j : pi.map.get(level)) {
                if (svFlag[j]) {
                    ++nSV;
                    ++totalSV;
                }
            }
            model.nSV[i] = nSV;
            nzCount[i] = nSV;
        }

        LOGGER.info("Total nSV = " + totalSV + "\n");

        model.l = totalSV;
        model.SV = new DVector[totalSV];
        model.sv_indices = new int[totalSV];
        pos = 0;
        for (int i = 0; i < l; i++) {
            if (svFlag[i]) {
                model.SV[pos] = pi.xs[i];
                model.sv_indices[pos] = i + 1;
                pos++;
            }
        }

        int[] nz_start = new int[levelCount];
        nz_start[0] = 0;
        for (int i = 1; i < levelCount; i++) {
            nz_start[i] = nz_start[i - 1] + nzCount[i - 1];
        }

        model.sv_coef = new double[levelCount - 1][];
        for (int i = 0; i < levelCount - 1; i++) {
            model.sv_coef[i] = new double[totalSV];
        }

        pos = 0;
        for (int i = 0; i < levelCount; i++) {
            for (int j = i + 1; j < levelCount; j++) {
                // classifier (i,j): coefficients with
                // i are in sv_coef[j-1][nz_start[i]...],
                // j are in sv_coef[i][nz_start[j]...]

                int q = nz_start[i];
                int k;
                String li = pi.levels.get(i);
                for (k = 0; k < pi.map.get(li).size(); k++) {
                    if (svFlag[pi.map.get(li).get(k)]) {
                        model.sv_coef[j - 1][q++] = f[pos].alpha[k];
                    }
                }
                q = nz_start[j];
                String lj = pi.levels.get(j);
                for (k = 0; k < pi.map.get(lj).size(); k++) {
                    if (svFlag[pi.map.get(lj).get(k)]) {
                        model.sv_coef[i][q++] = f[pos].alpha[pi.map.get(li).size() + k];
                    }
                }
                ++pos;
            }
        }
        return model;
    }

    private record Decision(double[] alpha, double obj, double rho, int nSV, int nBSV, double upperBoundP, double upperBoundN, double r) {
    }

    private static Decision solve_c_svc(ProblemInfo pi, double cp, double cn) {
        int l = pi.size();
        double[] minusOnes = new double[l];
        byte[] y = new byte[l];
        double[] alpha = new double[l];

        for (int i = 0; i < l; i++) {
            alpha[i] = 0;
            minusOnes[i] = -1;
            y[i] = (pi.y[i] > 0) ? (byte) +1 : -1;
        }

        Solver s = new Solver();
        Solver.SolutionInfo si = new Solver.SolutionInfo();
        s.Solve(l, new SvcKernelMatrix(pi.computeProblem(), pi.computeParameters(), y), minusOnes, y,
                alpha, cp, cn, pi.eps, si, pi.shrinking ? 1 : 0);

        double sumAlpha = DVector.wrap(alpha).sum();

        if (cp == cn) {
            LOGGER.info("nu = " + sumAlpha / (cp * pi.size()) + "\n");
        }

        for (int i = 0; i < l; i++) {
            alpha[i] *= y[i];
        }

        int nSV = 0;
        int nBSV = 0;
        for (int i = 0; i < pi.size(); i++) {
            if (Math.abs(alpha[i]) > 0) {
                ++nSV;
                if (pi.y[i] > 0) {
                    if (Math.abs(alpha[i]) >= si.upper_bound_p) {
                        ++nBSV;
                    }
                } else {
                    if (Math.abs(alpha[i]) >= si.upper_bound_n) {
                        ++nBSV;
                    }
                }
            }
        }
        // output SVs
        LOGGER.info("obj = " + si.obj + ", rho = " + si.rho + ", nSV = " + nSV + ", nBSV = " + nBSV);

        return new Decision(alpha, si.obj, si.rho, nSV, nBSV, si.upper_bound_p, si.upper_bound_n, si.r);
    }

    private static Decision solve_nu_svc(ProblemInfo pi) {
        int i;
        int l = pi.size();
        double nu = pi.nu;
        double[] alpha = new double[l];

        byte[] y = new byte[l];

        for (i = 0; i < l; i++) {
            if (pi.y[i] > 0) {
                y[i] = +1;
            } else {
                y[i] = -1;
            }
        }

        double sumPos = nu * l / 2;
        double sumNeg = nu * l / 2;

        for (i = 0; i < l; i++) {
            if (y[i] == +1) {
                alpha[i] = Math.min(1.0, sumPos);
                sumPos -= alpha[i];
            } else {
                alpha[i] = Math.min(1.0, sumNeg);
                sumNeg -= alpha[i];
            }
        }

        double[] zeros = new double[l];

        for (i = 0; i < l; i++) {
            zeros[i] = 0;
        }

        Solver_NU s = new Solver_NU();
        Solver.SolutionInfo si = new Solver.SolutionInfo();
        s.Solve(l, new SvcKernelMatrix(pi.computeProblem(), pi.computeParameters(), y), zeros, y, alpha,
                1.0, 1.0, pi.eps, si, pi.shrinking ? 1 : 0);
        double r = si.r;

        LOGGER.info("c = " + 1 / r + "\n");

        for (i = 0; i < l; i++) {
            alpha[i] *= y[i] / r;
        }

        si.rho /= r;
        si.obj /= (r * r);
        si.upper_bound_p = 1 / r;
        si.upper_bound_n = 1 / r;

        int nSV = 0;
        int nBSV = 0;
        for (i = 0; i < pi.size(); i++) {
            if (Math.abs(alpha[i]) > 0) {
                ++nSV;
                if (pi.y[i] > 0) {
                    if (Math.abs(alpha[i]) >= si.upper_bound_p) {
                        ++nBSV;
                    }
                } else {
                    if (Math.abs(alpha[i]) >= si.upper_bound_n) {
                        ++nBSV;
                    }
                }
            }
        }
        // output SVs
        LOGGER.info("obj = " + si.obj + ", rho = " + si.rho + ", nSV = " + nSV + ", nBSV = " + nBSV);

        return new Decision(alpha, si.obj, si.rho, nSV, nBSV, si.upper_bound_p, si.upper_bound_n, si.rho);
    }

    // Cross-validation decision values for probability estimates
    private double[] svm_binary_svc_probability(ProblemInfo pi, double Cp, double Cn) {
        int i;
        int nr_fold = 5;
        double[] dec_values = new double[pi.size()];

        int[] perm = IntArrays.newSeq(0, pi.size());
//        IntArrays.shuffle(perm, new Random(42));

        Random rand = new Random(42);
        for (i = 0; i < pi.size(); i++) {
            int j = i + rand.nextInt(pi.size() - i);
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }

        LOGGER.info("binary cv train levels: %s, %s".formatted(pi.levels.get(0), pi.levels.get(1)));
        for (i = 0; i < nr_fold; i++) {
            LOGGER.info("fold: " + i);
            int begin = i * pi.size() / nr_fold;
            int end = (i + 1) * pi.size() / nr_fold;
            int j;

            ProblemInfo spi = ProblemInfo.binarySubsetProblem(pi, perm, begin, end, Cp, Cn);

            int p_count = 0, n_count = 0;
            for (j = 0; j < spi.size(); j++) {
                if (spi.y[j] > 0) {
                    p_count++;
                } else {
                    n_count++;
                }
            }
            LOGGER.info("pcount: %d, ncount: %d".formatted(p_count, n_count));

            if (p_count == 0 && n_count == 0) {
                for (j = begin; j < end; j++) {
                    dec_values[perm[j]] = 0;
                }
            } else if (p_count > 0 && n_count == 0) {
                for (j = begin; j < end; j++) {
                    dec_values[perm[j]] = 1;
                }
            } else if (p_count == 0 && n_count > 0) {
                for (j = begin; j < end; j++) {
                    dec_values[perm[j]] = -1;
                }
            } else {
                svm_model submodel = Svm.svm_train(spi.computeProblem(), spi.computeParameters());
                for (j = begin; j < end; j++) {
                    double[] dec_value = new double[1];
                    Svm.svm_predict_values(submodel, pi.xs[perm[j]], dec_value);
                    dec_values[perm[j]] = dec_value[0];
                    // ensure +1 -1 order; reason not using CV subroutine
                    dec_values[perm[j]] *= submodel.label[0];
                }
            }
        }
        double[] ab = sigmoid_train(pi.size(), dec_values, pi.y);
        LOGGER.info("binary cv train levels: %s, %s -> %f, %f".formatted(pi.levels.get(0), pi.levels.get(1), ab[0], ab[1]));
        return ab;
    }

    // Platt's binary SVM Probablistic Output: an improvement from Lin et al.
    private static double[] sigmoid_train(int l, double[] dec_values, double[] labels) {
        double A, B;
        double prior1 = 0, prior0 = 0;
        int i;

        for (i = 0; i < l; i++) {
            if (labels[i] > 0) {
                prior1 += 1;
            } else {
                prior0 += 1;
            }
        }

        int max_iter = 100;    // Maximal number of iterations
        double min_step = 1e-10;    // Minimal step taken in line search
        double sigma = 1e-12;    // For numerically strict PD of Hessian
        double eps = 1e-5;
        double hiTarget = (prior1 + 1.0) / (prior1 + 2.0);
        double loTarget = 1 / (prior0 + 2.0);
        double[] t = new double[l];
        double fApB, p, q, h11, h22, h21, g1, g2, det, dA, dB, gd, stepsize;
        double newA, newB, newf, d1, d2;
        int iter;

        // Initial Point and Initial Fun Value
        A = 0.0;
        B = Math.log((prior0 + 1.0) / (prior1 + 1.0));
        double fval = 0.0;

        for (i = 0; i < l; i++) {
            if (labels[i] > 0) {
                t[i] = hiTarget;
            } else {
                t[i] = loTarget;
            }
            fApB = dec_values[i] * A + B;
            if (fApB >= 0) {
                fval += t[i] * fApB + Math.log(1 + Math.exp(-fApB));
            } else {
                fval += (t[i] - 1) * fApB + Math.log(1 + Math.exp(fApB));
            }
        }
        for (iter = 0; iter < max_iter; iter++) {
            // Update Gradient and Hessian (use H' = H + sigma I)
            h11 = sigma; // numerically ensures strict PD
            h22 = sigma;
            h21 = 0.0;
            g1 = 0.0;
            g2 = 0.0;
            for (i = 0; i < l; i++) {
                fApB = dec_values[i] * A + B;
                if (fApB >= 0) {
                    p = Math.exp(-fApB) / (1.0 + Math.exp(-fApB));
                    q = 1.0 / (1.0 + Math.exp(-fApB));
                } else {
                    p = 1.0 / (1.0 + Math.exp(fApB));
                    q = Math.exp(fApB) / (1.0 + Math.exp(fApB));
                }
                d2 = p * q;
                h11 += dec_values[i] * dec_values[i] * d2;
                h22 += d2;
                h21 += dec_values[i] * d2;
                d1 = t[i] - p;
                g1 += dec_values[i] * d1;
                g2 += d1;
            }

            // Stopping Criteria
            if (Math.abs(g1) < eps && Math.abs(g2) < eps) {
                break;
            }

            // Finding Newton direction: -inv(H') * g
            det = h11 * h22 - h21 * h21;
            dA = -(h22 * g1 - h21 * g2) / det;
            dB = -(-h21 * g1 + h11 * g2) / det;
            gd = g1 * dA + g2 * dB;


            stepsize = 1;        // Line Search
            while (stepsize >= min_step) {
                newA = A + stepsize * dA;
                newB = B + stepsize * dB;

                // New function value
                newf = 0.0;
                for (i = 0; i < l; i++) {
                    fApB = dec_values[i] * newA + newB;
                    if (fApB >= 0) {
                        newf += t[i] * fApB + Math.log(1 + Math.exp(-fApB));
                    } else {
                        newf += (t[i] - 1) * fApB + Math.log(1 + Math.exp(fApB));
                    }
                }
                // Check sufficient decrease
                if (newf < fval + 0.0001 * stepsize * gd) {
                    A = newA;
                    B = newB;
                    fval = newf;
                    break;
                } else {
                    stepsize = stepsize / 2.0;
                }
            }

            if (stepsize < min_step) {
                Svm.info("Line search fails in two-class probability estimates\n");
                break;
            }
        }

        if (iter >= max_iter) {
            Svm.info("Reaching maximal iterations in two-class probability estimates\n");
        }
        return new double[] {A, B};
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

    public static void main(String[] args) {
        RandomSource.setSeed(42);
//        run(true);
        run(false);
    }

    static void run(boolean prob) {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.of(0, 1, 4)).copy();

        SVMClassifier model = new SVMClassifier();
        model.c.set(10.0);
        model.kernel.set(new WaveletKernel(true, 0.2, 3));
        model.kernel.set(new RBFKernel(5.0));
        model.probability.set(prob);
        model.fit(iris, "class");

        Confusion confusion = Confusion.from(iris.rvar("class"), model.predict(iris).firstClasses());
        confusion.printSummary();

        Var x1 = iris.rvar(0);
        Var x2 = iris.rvar(1);

        ChannelMeshGrid mg = new ChannelMeshGrid(3, x1.dv().min(), x1.dv().max(), x2.dv().min(), x2.dv().max(), 0.005);
        VarDouble x = mg.getXRange();
        VarDouble y = mg.getYRange();

        Frame sample = SolidFrame.emptyFrom(iris, x.size() * y.size());

        int pos = 0;
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                sample.setDouble(pos, 0, x.getDouble(i));
                sample.setDouble(pos, 1, y.getDouble(j));
                pos++;
            }
        }
        ClassifierResult spred = model.predict(sample);
        Frame density = spred.firstDensity();

        pos = 0;
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                DVector value = DVector.wrap(density.getDouble(pos, 1), density.getDouble(pos, 2), density.getDouble(pos, 3));
                mg.set(i, j, value);
                pos++;
            }
        }

        // and finally plot the results

        double maxValue = Double.NEGATIVE_INFINITY;
        double minValue = Double.POSITIVE_INFINITY;
        for (Var v : density.varList()) {
            maxValue = Math.max(maxValue, v.dv().max());
            minValue = Math.min(minValue, v.dv().min());
        }

        Plot p = new Plot();
        mg.plot(p, Double.NaN, Double.NaN, 20);

        WS.draw(p.points(iris.rvar(0), iris.rvar(1), fill(iris.rvar(2)), pch(2)), 800, 600);
    }
}
