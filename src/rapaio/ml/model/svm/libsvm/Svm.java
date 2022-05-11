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

package rapaio.ml.model.svm.libsvm;

import java.util.Random;
import java.util.logging.Logger;

import rapaio.core.RandomSource;
import rapaio.experiment.ml.svm.libsvm.svm_parameter;
import rapaio.math.linear.DVector;
import rapaio.util.collection.IntArrays;
import rapaio.util.collection.TArrays;

public class Svm {

    private static final Logger LOGGER = Logger.getLogger(Svm.class.getName());

    public static final Random rand = RandomSource.getRandom();

    private static SolutionInfo solve_c_svc(SvmProblem prob, SvmParameter param, double[] alpha, double cp, double cn) {
        int l = prob.len;
        double[] minus_ones = new double[l];
        byte[] y = new byte[l];

        for (int i = 0; i < l; i++) {
            alpha[i] = 0;
            minus_ones[i] = -1;
            y[i] = (prob.y[i] > 0) ? (byte) +1 : -1;
        }

        SolverC s = new SolverC();
        SolutionInfo si = new SolutionInfo();
        s.solve(l, new SvcKernelMatrix(prob.len, prob.xs, param.kernel, param.cacheSize, y), minus_ones, y,
                alpha, cp, cn, param.eps, si, param.shrinking);

        double sumAlpha = DVector.wrap(alpha).sum();

        if (cp == cn) {
            LOGGER.info("nu = " + sumAlpha / (cp * prob.len) + "\n");
        }

        for (int i = 0; i < l; i++) {
            alpha[i] *= y[i];
        }
        return si;
    }

    private static SolutionInfo solve_nu_svc(SvmProblem prob, SvmParameter param, double[] alpha) {
        int i;
        int l = prob.len;
        double nu = param.nu;

        byte[] y = new byte[l];

        for (i = 0; i < l; i++) {
            if (prob.y[i] > 0) {
                y[i] = 1;
            } else {
                y[i] = -1;
            }
        }

        double sumPos = nu * l / 2;
        double sumNeg = nu * l / 2;

        for (i = 0; i < l; i++) {
            if (y[i] == 1) {
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

        SolverNU s = new SolverNU();
        SolutionInfo si = new SolutionInfo();
        s.solve(l, new SvcKernelMatrix(prob.len, prob.xs, param.kernel, param.cacheSize, y), zeros, y,
                alpha, 1.0, 1.0, param.eps, si, param.shrinking);
        double r = si.r;

        LOGGER.info("c = " + 1 / r + "\n");

        for (i = 0; i < l; i++) {
            alpha[i] *= y[i] / r;
        }

        si.rho /= r;
        si.obj /= (r * r);
        si.pUpperBound = 1 / r;
        si.nUpperBound = 1 / r;

        return si;
    }

    private static SolutionInfo solve_one_class(SvmProblem prob, SvmParameter param, double[] alpha) {
        int l = prob.len;
        double[] zeros = new double[l];
        byte[] ones = new byte[l];
        int i;

        int n = (int) (param.nu * prob.len);    // # of alpha's at upper bound

        for (i = 0; i < n; i++) {
            alpha[i] = 1;
        }
        if (n < prob.len) {
            alpha[n] = param.nu * prob.len - n;
        }
        for (i = n + 1; i < l; i++) {
            alpha[i] = 0;
        }

        for (i = 0; i < l; i++) {
            zeros[i] = 0;
            ones[i] = 1;
        }

        SolverC s = new SolverC();
        SolutionInfo si = new SolutionInfo();
        s.solve(l, new OneClassKernelMatrix(prob, param), zeros, ones,
                alpha, 1.0, 1.0, param.eps, si, param.shrinking);
        return si;
    }

    private static SolutionInfo solve_epsilon_svr(SvmProblem prob, SvmParameter param, double[] alpha) {
        int l = prob.len;
        double[] alpha2 = new double[2 * l];
        double[] linear_term = new double[2 * l];
        byte[] y = new byte[2 * l];
        int i;

        for (i = 0; i < l; i++) {
            alpha2[i] = 0;
            linear_term[i] = param.p - prob.y[i];
            y[i] = 1;

            alpha2[i + l] = 0;
            linear_term[i + l] = param.p + prob.y[i];
            y[i + l] = -1;
        }

        SolverC s = new SolverC();
        SolutionInfo si = new SolutionInfo();
        s.solve(2 * l, new SvrKernelMatrix(prob.len, prob.xs, param.kernel, param.cacheSize), linear_term, y,
                alpha2, param.c, param.c, param.eps, si, param.shrinking);

        double sum_alpha = 0;
        for (i = 0; i < l; i++) {
            alpha[i] = alpha2[i] - alpha2[i + l];
            sum_alpha += Math.abs(alpha[i]);
        }
        LOGGER.info("nu = " + sum_alpha / (param.c * l) + "\n");
        return si;
    }

    private static SolutionInfo solve_nu_svr(SvmProblem prob, SvmParameter param, double[] alpha) {
        int l = prob.len;
        double C = param.c;
        double[] alpha2 = new double[2 * l];
        double[] linear_term = new double[2 * l];
        byte[] y = new byte[2 * l];
        int i;

        double sum = C * param.nu * l / 2;
        for (i = 0; i < l; i++) {
            alpha2[i] = alpha2[i + l] = Math.min(sum, C);
            sum -= alpha2[i];

            linear_term[i] = -prob.y[i];
            y[i] = 1;

            linear_term[i + l] = prob.y[i];
            y[i + l] = -1;
        }

        SolverNU s = new SolverNU();
        SolutionInfo si = new SolutionInfo();
        s.solve(2 * l, new SvrKernelMatrix(prob.len, prob.xs, param.kernel, param.cacheSize), linear_term, y,
                alpha2, C, C, param.eps, si, param.shrinking);

        LOGGER.info("epsilon = " + (-si.r) + "\n");

        for (i = 0; i < l; i++) {
            alpha[i] = alpha2[i] - alpha2[i + l];
        }
        return si;
    }

    static Decision svm_train_one(SvmProblem prob, SvmParameter param, double cp, double cn) {
        double[] alpha = new double[prob.len];
        SolutionInfo si = switch (param.svmType) {
            case SvmParameter.C_SVC -> solve_c_svc(prob, param, alpha, cp, cn);
            case SvmParameter.NU_SVC -> solve_nu_svc(prob, param, alpha);
            case SvmParameter.ONE_CLASS -> solve_one_class(prob, param, alpha);
            case SvmParameter.EPSILON_SVR -> solve_epsilon_svr(prob, param, alpha);
            case SvmParameter.NU_SVR -> solve_nu_svr(prob, param, alpha);
            default -> new SolutionInfo();
        };

        LOGGER.info("obj = " + si.obj + ", rho = " + si.rho + "\n");

        // output SVs

        int nSV = 0;
        int nBSV = 0;
        for (int i = 0; i < prob.len; i++) {
            if (Math.abs(alpha[i]) > 0) {
                ++nSV;
                if (prob.y[i] > 0) {
                    if (Math.abs(alpha[i]) >= si.pUpperBound) {
                        ++nBSV;
                    }
                } else {
                    if (Math.abs(alpha[i]) >= si.nUpperBound) {
                        ++nBSV;
                    }
                }
            }
        }

        LOGGER.info("nSV = " + nSV + ", nBSV = " + nBSV + "\n");

        return new Decision(alpha, si.rho);
    }

    // Platt's binary SVM Probabilistic Output: an improvement from Lin et al.
    public static double[] sigmoid_train(int l, double[] decisionValues, double[] labels) {
        double a;
        double b;
        double prior1 = 0;
        double prior0 = 0;
        int i;

        for (i = 0; i < l; i++) {
            if (labels[i] > 0) {
                prior1 += 1;
            } else {
                prior0 += 1;
            }
        }

        int maxIter = 100;    // Maximal number of iterations
        double minStep = 1e-10;    // Minimal step taken in line search
        double sigma = 1e-12;    // For numerically strict PD of Hessian
        double eps = 1e-5;
        double hiTarget = (prior1 + 1.0) / (prior1 + 2.0);
        double loTarget = 1 / (prior0 + 2.0);
        double[] t = new double[l];
        double fApB, p, q, h11, h22, h21, g1, g2, det, dA, dB, gd, stepsize;
        double newA, newB, newf, d1, d2;
        int iter;

        // Initial Point and Initial Fun Value
        a = 0.0;
        b = Math.log((prior0 + 1.0) / (prior1 + 1.0));
        double fval = 0.0;

        for (i = 0; i < l; i++) {
            if (labels[i] > 0) {
                t[i] = hiTarget;
            } else {
                t[i] = loTarget;
            }
            fApB = decisionValues[i] * a + b;
            if (fApB >= 0) {
                fval += t[i] * fApB + Math.log(1 + Math.exp(-fApB));
            } else {
                fval += (t[i] - 1) * fApB + Math.log(1 + Math.exp(fApB));
            }
        }
        for (iter = 0; iter < maxIter; iter++) {
            // Update Gradient and Hessian (use H' = H + sigma I)
            h11 = sigma; // numerically ensures strict PD
            h22 = sigma;
            h21 = 0.0;
            g1 = 0.0;
            g2 = 0.0;
            for (i = 0; i < l; i++) {
                fApB = decisionValues[i] * a + b;
                if (fApB >= 0) {
                    p = Math.exp(-fApB) / (1.0 + Math.exp(-fApB));
                    q = 1.0 / (1.0 + Math.exp(-fApB));
                } else {
                    p = 1.0 / (1.0 + Math.exp(fApB));
                    q = Math.exp(fApB) / (1.0 + Math.exp(fApB));
                }
                d2 = p * q;
                h11 += decisionValues[i] * decisionValues[i] * d2;
                h22 += d2;
                h21 += decisionValues[i] * d2;
                d1 = t[i] - p;
                g1 += decisionValues[i] * d1;
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
            while (stepsize >= minStep) {
                newA = a + stepsize * dA;
                newB = b + stepsize * dB;

                // New function value
                newf = 0.0;
                for (i = 0; i < l; i++) {
                    fApB = decisionValues[i] * newA + newB;
                    if (fApB >= 0) {
                        newf += t[i] * fApB + Math.log(1 + Math.exp(-fApB));
                    } else {
                        newf += (t[i] - 1) * fApB + Math.log(1 + Math.exp(fApB));
                    }
                }
                // Check sufficient decrease
                if (newf < fval + 0.0001 * stepsize * gd) {
                    a = newA;
                    b = newB;
                    fval = newf;
                    break;
                } else {
                    stepsize = stepsize / 2.0;
                }
            }

            if (stepsize < minStep) {
                LOGGER.info("Line search fails in two-class probability estimates\n");
                break;
            }
        }

        if (iter >= maxIter) {
            LOGGER.info("Reaching maximal iterations in two-class probability estimates\n");
        }
        return new double[] {a, b};
    }

    private static double sigmoid_predict(double decisionValue, double a, double b) {
        double fApB = decisionValue * a + b;
        // 1-p used later; avoid catastrophic cancellation
        if (fApB >= 0) {
            return Math.exp(-fApB) / (1.0 + Math.exp(-fApB));
        } else {
            return 1.0 / (1 + Math.exp(fApB));
        }
    }

    // Method 2 from the multiclass_prob paper by Wu, Lin, and Weng
    private static void multiclass_probability(int k, double[][] r, double[] p) {
        int t, j;
        int iter, max_iter = Math.max(100, k);
        double[][] Q = new double[k][k];
        double[] Qp = new double[k];
        double pQp, eps = 0.005 / k;

        for (t = 0; t < k; t++) {
            p[t] = 1.0 / k;  // Valid if k = 1
            Q[t][t] = 0;
            for (j = 0; j < t; j++) {
                Q[t][t] += r[j][t] * r[j][t];
                Q[t][j] = Q[j][t];
            }
            for (j = t + 1; j < k; j++) {
                Q[t][t] += r[j][t] * r[j][t];
                Q[t][j] = -r[j][t] * r[t][j];
            }
        }
        for (iter = 0; iter < max_iter; iter++) {
            // stopping condition, recalculate QP,pQP for numerical accuracy
            pQp = 0;
            for (t = 0; t < k; t++) {
                Qp[t] = 0;
                for (j = 0; j < k; j++) {
                    Qp[t] += Q[t][j] * p[j];
                }
                pQp += p[t] * Qp[t];
            }
            double max_error = 0;
            for (t = 0; t < k; t++) {
                double error = Math.abs(Qp[t] - pQp);
                if (error > max_error) {
                    max_error = error;
                }
            }
            if (max_error < eps) {
                break;
            }

            for (t = 0; t < k; t++) {
                double diff = (-Qp[t] + pQp) / Q[t][t];
                p[t] += diff;
                pQp = (pQp + diff * (diff * Q[t][t] + 2 * Qp[t])) / (1 + diff) / (1 + diff);
                for (j = 0; j < k; j++) {
                    Qp[j] = (Qp[j] + diff * Q[t][j]) / (1 + diff);
                    p[j] /= (1 + diff);
                }
            }
        }
        if (iter >= max_iter) {
            LOGGER.info("Exceeds max_iter in multiclass_prob\n");
        }
    }

    // Cross-validation decision values for probability estimates
    public static double[] svm_binary_svc_probability(SvmProblem prob, SvmParameter param, double cp, double cn) {
        int i;
        int nr_fold = 5;
        int[] perm = IntArrays.newSeq(0, prob.len);
        double[] dec_values = new double[prob.len];

        // random shuffle
        for (i = 0; i < prob.len; i++) {
            int j = i + rand.nextInt(prob.len - i);
            TArrays.swap(perm, i, j);
        }
        for (i = 0; i < nr_fold; i++) {
            int begin = i * prob.len / nr_fold;
            int end = (i + 1) * prob.len / nr_fold;
            int j, k;
            SvmProblem subprob = new SvmProblem();

            subprob.len = prob.len - (end - begin);
            subprob.xs = new DVector[subprob.len];
            subprob.y = new double[subprob.len];

            k = 0;
            for (j = 0; j < begin; j++) {
                subprob.xs[k] = prob.xs[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            for (j = end; j < prob.len; j++) {
                subprob.xs[k] = prob.xs[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            int p_count = 0, n_count = 0;
            for (j = 0; j < k; j++) {
                if (subprob.y[j] > 0) {
                    p_count++;
                } else {
                    n_count++;
                }
            }

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
                SvmParameter subparam = param.copy();
                subparam.probability = 0;
                subparam.c = 1.0;
                subparam.nrWeight = 2;
                subparam.weightLabel = new int[2];
                subparam.weight = new double[2];
                subparam.weightLabel[0] = 1;
                subparam.weightLabel[1] = -1;
                subparam.weight[0] = cp;
                subparam.weight[1] = cn;
                SvmModel submodel = svm_train(subprob, subparam);
                for (j = begin; j < end; j++) {
                    double[] dec_value = new double[1];
                    svm_predict_values(submodel, prob.xs[perm[j]], dec_value);
                    dec_values[perm[j]] = dec_value[0];
                    // ensure +1 -1 order; reason not using CV subroutine
                    dec_values[perm[j]] *= submodel.label[0];
                }
            }
        }
        return sigmoid_train(prob.len, dec_values, prob.y);
    }

    // Return parameter of a Laplace distribution
    private static double svm_svr_probability(SvmProblem prob, SvmParameter param) {
        int i;
        int nr_fold = 5;
        double[] ymv = new double[prob.len];
        double mae = 0;

        SvmParameter newparam = param.copy();
        newparam.probability = 0;
        svm_cross_validation(prob, newparam, nr_fold, ymv);
        for (i = 0; i < prob.len; i++) {
            ymv[i] = prob.y[i] - ymv[i];
            mae += Math.abs(ymv[i]);
        }
        mae /= prob.len;
        double std = Math.sqrt(2 * mae * mae);
        int count = 0;
        mae = 0;
        for (i = 0; i < prob.len; i++) {
            if (Math.abs(ymv[i]) > 5 * std) {
                count = count + 1;
            } else {
                mae += Math.abs(ymv[i]);
            }
        }
        mae /= (prob.len - count);
        LOGGER.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
                + mae + "\n");
        return mae;
    }

    // label: label name, start: begin of each class, count: #data of classes, perm: indices to the original data
    // perm, length l, must be allocated before calling this subroutine
    private static void svm_group_classes(SvmProblem prob, int[] nr_class_ret, int[][] label_ret, int[][] start_ret, int[][] count_ret,
            int[] perm) {
        int l = prob.len;
        int max_nr_class = 16;
        int nr_class = 0;
        int[] label = new int[max_nr_class];
        int[] count = new int[max_nr_class];
        int[] data_label = new int[l];
        int i;

        for (i = 0; i < l; i++) {
            int this_label = (int) (prob.y[i]);
            int j;
            for (j = 0; j < nr_class; j++) {
                if (this_label == label[j]) {
                    ++count[j];
                    break;
                }
            }
            data_label[i] = j;
            if (j == nr_class) {
                if (nr_class == max_nr_class) {
                    max_nr_class *= 2;
                    int[] new_data = new int[max_nr_class];
                    System.arraycopy(label, 0, new_data, 0, label.length);
                    label = new_data;
                    new_data = new int[max_nr_class];
                    System.arraycopy(count, 0, new_data, 0, count.length);
                    count = new_data;
                }
                label[nr_class] = this_label;
                count[nr_class] = 1;
                ++nr_class;
            }
        }

        //
        // Labels are ordered by their first occurrence in the training set.
        // However, for two-class sets with -1/+1 labels and -1 appears first,
        // we swap labels to ensure that internally the binary SVM has positive data corresponding to the +1 instances.
        //
        if (nr_class == 2 && label[0] == -1 && label[1] == 1) {
            IntArrays.swap(label, 0, 1);
            IntArrays.swap(count, 0, 1);
            for (i = 0; i < l; i++) {
                data_label[i] = data_label[i] == 0 ? 1 : 0;
            }
        }

        int[] start = new int[nr_class];
        start[0] = 0;
        for (i = 1; i < nr_class; i++) {
            start[i] = start[i - 1] + count[i - 1];
        }
        for (i = 0; i < l; i++) {
            perm[start[data_label[i]]] = i;
            ++start[data_label[i]];
        }
        start[0] = 0;
        for (i = 1; i < nr_class; i++) {
            start[i] = start[i - 1] + count[i - 1];
        }

        nr_class_ret[0] = nr_class;
        label_ret[0] = label;
        start_ret[0] = start;
        count_ret[0] = count;
    }

    public static SvmModel svm_train(SvmProblem prob, SvmParameter param) {
        SvmModel model = new SvmModel();
        model.param = param;

        if (param.svmType == svm_parameter.ONE_CLASS || param.svmType == svm_parameter.EPSILON_SVR
                || param.svmType == svm_parameter.NU_SVR) {
            // regression or one-class-svm
            model.nr_class = 2;
            model.label = null;
            model.nSV = null;
            model.probA = null;
            model.probB = null;
            model.svCoef = new double[1][];

            if (param.probability == 1 && (param.svmType == svm_parameter.EPSILON_SVR || param.svmType == svm_parameter.NU_SVR)) {
                model.probA = new double[1];
                model.probA[0] = svm_svr_probability(prob, param);
            }

            Decision f = svm_train_one(prob, param, 0, 0);
            model.rho = new double[1];
            model.rho[0] = f.rho;

            int nSV = 0;
            int i;
            for (i = 0; i < prob.len; i++) {
                if (Math.abs(f.alpha[i]) > 0) {
                    ++nSV;
                }
            }
            model.l = nSV;
            model.SV = new DVector[nSV];
            model.svCoef[0] = new double[nSV];
            model.sv_indices = new int[nSV];
            int j = 0;
            for (i = 0; i < prob.len; i++) {
                if (Math.abs(f.alpha[i]) > 0) {
                    model.SV[j] = prob.xs[i];
                    model.svCoef[0][j] = f.alpha[i];
                    model.sv_indices[j] = i + 1;
                    ++j;
                }
            }
        } else {
            // classification
            int l = prob.len;
            int[] tmp_nr_class = new int[1];
            int[][] tmp_label = new int[1][];
            int[][] tmp_start = new int[1][];
            int[][] tmp_count = new int[1][];
            int[] perm = new int[l];

            // group training data of the same class
            svm_group_classes(prob, tmp_nr_class, tmp_label, tmp_start, tmp_count, perm);
            int nr_class = tmp_nr_class[0];
            int[] label = tmp_label[0];
            int[] start = tmp_start[0];
            int[] count = tmp_count[0];

            if (nr_class == 1) {
                LOGGER.info("WARNING: training data in only one class. See README for details.\n");
            }

            DVector[] x = new DVector[l];
            for (int i = 0; i < l; i++) {
                x[i] = prob.xs[perm[i]];
            }

            // calculate weighted c

            double[] weighted_C = new double[nr_class];
            for (int i = 0; i < nr_class; i++) {
                weighted_C[i] = param.c;
            }
            for (int i = 0; i < param.nrWeight; i++) {
                int j;
                for (j = 0; j < nr_class; j++) {
                    if (param.weightLabel[i] == label[j]) {
                        break;
                    }
                }
                if (j == nr_class) {
                    System.err.print("WARNING: class label " + param.weightLabel[i] + " specified in weight is not found\n");
                } else {
                    weighted_C[j] *= param.weight[i];
                }
            }

            // train k*(k-1)/2 models

            boolean[] nonzero = new boolean[l];
            Decision[] f = new Decision[nr_class * (nr_class - 1) / 2];

            double[] probA = null, probB = null;
            if (param.probability == 1) {
                probA = new double[nr_class * (nr_class - 1) / 2];
                probB = new double[nr_class * (nr_class - 1) / 2];
            }

            int p = 0;
            for (int i = 0; i < nr_class; i++) {
                for (int j = i + 1; j < nr_class; j++) {
                    SvmProblem sub_prob = new SvmProblem();
                    int si = start[i], sj = start[j];
                    int ci = count[i], cj = count[j];
                    sub_prob.len = ci + cj;
                    sub_prob.xs = new DVector[sub_prob.len];
                    sub_prob.y = new double[sub_prob.len];
                    int k;
                    for (k = 0; k < ci; k++) {
                        sub_prob.xs[k] = x[si + k];
                        sub_prob.y[k] = 1;
                    }
                    for (k = 0; k < cj; k++) {
                        sub_prob.xs[ci + k] = x[sj + k];
                        sub_prob.y[ci + k] = -1;
                    }

                    if (param.probability == 1) {
                        double[] probAB = svm_binary_svc_probability(sub_prob, param, weighted_C[i], weighted_C[j]);
                        probA[p] = probAB[0];
                        probB[p] = probAB[1];
                    }

                    f[p] = svm_train_one(sub_prob, param, weighted_C[i], weighted_C[j]);
                    for (k = 0; k < ci; k++) {
                        if (Math.abs(f[p].alpha[k]) > 0) {
                            nonzero[si + k] = true;
                        }
                    }
                    for (k = 0; k < cj; k++) {
                        if (Math.abs(f[p].alpha[ci + k]) > 0) {
                            nonzero[sj + k] = true;
                        }
                    }
                    ++p;
                }
            }

            // build output

            model.nr_class = nr_class;

            model.label = new int[nr_class];
            System.arraycopy(label, 0, model.label, 0, nr_class);

            model.rho = new double[nr_class * (nr_class - 1) / 2];
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                model.rho[i] = f[i].rho;
            }

            if (param.probability == 1) {
                model.probA = new double[nr_class * (nr_class - 1) / 2];
                model.probB = new double[nr_class * (nr_class - 1) / 2];
                for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                    model.probA[i] = probA[i];
                    model.probB[i] = probB[i];
                }
            } else {
                model.probA = null;
                model.probB = null;
            }

            int total_sv = 0;
            int[] nz_count = new int[nr_class];
            model.nSV = new int[nr_class];
            for (int i = 0; i < nr_class; i++) {
                int nSV = 0;
                for (int j = 0; j < count[i]; j++) {
                    if (nonzero[start[i] + j]) {
                        ++nSV;
                        ++total_sv;
                    }
                }
                model.nSV[i] = nSV;
                nz_count[i] = nSV;
            }

            LOGGER.info("Total nSV = " + total_sv + "\n");

            model.l = total_sv;
            model.SV = new DVector[total_sv];
            model.sv_indices = new int[total_sv];
            p = 0;
            for (int i = 0; i < l; i++) {
                if (nonzero[i]) {
                    model.SV[p] = x[i];
                    model.sv_indices[p++] = perm[i] + 1;
                }
            }

            int[] nz_start = new int[nr_class];
            nz_start[0] = 0;
            for (int i = 1; i < nr_class; i++) {
                nz_start[i] = nz_start[i - 1] + nz_count[i - 1];
            }

            model.svCoef = new double[nr_class - 1][];
            for (int i = 0; i < nr_class - 1; i++) {
                model.svCoef[i] = new double[total_sv];
            }

            p = 0;
            for (int i = 0; i < nr_class; i++) {
                for (int j = i + 1; j < nr_class; j++) {
                    // classifier (i,j): coefficients with
                    // i are in sv_coef[j-1][nz_start[i]...],
                    // j are in sv_coef[i][nz_start[j]...]

                    int si = start[i];
                    int sj = start[j];

                    int q = nz_start[i];
                    int k;
                    for (k = 0; k < count[i]; k++) {
                        if (nonzero[si + k]) {
                            model.svCoef[j - 1][q++] = f[p].alpha[k];
                        }
                    }
                    q = nz_start[j];
                    for (k = 0; k < count[j]; k++) {
                        if (nonzero[sj + k]) {
                            model.svCoef[i][q++] = f[p].alpha[count[i] + k];
                        }
                    }
                    p++;
                }
            }
        }
        return model;
    }

    // Stratified cross validation
    public static void svm_cross_validation(SvmProblem prob, SvmParameter param, int nrFold, double[] target) {
        int i;
        int[] fold_start = new int[nrFold + 1];
        int l = prob.len;
        int[] perm = new int[l];

        // stratified cv may not give leave-one-out rate
        // Each class to l folds -> some folds may have zero elements
        if ((param.svmType == SvmParameter.C_SVC ||
                param.svmType == SvmParameter.NU_SVC) && nrFold < l) {
            int[] tmp_nr_class = new int[1];
            int[][] tmp_label = new int[1][];
            int[][] tmp_start = new int[1][];
            int[][] tmp_count = new int[1][];

            svm_group_classes(prob, tmp_nr_class, tmp_label, tmp_start, tmp_count, perm);

            int nr_class = tmp_nr_class[0];
            int[] start = tmp_start[0];
            int[] count = tmp_count[0];

            // random shuffle and then data grouped by fold using the array perm
            int[] fold_count = new int[nrFold];
            int c;
            int[] index = new int[l];
            for (i = 0; i < l; i++) {
                index[i] = perm[i];
            }
            for (c = 0; c < nr_class; c++) {
                for (i = 0; i < count[c]; i++) {
                    int j = i + rand.nextInt(count[c] - i);
                    IntArrays.swap(index, start[c] + i, start[c] + j);
                }
            }
            for (i = 0; i < nrFold; i++) {
                fold_count[i] = 0;
                for (c = 0; c < nr_class; c++) {
                    fold_count[i] += (i + 1) * count[c] / nrFold - i * count[c] / nrFold;
                }
            }
            fold_start[0] = 0;
            for (i = 1; i <= nrFold; i++) {
                fold_start[i] = fold_start[i - 1] + fold_count[i - 1];
            }
            for (c = 0; c < nr_class; c++) {
                for (i = 0; i < nrFold; i++) {
                    int begin = start[c] + i * count[c] / nrFold;
                    int end = start[c] + (i + 1) * count[c] / nrFold;
                    for (int j = begin; j < end; j++) {
                        perm[fold_start[i]] = index[j];
                        fold_start[i]++;
                    }
                }
            }
            fold_start[0] = 0;
            for (i = 1; i <= nrFold; i++) {
                fold_start[i] = fold_start[i - 1] + fold_count[i - 1];
            }
        } else {
            for (i = 0; i < l; i++) {
                perm[i] = i;
            }
            for (i = 0; i < l; i++) {
                int j = i + rand.nextInt(l - i);
                IntArrays.swap(perm, i, j);
            }
            for (i = 0; i <= nrFold; i++) {
                fold_start[i] = i * l / nrFold;
            }
        }

        for (i = 0; i < nrFold; i++) {
            int begin = fold_start[i];
            int end = fold_start[i + 1];
            int j, k;
            SvmProblem subprob = new SvmProblem();

            subprob.len = l - (end - begin);
            subprob.xs = new DVector[subprob.len];
            subprob.y = new double[subprob.len];

            k = 0;
            for (j = 0; j < begin; j++) {
                subprob.xs[k] = prob.xs[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            for (j = end; j < l; j++) {
                subprob.xs[k] = prob.xs[perm[j]];
                subprob.y[k] = prob.y[perm[j]];
                ++k;
            }
            SvmModel submodel = svm_train(subprob, param);
            if (param.probability == 1 &&
                    (param.svmType == SvmParameter.C_SVC ||
                            param.svmType == SvmParameter.NU_SVC)) {
                double[] prob_estimates = new double[submodel.nr_class];
                for (j = begin; j < end; j++) {
                    target[perm[j]] = svm_predict_probability(submodel, prob.xs[perm[j]], prob_estimates);
                }
            } else {
                for (j = begin; j < end; j++) {
                    target[perm[j]] = svm_predict(submodel, prob.xs[perm[j]]);
                }
            }
        }
    }

    public static double svm_predict(SvmModel model, DVector x) {
        double[] decisionvalues;
        if (model.param.svmType == SvmParameter.ONE_CLASS ||
                model.param.svmType == SvmParameter.EPSILON_SVR ||
                model.param.svmType == SvmParameter.NU_SVR) {
            decisionvalues = new double[1];
        } else {
            decisionvalues = new double[model.nr_class * (model.nr_class - 1) / 2];
        }
        return svm_predict_values(model, x, decisionvalues);
    }

    public static double svm_predict_values(SvmModel model, DVector x, double[] decisionValues) {
        if (model.param.svmType == SvmParameter.ONE_CLASS ||
                model.param.svmType == SvmParameter.EPSILON_SVR ||
                model.param.svmType == SvmParameter.NU_SVR) {
            double[] sv_coef = model.svCoef[0];
            double sum = 0;
            for (int i = 0; i < model.l; i++) {
                sum += sv_coef[i] * model.param.kernel.compute(x, model.SV[i]);
            }
            sum -= model.rho[0];
            decisionValues[0] = sum;

            if (model.param.svmType == SvmParameter.ONE_CLASS) {
                return (sum > 0) ? 1 : -1;
            } else {
                return sum;
            }
        } else {
            int nr_class = model.nr_class;
            int l = model.l;

            double[] kvalue = new double[l];
            for (int i = 0; i < l; i++) {
                kvalue[i] = model.param.kernel.compute(x, model.SV[i]);
            }

            int[] start = new int[nr_class];
            start[0] = 0;
            for (int i = 1; i < nr_class; i++) {
                start[i] = start[i - 1] + model.nSV[i - 1];
            }

            int[] vote = new int[nr_class];

            int p = 0;
            for (int i = 0; i < nr_class; i++) {
                for (int j = i + 1; j < nr_class; j++) {
                    double sum = 0;
                    int si = start[i];
                    int sj = start[j];
                    int ci = model.nSV[i];
                    int cj = model.nSV[j];

                    int k;
                    double[] coef1 = model.svCoef[j - 1];
                    double[] coef2 = model.svCoef[i];
                    for (k = 0; k < ci; k++) {
                        sum += coef1[si + k] * kvalue[si + k];
                    }
                    for (k = 0; k < cj; k++) {
                        sum += coef2[sj + k] * kvalue[sj + k];
                    }
                    sum -= model.rho[p];
                    decisionValues[p] = sum;

                    if (decisionValues[p] > 0) {
                        ++vote[i];
                    } else {
                        ++vote[j];
                    }
                    p++;
                }
            }

            int vote_max_idx = 0;
            for (int i = 1; i < nr_class; i++) {
                if (vote[i] > vote[vote_max_idx]) {
                    vote_max_idx = i;
                }
            }

            return model.label[vote_max_idx];
        }
    }

    public static double svm_predict_probability(SvmModel model, DVector x, double[] probEstimates) {
        if ((model.param.svmType == SvmParameter.C_SVC || model.param.svmType == SvmParameter.NU_SVC) &&
                model.probA != null && model.probB != null) {
            int i;
            int nr_class = model.nr_class;
            double[] decisionValues = new double[nr_class * (nr_class - 1) / 2];
            svm_predict_values(model, x, decisionValues);

            double min_prob = 1e-7;
            double[][] pairwise_prob = new double[nr_class][nr_class];

            int k = 0;
            for (i = 0; i < nr_class; i++) {
                for (int j = i + 1; j < nr_class; j++) {
                    pairwise_prob[i][j] =
                            Math.min(Math.max(sigmoid_predict(decisionValues[k], model.probA[k], model.probB[k]), min_prob), 1 - min_prob);
                    pairwise_prob[j][i] = 1 - pairwise_prob[i][j];
                    k++;
                }
            }
            if (nr_class == 2) {
                probEstimates[0] = pairwise_prob[0][1];
                probEstimates[1] = pairwise_prob[1][0];
            } else {
                multiclass_probability(nr_class, pairwise_prob, probEstimates);
            }

            int prob_max_idx = 0;
            for (i = 1; i < nr_class; i++) {
                if (probEstimates[i] > probEstimates[prob_max_idx]) {
                    prob_max_idx = i;
                }
            }
            return model.label[prob_max_idx];
        } else {
            return svm_predict(model, x);
        }
    }

    record Decision(double[] alpha, double rho) {
    }

    public static class SolutionInfo {
        double obj;
        double rho;
        double pUpperBound;
        double nUpperBound;
        double r;
    }
}