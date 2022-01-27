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

import java.util.logging.Logger;

import rapaio.util.collection.TArrays;

/**
 * An SMO algorithm in Fan et al., JMLR 6(2005), p. 1889--1918
 * Solves:
 * <p>
 * min 0.5(\alpha^T Q \alpha) + p^T \alpha
 * <p>
 * y^T \alpha = \delta
 * y_i = +1 or -1
 * 0 <= alpha_i <= Cp for y_i = 1
 * 0 <= alpha_i <= Cn for y_i = -1
 * <p>
 * Given:
 * <p>
 * Q, p, y, Cp, Cn, and an initial feasible point \alpha
 * l is the size of vectors and matrices
 * eps is the stopping tolerance
 * <p>
 * solution will be put in \alpha, objective value will be put in obj
 */
public class SolverC {
    private static final Logger LOGGER = Logger.getLogger(SolverC.class.getName());

    static final byte ALPHA_STATUS_LOWER_BOUND = 0;
    static final byte ALPHA_STATUS_UPPER_BOUND = 1;
    static final byte ALPHA_STATUS_FREE = 2;

    int activeSize;
    byte[] y;
    double[] grad;        // gradient of objective function
    byte[] alphaStatus;    // LOWER_BOUND, UPPER_BOUND, FREE
    double[] alpha;
    AbstractKernelMatrix q;
    double[] qd;
    double eps;
    double cp;
    double cn;
    double[] p;
    int[] activeSet;
    double[] gradBar;        // gradient, if we treat free variables as 0
    int len;
    boolean unshrink;    // XXX

    double getC(int i) {
        return (y[i] > 0) ? cp : cn;
    }

    void update_alpha_status(int i) {
        if (alpha[i] >= getC(i)) {
            alphaStatus[i] = ALPHA_STATUS_UPPER_BOUND;
        } else if (alpha[i] <= 0) {
            alphaStatus[i] = ALPHA_STATUS_LOWER_BOUND;
        } else {
            alphaStatus[i] = ALPHA_STATUS_FREE;
        }
    }

    boolean isUpperBound(int i) {
        return alphaStatus[i] == ALPHA_STATUS_UPPER_BOUND;
    }

    boolean isLowerBound(int i) {
        return alphaStatus[i] == ALPHA_STATUS_LOWER_BOUND;
    }

    boolean is_free(int i) {
        return alphaStatus[i] == ALPHA_STATUS_FREE;
    }

    void swapIndex(int i, int j) {
        q.swapIndex(i, j);
        TArrays.swap(y, i, j);
        TArrays.swap(grad, i, j);
        TArrays.swap(alphaStatus, i, j);
        TArrays.swap(alpha, i, j);
        TArrays.swap(p, i, j);
        TArrays.swap(activeSet, i, j);
        TArrays.swap(gradBar, i, j);
    }

    void reconstruct_gradient() {
        // reconstruct inactive elements of grad from gradBar and free variables

        if (activeSize == len) {
            return;
        }

        int i, j;
        int nr_free = 0;

        for (j = activeSize; j < len; j++) {
            grad[j] = gradBar[j] + p[j];
        }

        for (j = 0; j < activeSize; j++) {
            if (is_free(j)) {
                nr_free++;
            }
        }

        if (2 * nr_free < activeSize) {
            LOGGER.info("\nWARNING: using -h 0 may be faster\n");
        }

        if (nr_free * len > 2 * activeSize * (len - activeSize)) {
            for (i = activeSize; i < len; i++) {
                double[] Q_i = q.getQ(i, activeSize);
                for (j = 0; j < activeSize; j++) {
                    if (is_free(j)) {
                        grad[i] += alpha[j] * Q_i[j];
                    }
                }
            }
        } else {
            for (i = 0; i < activeSize; i++) {
                if (is_free(i)) {
                    double[] Q_i = q.getQ(i, len);
                    double alpha_i = alpha[i];
                    for (j = activeSize; j < len; j++) {
                        grad[j] += alpha_i * Q_i[j];
                    }
                }
            }
        }
    }

    void initializeAlphaStatus(int trainingSize) {
        alphaStatus = new byte[trainingSize];
        for (int i = 0; i < trainingSize; i++) {
            update_alpha_status(i);
        }
    }

    void initializeActiveSet(int trainingSize) {
        activeSet = new int[trainingSize];
        for (int i = 0; i < trainingSize; i++) {
            activeSet[i] = i;
        }
        activeSize = trainingSize;
    }

    void initializeGradient(int trainingSize) {
        grad = new double[trainingSize];
        gradBar = new double[trainingSize];
        for (int i = 0; i < trainingSize; i++) {
            grad[i] = p[i];
            gradBar[i] = 0;
        }
        for (int i = 0; i < trainingSize; i++) {
            if (!isLowerBound(i)) {
                double[] Q_i = q.getQ(i, trainingSize);
                double alpha_i = alpha[i];
                int j;
                for (j = 0; j < trainingSize; j++) {
                    grad[j] += alpha_i * Q_i[j];
                }
                if (isUpperBound(i)) {
                    for (j = 0; j < trainingSize; j++) {
                        gradBar[j] += getC(i) * Q_i[j];
                    }
                }
            }
        }
    }

    record WorkingSet(int i, int j) {
    }

    public void solve(int len, AbstractKernelMatrix Q, double[] p_, byte[] y_,
            double[] alpha_, double Cp, double Cn, double eps, Svm.SolutionInfo si, int shrinking) {
        this.len = len;
        this.q = Q;
        this.qd = Q.getQD();
        this.p = p_.clone();
        this.y = y_.clone();
        this.alpha = alpha_.clone();
        this.cp = Cp;
        this.cn = Cn;
        this.eps = eps;
        this.unshrink = false;

        // initialize alpha_status
        initializeAlphaStatus(len);

        // initialize active set (for shrinking)
        initializeActiveSet(len);

        // initialize gradient
        initializeGradient(len);

        // optimization step

        int iter = 0;
        int maxIter = Math.max(10_000_000, len > Integer.MAX_VALUE / 100 ? Integer.MAX_VALUE : 100 * len);
        int counter = Math.min(len, 1000) + 1;

        while (iter < maxIter) {

            // show progress and do shrinking
            if (--counter == 0) {
                counter = Math.min(len, 1000);
                if (shrinking != 0) {
                    doShrinking();
                }
                LOGGER.info(".");
            }

            WorkingSet ws = selectWorkingSet();
            if (ws == null) {
                // reconstruct the whole gradient
                reconstruct_gradient();
                // reset active set size and check
                activeSize = len;
                LOGGER.info("*");
                ws = selectWorkingSet();
                if (ws == null) {
                    break;
                } else {
                    counter = 1;    // do shrinking next iteration
                }
            }

            int i = ws.i;
            int j = ws.j;

            ++iter;

            // update alpha[i] and alpha[j], handle bounds carefully

            double[] qi = Q.getQ(i, activeSize);
            double[] qj = Q.getQ(j, activeSize);

            double ci = getC(i);
            double cj = getC(j);

            double old_alpha_i = alpha[i];
            double old_alpha_j = alpha[j];

            if (y[i] != y[j]) {
                double quad_coef = qd[i] + qd[j] + 2 * qi[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (-grad[i] - grad[j]) / quad_coef;
                double diff = alpha[i] - alpha[j];
                alpha[i] += delta;
                alpha[j] += delta;

                if (diff > 0) {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = diff;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = -diff;
                    }
                }
                if (diff > ci - cj) {
                    if (alpha[i] > ci) {
                        alpha[i] = ci;
                        alpha[j] = ci - diff;
                    }
                } else {
                    if (alpha[j] > cj) {
                        alpha[j] = cj;
                        alpha[i] = cj + diff;
                    }
                }
            } else {
                double quad_coef = qd[i] + qd[j] - 2 * qi[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (grad[i] - grad[j]) / quad_coef;
                double sum = alpha[i] + alpha[j];
                alpha[i] -= delta;
                alpha[j] += delta;

                if (sum > ci) {
                    if (alpha[i] > ci) {
                        alpha[i] = ci;
                        alpha[j] = sum - ci;
                    }
                } else {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = sum;
                    }
                }
                if (sum > cj) {
                    if (alpha[j] > cj) {
                        alpha[j] = cj;
                        alpha[i] = sum - cj;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = sum;
                    }
                }
            }

            // update grad

            double delta_alpha_i = alpha[i] - old_alpha_i;
            double delta_alpha_j = alpha[j] - old_alpha_j;

            for (int k = 0; k < activeSize; k++) {
                grad[k] += qi[k] * delta_alpha_i + qj[k] * delta_alpha_j;
            }

            // update alpha_status and G_bar

            boolean ui = isUpperBound(i);
            boolean uj = isUpperBound(j);
            update_alpha_status(i);
            update_alpha_status(j);
            int k;
            if (ui != isUpperBound(i)) {
                qi = Q.getQ(i, len);
                if (ui) {
                    for (k = 0; k < len; k++) {
                        gradBar[k] -= ci * qi[k];
                    }
                } else {
                    for (k = 0; k < len; k++) {
                        gradBar[k] += ci * qi[k];
                    }
                }
            }

            if (uj != isUpperBound(j)) {
                qj = Q.getQ(j, len);
                if (uj) {
                    for (k = 0; k < len; k++) {
                        gradBar[k] -= cj * qj[k];
                    }
                } else {
                    for (k = 0; k < len; k++) {
                        gradBar[k] += cj * qj[k];
                    }
                }
            }
        }

        if (iter >= maxIter) {
            if (activeSize < len) {
                // reconstruct the whole gradient to calculate objective value
                reconstruct_gradient();
                activeSize = len;
                LOGGER.info("*");
            }
            System.err.print("\nWARNING: reaching max number of iterations\n");
        }

        // calculate rho

        si.rho = calculateRho();

        // calculate objective value
        double v = 0;
        for (int i = 0; i < len; i++) {
            v += alpha[i] * (grad[i] + p[i]);
        }

        si.obj = v / 2;

        // put back the solution
        for (int i = 0; i < len; i++) {
            alpha_[activeSet[i]] = alpha[i];
        }

        si.pUpperBound = Cp;
        si.nUpperBound = Cn;

        LOGGER.info("\noptimization finished, #iter = " + iter + "\n");
    }

    /**
     * Return i,j such that:
     * <p>
     * i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
     * j: minimizes the decrease of obj value
     * (if quadratic coefficient <= 0, replace it with \tau)
     * -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)
     * Returns null if already optimal.
     */
    WorkingSet selectWorkingSet() {

        double gradMax1 = Double.NEGATIVE_INFINITY;
        double gradMax2 = Double.NEGATIVE_INFINITY;
        int Gmax_idx = -1;
        int Gmin_idx = -1;
        double obj_diff_min = Double.POSITIVE_INFINITY;

        for (int t = 0; t < activeSize; t++) {
            if (y[t] == +1) {
                if (!isUpperBound(t)) {
                    if (-grad[t] >= gradMax1) {
                        gradMax1 = -grad[t];
                        Gmax_idx = t;
                    }
                }
            } else {
                if (!isLowerBound(t)) {
                    if (grad[t] >= gradMax1) {
                        gradMax1 = grad[t];
                        Gmax_idx = t;
                    }
                }
            }
        }

        int i = Gmax_idx;
        double[] Q_i = null;
        // null Q_i not accessed: Gmax=-INF if i=-1
        if (i != -1) {
            Q_i = q.getQ(i, activeSize);
        }

        for (int j = 0; j < activeSize; j++) {
            if (y[j] == +1) {
                if (!isLowerBound(j)) {
                    double grad_diff = gradMax1 + grad[j];
                    if (grad[j] >= gradMax2) {
                        gradMax2 = grad[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = qd[i] + qd[j] - 2.0 * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            } else {
                if (!isUpperBound(j)) {
                    double grad_diff = gradMax1 - grad[j];
                    if (-grad[j] >= gradMax2) {
                        gradMax2 = -grad[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = qd[i] + qd[j] + 2.0 * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            }
        }

        if (gradMax1 + gradMax2 < eps || Gmin_idx == -1) {
            return null;
        }

        return new WorkingSet(Gmax_idx, Gmin_idx);
    }

    private boolean needsShrinking(int i, double gradMax1, double gradMax2) {
        if (isUpperBound(i)) {
            return (y[i] == +1) ? -grad[i] > gradMax1 : -grad[i] > gradMax2;
        }
        if (isLowerBound(i)) {
            return (y[i] == +1) ? grad[i] > gradMax2 : grad[i] > gradMax1;
        }
        return (false);
    }

    void doShrinking() {
        int i;
        double gradMax1 = Double.NEGATIVE_INFINITY;        // max { -y_i * grad(f)_i | i in I_up(\alpha) }
        double gradMax2 = Double.NEGATIVE_INFINITY;        // max { y_i * grad(f)_i | i in I_low(\alpha) }

        // find maximal violating pair first
        for (i = 0; i < activeSize; i++) {
            if (y[i] == +1) {
                if (!isUpperBound(i)) {
                    if (-grad[i] >= gradMax1) {
                        gradMax1 = -grad[i];
                    }
                }
                if (!isLowerBound(i)) {
                    if (grad[i] >= gradMax2) {
                        gradMax2 = grad[i];
                    }
                }
            } else {
                if (!isUpperBound(i)) {
                    if (-grad[i] >= gradMax2) {
                        gradMax2 = -grad[i];
                    }
                }
                if (!isLowerBound(i)) {
                    if (grad[i] >= gradMax1) {
                        gradMax1 = grad[i];
                    }
                }
            }
        }

        if (!unshrink && gradMax1 + gradMax2 <= eps * 10) {
            unshrink = true;
            reconstruct_gradient();
            activeSize = len;
            LOGGER.info("*");
        }

        for (i = 0; i < activeSize; i++) {
            if (needsShrinking(i, gradMax1, gradMax2)) {
                activeSize--;
                while (activeSize > i) {
                    if (!needsShrinking(activeSize, gradMax1, gradMax2)) {
                        swapIndex(i, activeSize);
                        break;
                    }
                    activeSize--;
                }
            }
        }
    }

    double calculateRho() {
        int freeCount = 0;
        double ub = Double.POSITIVE_INFINITY;
        double lb = Double.NEGATIVE_INFINITY;
        double freeSum = 0;
        for (int i = 0; i < activeSize; i++) {
            double yg = y[i] * grad[i];
            if (isUpperBound(i)) {
                if (y[i] < 0) {
                    ub = Math.min(ub, yg);
                } else {
                    lb = Math.max(lb, yg);
                }
            } else if (isLowerBound(i)) {
                if (y[i] > 0) {
                    ub = Math.min(ub, yg);
                } else {
                    lb = Math.max(lb, yg);
                }
            } else {
                freeCount++;
                freeSum += yg;
            }
        }

        if (freeCount > 0) {
            return freeSum / freeCount;
        } else {
            return (ub + lb) / 2;
        }
    }

}
