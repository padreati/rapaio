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
import static rapaio.sys.With.lwd;
import static rapaio.sys.With.pch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import rapaio.core.tools.GridData;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.mapping.ArrayMapping;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.svm.libsvm.Solver;
import rapaio.experiment.ml.svm.libsvm.Solver_NU;
import rapaio.experiment.ml.svm.libsvm.SvcKernelMatrix;
import rapaio.experiment.ml.svm.libsvm.Svm;
import rapaio.experiment.ml.svm.libsvm.svm_model;
import rapaio.experiment.ml.svm.libsvm.svm_parameter;
import rapaio.experiment.ml.svm.libsvm.svm_problem;
import rapaio.graphics.opt.ColorGradient;
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
import rapaio.sys.With;
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
     * -c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1).
     */
    public final ValueParam<Double, SVMClassifier> c = new ValueParam<>(this, 1.0, "cost");

    /**
     * -n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
     */
    public final ValueParam<Double, SVMClassifier> nu = new ValueParam<>(this, 0.5, "nu");

    /**
     * -p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)
     */
    public final ValueParam<Double, SVMClassifier> epsilon = new ValueParam<>(this, 0.1, "epsilon");

    public final ValueParam<Long, SVMClassifier> cacheSize = new ValueParam<>(this, 100L, "cacheSize");

    /**
     * -e epsilon : set tolerance of termination criterion (default 0.001)
     */
    public final ValueParam<Double, SVMClassifier> tolerance = new ValueParam<>(this, 0.001, "tolerance");

    /**
     * -h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)
     */
    public final ValueParam<Boolean, SVMClassifier> shrinking = new ValueParam<>(this, true, "shrinking");

    /**
     * -b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
     */
    public final ValueParam<Boolean, SVMClassifier> probability = new ValueParam<>(this, false, "p");

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
    private TargetInfo ti;

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

    private class TargetInfo {

        public final List<String> labels;
        public final HashMap<String, Integer> index;
        public final HashMap<String, Mapping> map;

        public TargetInfo(Var target) {
            labels = new ArrayList<>();
            index = new HashMap<>();
            map = new HashMap<>();

            if (levels.get().isEmpty()) {
                // specified by the dictionary from target variable
                target.levels().stream().skip(1).forEach(label -> {
                    index.put(label, labels.size());
                    labels.add(label);
                    map.put(label, new ArrayMapping());
                });
            } else {
                List<String> levels = SVMClassifier.this.levels.get();
                for (String level : levels) {
                    index.put(level, labels.size());
                    labels.add(level);
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
        }
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        DMatrix x = DMatrix.copy(df.mapVars(inputNames));
        Var target = df.rvar(firstTargetName());

        this.ti = new TargetInfo(target);

        svm_problem prob = computeProblem(x, target);
        svm_parameter param = computeParameters();

        model = new svm_model();
        model.param = param;

        int l = prob.l;

        int levelCount = ti.labels.size();

        // define training sets for each label
        DVector[][] xs = new DVector[ti.labels.size()][];
        for (int i = 0; i < ti.labels.size(); i++) {
            Mapping mapping = ti.map.get(ti.labels.get(i));
            xs[i] = new DVector[mapping.size()];
            for (int j = 0; j < mapping.size(); j++) {
                xs[i][j] = prob.x[mapping.get(j)];
            }
        }

        // calculate weighted C

        double[] weightedC = DoubleArrays.newFill(levelCount, param.C);
        for (var levelWeight : wi.get().entrySet()) {
            int index = ti.index.get(levelWeight.getKey());
            weightedC[index] *= levelWeight.getValue();
        }

        // train k*(k-1)/2 models

        boolean[] nonzero = new boolean[l];
        decision[] f = new decision[levelCount * (levelCount - 1) / 2];

        double[] probA = null;
        double[] probB = null;
        if (param.probability == 1) {
            probA = new double[levelCount * (levelCount - 1) / 2];
            probB = new double[levelCount * (levelCount - 1) / 2];
        }

        int p = 0;
        for (int i = 0; i < levelCount; i++) {
            for (int j = i + 1; j < levelCount; j++) {
                int ii = i;
                svm_problem sub_prob = new svm_problem();
                sub_prob.l = xs[i].length + xs[j].length;
                sub_prob.x = TArrays.concat(xs[i], xs[j]);
                sub_prob.y = DoubleArrays.newFrom(0, sub_prob.l, pos -> pos < xs[ii].length ? +1 : -1);

                if (param.probability == 1) {
                    double[] probAB = new double[2];
                    Svm.svm_binary_svc_probability(sub_prob, param, weightedC[i], weightedC[j], probAB);
                    probA[p] = probAB[0];
                    probB[p] = probAB[1];
                }

                f[p] = svm_train_one(sub_prob, param, weightedC[i], weightedC[j]);
                String li = ti.labels.get(i);
                for (int k = 0; k < xs[i].length; k++) {
                    if (Math.abs(f[p].alpha[k]) > 0) {
                        nonzero[ti.map.get(li).get(k)] = true;
                    }
                }
                String lj = ti.labels.get(j);
                for (int k = 0; k < xs[j].length; k++) {
                    if (Math.abs(f[p].alpha[xs[i].length + k]) > 0) {
                        nonzero[ti.map.get(lj).get(k)] = true;
                    }
                }
                ++p;
            }
        }

        // build output

        model.nr_class = levelCount;

        model.label = IntArrays.newSeq(0, ti.labels.size());

        model.rho = new double[levelCount * (levelCount - 1) / 2];
        for (int i = 0; i < levelCount * (levelCount - 1) / 2; i++) {
            model.rho[i] = f[i].rho;
        }

        if (param.probability == 1) {
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
            String level = ti.labels.get(i);
            int nSV = 0;
            for (int j : ti.map.get(level)) {
                if (nonzero[j]) {
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
        p = 0;
        for (int i = 0; i < l; i++) {
            if (nonzero[i]) {
                model.SV[p] = prob.x[i];
                model.sv_indices[p] = i;
                p++;
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

        p = 0;
        for (int i = 0; i < levelCount; i++) {
            for (int j = i + 1; j < levelCount; j++) {
                // classifier (i,j): coefficients with
                // i are in sv_coef[j-1][nz_start[i]...],
                // j are in sv_coef[i][nz_start[j]...]

                int q = nz_start[i];
                int k;
                String li = ti.labels.get(i);
                for (k = 0; k < ti.map.get(li).size(); k++) {
                    if (nonzero[ti.map.get(li).get(k)]) {
                        model.sv_coef[j - 1][q++] = f[p].alpha[k];
                    }
                }
                q = nz_start[j];
                String lj = ti.labels.get(j);
                for (k = 0; k < ti.map.get(lj).size(); k++) {
                    if (nonzero[ti.map.get(lj).get(k)]) {
                        model.sv_coef[i][q++] = f[p].alpha[ti.map.get(li).size() + k];
                    }
                }
                ++p;
            }
        }
        return true;
    }

    private record decision(double[] alpha, double obj, double rho, int nSV, int nBSV, double upperBoundP, double upperBoundN) {
    }

    public static decision svm_train_one(svm_problem prob, svm_parameter param, double Cp, double Cn) {
        double[] alpha = new double[prob.l];
        Solver.SolutionInfo si = new Solver.SolutionInfo();
        switch (param.svm_type) {
            case svm_parameter.C_SVC:
                solve_c_svc(prob, param, alpha, si, Cp, Cn);
                break;
            case svm_parameter.NU_SVC:
                solve_nu_svc(prob, param, alpha, si);
                break;
        }

        LOGGER.info("obj = " + si.obj + ", rho = " + si.rho + "\n");

        // output SVs

        int nSV = 0;
        int nBSV = 0;
        for (int i = 0; i < prob.l; i++) {
            if (Math.abs(alpha[i]) > 0) {
                ++nSV;
                if (prob.y[i] > 0) {
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

        LOGGER.info("nSV = " + nSV + ", nBSV = " + nBSV + "\n");

        return new decision(alpha, si.obj, si.rho, nSV, nBSV, si.upper_bound_p, si.upper_bound_n);
    }

    private static void solve_c_svc(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si, double Cp, double Cn) {
        int l = prob.l;
        double[] minus_ones = new double[l];
        byte[] y = new byte[l];

        for (int i = 0; i < l; i++) {
            alpha[i] = 0;
            minus_ones[i] = -1;
            y[i] = (prob.y[i] > 0) ? (byte) +1 : -1;
        }

        Solver s = new Solver();
        s.Solve(l, new SvcKernelMatrix(prob, param, y), minus_ones, y,
                alpha, Cp, Cn, param.eps, si, param.shrinking);

        double sumAlpha = DVector.wrap(alpha).sum();

        if (Cp == Cn) {
            Svm.info("nu = " + sumAlpha / (Cp * prob.l) + "\n");
        }

        for (int i = 0; i < l; i++) {
            alpha[i] *= y[i];
        }
    }

    private static void solve_nu_svc(svm_problem prob, svm_parameter param, double[] alpha, Solver.SolutionInfo si) {
        int i;
        int l = prob.l;
        double nu = param.nu;

        byte[] y = new byte[l];

        for (i = 0; i < l; i++) {
            if (prob.y[i] > 0) {
                y[i] = +1;
            } else {
                y[i] = -1;
            }
        }

        double sum_pos = nu * l / 2;
        double sum_neg = nu * l / 2;

        for (i = 0; i < l; i++) {
            if (y[i] == +1) {
                alpha[i] = Math.min(1.0, sum_pos);
                sum_pos -= alpha[i];
            } else {
                alpha[i] = Math.min(1.0, sum_neg);
                sum_neg -= alpha[i];
            }
        }

        double[] zeros = new double[l];

        for (i = 0; i < l; i++) {
            zeros[i] = 0;
        }

        Solver_NU s = new Solver_NU();
        s.Solve(l, new SvcKernelMatrix(prob, param, y), zeros, y,
                alpha, 1.0, 1.0, param.eps, si, param.shrinking);
        double r = si.r;

        Svm.info("C = " + 1 / r + "\n");

        for (i = 0; i < l; i++) {
            alpha[i] *= y[i] / r;
        }

        si.rho /= r;
        si.obj /= (r * r);
        si.upper_bound_p = 1 / r;
        si.upper_bound_n = 1 / r;
    }


    private svm_problem computeProblem(DMatrix x, Var target) {
        svm_problem prob = new svm_problem();
        prob.l = x.rowCount();
        prob.x = new DVector[x.rowCount()];
        for (int i = 0; i < x.rowCount(); i++) {
            prob.x[i] = x.mapRow(i, With.copy());
        }
        prob.y = new double[x.rowCount()];
        for (int i = 0; i < x.rowCount(); i++) {
            switch (target.type()) {
                case BINARY, INT -> prob.y[i] = target.getInt(i);
                case NOMINAL -> prob.y[i] = target.getInt(i) - 1;
                default -> throw new IllegalArgumentException("Not implemented");
            }
        }
        return prob;
    }

    private svm_parameter computeParameters() {
        svm_parameter param = new svm_parameter();
        param.svm_type = type.get() == SvmType.C_SVC ? 0 : 1;
        param.kernel = kernel.get();

        param.cache_size = cacheSize.get();
        param.eps = epsilon.get();
        param.C = c.get();    // for C_SVC, EPSILON_SVR and NU_SVR

        param.nr_weight = wi.get().size();        // for C_SVC
        param.weight_label = new int[param.nr_weight];    // for C_SVC
        param.weight = new double[param.nr_weight];        // for C_SVC
        int pos = 0;
        for (var w : wi.get().entrySet()) {
            param.weight_label[pos] = ti.index.get(w.getKey());
            param.weight[pos] = w.getValue();
            pos++;
        }

        param.nu = nu.get();    // for NU_SVC, ONE_CLASS, and NU_SVR
        param.p = 0;    // for EPSILON_SVR
        param.shrinking = shrinking.get() ? 1 : 0;    // use the shrinking heuristics

        // TODO fix that?
        param.probability = probability.get() ? 1 : 0; // do probability estimates

        return param;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {

        ClassifierResult result = ClassifierResult.build(this, df, withClasses, withDistributions);
        DMatrix xs = DMatrix.copy(df.mapVars(inputNames));
        for (int i = 0; i < xs.rowCount(); i++) {
            int k = ti.labels.size();
            double[] values = new double[k * (k - 1) / 2];

            double score = (probability.get()) ? Svm.svm_predict_probability(model, xs.mapRow(i), values)
                    : Svm.svm_predict_values(model, xs.mapRow(i), values);

            LOGGER.finest("i:%d, score:%f, values:%s".formatted(
                    i, score, Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","))));

            result.firstClasses().setLabel(i, ti.labels.get((int) score));
            double last = 1 - DoubleArrays.sum(values, 0, values.length);
            for (int j = 0; j < k - 1; j++) {
                result.firstDensity().setDouble(i, j + 1, values[j]);
            }
            result.firstDensity().setDouble(i, k, last);
        }
        return result;
    }

    public static void main(String[] args) {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.of(0, 1, 4)).copy();

        SVMClassifier model = new SVMClassifier();
        model.c.set(10.0);
        model.kernel.set(new WaveletKernel(true, 0.2, 3));
        model.probability.set(true);

        model.fit(iris, "class");

        ClassifierResult pred = model.predict(iris);

        Confusion confusion = Confusion.from(iris.rvar("class"), pred.firstClasses());
        confusion.printSummary();

        Var x1 = iris.rvar(0);
        Var x2 = iris.rvar(1);

        DVector x1v = x1.dVec();
        DVector x2v = x2.dVec();

        VarDouble x = VarDouble.seq(x1v.min(), x1v.max(), (x1v.max() - x1v.min()) / 256);
        VarDouble y = VarDouble.seq(x2v.min(), x2v.max(), (x2v.max() - x2v.min()) / 256);

        GridData mg = new GridData(x, y);
        Frame sample = SolidFrame.emptyFrom(iris, x.size() * y.size());

        int pos = 0;
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                sample.setDouble(pos, 0, x.getDouble(i));
                sample.setDouble(pos, 1, y.getDouble(j));
                pos++;
            }
        }
        Frame density = model.predict(sample).firstDensity();
        Var sp = model.predict(sample).firstClasses();


        pos = 0;
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                double pp = 256 * (sp.getInt(pos) - 1 + density.getDouble(pos, sp.getInt(pos)) / 3.0);
                mg.setValue(i, j, pp);
                pos++;
            }
        }

        // and finally plot the results

        Plot p = new Plot();
        double[] pp = VarDouble.seq(0, 1, 0.01).elements();
        double[] qq = mg.quantiles(pp);

        p.isoBands(mg, ColorGradient.newHueGradient(DVector.wrap(pp).copy().mul(-1).add(1).dVar().elements()), qq, lwd(2f));

        WS.draw(p.points(iris.rvar(0), iris.rvar(1), fill(iris.rvar(2)), pch(2)), 800, 600);
    }
}
