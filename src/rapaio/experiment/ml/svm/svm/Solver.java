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

package rapaio.experiment.ml.svm.svm;

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
public class Solver {
    int active_size;
    byte[] y;
    double[] G;        // gradient of objective function
    static final byte LOWER_BOUND = 0;
    static final byte UPPER_BOUND = 1;
    static final byte FREE = 2;
    byte[] alpha_status;    // LOWER_BOUND, UPPER_BOUND, FREE
    double[] alpha;
    AbstractKernelMatrix Q;
    double[] QD;
    double eps;
    double Cp, Cn;
    double[] p;
    int[] active_set;
    double[] G_bar;        // gradient, if we treat free variables as 0
    int l;
    boolean unshrink;    // XXX

    static final double INF = Double.POSITIVE_INFINITY;

    double get_C(int i) {
        return (y[i] > 0) ? Cp : Cn;
    }

    void update_alpha_status(int i) {
        if (alpha[i] >= get_C(i)) {
            alpha_status[i] = UPPER_BOUND;
        } else if (alpha[i] <= 0) {
            alpha_status[i] = LOWER_BOUND;
        } else {
            alpha_status[i] = FREE;
        }
    }

    boolean is_upper_bound(int i) {
        return alpha_status[i] == UPPER_BOUND;
    }

    boolean is_lower_bound(int i) {
        return alpha_status[i] == LOWER_BOUND;
    }

    boolean is_free(int i) {
        return alpha_status[i] == FREE;
    }

    // java: information about solution except alpha,
    // because we cannot return multiple values otherwise...
    public static class SolutionInfo {
        public double obj;
        public double rho;
        public double upper_bound_p;
        public double upper_bound_n;
        public double r;    // for Solver_NU
    }

    void swap_index(int i, int j) {
        Q.swap_index(i, j);
        TArrays.swap(y, i, j);
        TArrays.swap(G, i, j);
        TArrays.swap(alpha_status, i, j);
        TArrays.swap(alpha, i, j);
        TArrays.swap(p, i, j);
        TArrays.swap(active_set, i, j);
        TArrays.swap(G_bar, i, j);
    }

    void reconstruct_gradient() {
        // reconstruct inactive elements of G from G_bar and free variables

        if (active_size == l) {
            return;
        }

        int i, j;
        int nr_free = 0;

        for (j = active_size; j < l; j++) {
            G[j] = G_bar[j] + p[j];
        }

        for (j = 0; j < active_size; j++) {
            if (is_free(j)) {
                nr_free++;
            }
        }

        if (2 * nr_free < active_size) {
            Svm.info("\nWARNING: using -h 0 may be faster\n");
        }

        if (nr_free * l > 2 * active_size * (l - active_size)) {
            for (i = active_size; i < l; i++) {
                float[] Q_i = Q.get_Q(i, active_size);
                for (j = 0; j < active_size; j++) {
                    if (is_free(j)) {
                        G[i] += alpha[j] * Q_i[j];
                    }
                }
            }
        } else {
            for (i = 0; i < active_size; i++) {
                if (is_free(i)) {
                    float[] Q_i = Q.get_Q(i, l);
                    double alpha_i = alpha[i];
                    for (j = active_size; j < l; j++) {
                        G[j] += alpha_i * Q_i[j];
                    }
                }
            }
        }
    }

    void initializeAlphaStatus(int trainingSize) {
        alpha_status = new byte[trainingSize];
        for (int i = 0; i < trainingSize; i++) {
            update_alpha_status(i);
        }
    }

    void initializeActiveSet(int trainingSize) {
        active_set = new int[trainingSize];
        for (int i = 0; i < trainingSize; i++) {
            active_set[i] = i;
        }
        active_size = trainingSize;
    }

    void initializeGradient(int trainingSize) {
        G = new double[trainingSize];
        G_bar = new double[trainingSize];
        for (int i = 0; i < trainingSize; i++) {
            G[i] = p[i];
            G_bar[i] = 0;
        }
        for (int i = 0; i < trainingSize; i++) {
            if (!is_lower_bound(i)) {
                float[] Q_i = Q.get_Q(i, trainingSize);
                double alpha_i = alpha[i];
                int j;
                for (j = 0; j < trainingSize; j++) {
                    G[j] += alpha_i * Q_i[j];
                }
                if (is_upper_bound(i)) {
                    for (j = 0; j < trainingSize; j++) {
                        G_bar[j] += get_C(i) * Q_i[j];
                    }
                }
            }
        }
    }

    record WorkingSet(int i, int j) {
    }

    public void Solve(int trainingSize, AbstractKernelMatrix Q, double[] p_, byte[] y_,
            double[] alpha_, double Cp, double Cn, double eps, SolutionInfo si, int shrinking) {
        this.l = trainingSize;
        this.Q = Q;
        QD = Q.get_QD();
        p = p_.clone();
        y = y_.clone();
        alpha = alpha_.clone();
        this.Cp = Cp;
        this.Cn = Cn;
        this.eps = eps;
        this.unshrink = false;

        // initialize alpha_status
        initializeAlphaStatus(trainingSize);

        // initialize active set (for shrinking)
        initializeActiveSet(trainingSize);

        // initialize gradient
        initializeGradient(trainingSize);

        // optimization step

        int iter = 0;
        int max_iter = Math.max(10000000, trainingSize > Integer.MAX_VALUE / 100 ? Integer.MAX_VALUE : 100 * trainingSize);
        int counter = Math.min(trainingSize, 1000) + 1;

        while (iter < max_iter) {
            // show progress and do shrinking

            if (--counter == 0) {
                counter = Math.min(trainingSize, 1000);
                if (shrinking != 0) {
                    do_shrinking();
                }
                Svm.info(".");
            }

            WorkingSet ws = select_working_set();
            if (ws == null) {
                // reconstruct the whole gradient
                reconstruct_gradient();
                // reset active set size and check
                active_size = trainingSize;
                Svm.info("*");
                ws = select_working_set();
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

            float[] Q_i = Q.get_Q(i, active_size);
            float[] Q_j = Q.get_Q(j, active_size);

            double C_i = get_C(i);
            double C_j = get_C(j);

            double old_alpha_i = alpha[i];
            double old_alpha_j = alpha[j];

            if (y[i] != y[j]) {
                double quad_coef = QD[i] + QD[j] + 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (-G[i] - G[j]) / quad_coef;
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
                if (diff > C_i - C_j) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = C_i - diff;
                    }
                } else {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = C_j + diff;
                    }
                }
            } else {
                double quad_coef = QD[i] + QD[j] - 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (G[i] - G[j]) / quad_coef;
                double sum = alpha[i] + alpha[j];
                alpha[i] -= delta;
                alpha[j] += delta;

                if (sum > C_i) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = sum - C_i;
                    }
                } else {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = sum;
                    }
                }
                if (sum > C_j) {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = sum - C_j;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = sum;
                    }
                }
            }

            // update G

            double delta_alpha_i = alpha[i] - old_alpha_i;
            double delta_alpha_j = alpha[j] - old_alpha_j;

            for (int k = 0; k < active_size; k++) {
                G[k] += Q_i[k] * delta_alpha_i + Q_j[k] * delta_alpha_j;
            }

            // update alpha_status and G_bar

            {
                boolean ui = is_upper_bound(i);
                boolean uj = is_upper_bound(j);
                update_alpha_status(i);
                update_alpha_status(j);
                int k;
                if (ui != is_upper_bound(i)) {
                    Q_i = Q.get_Q(i, trainingSize);
                    if (ui) {
                        for (k = 0; k < trainingSize; k++) {
                            G_bar[k] -= C_i * Q_i[k];
                        }
                    } else {
                        for (k = 0; k < trainingSize; k++) {
                            G_bar[k] += C_i * Q_i[k];
                        }
                    }
                }

                if (uj != is_upper_bound(j)) {
                    Q_j = Q.get_Q(j, trainingSize);
                    if (uj) {
                        for (k = 0; k < trainingSize; k++) {
                            G_bar[k] -= C_j * Q_j[k];
                        }
                    } else {
                        for (k = 0; k < trainingSize; k++) {
                            G_bar[k] += C_j * Q_j[k];
                        }
                    }
                }
            }

        }

        if (iter >= max_iter) {
            if (active_size < trainingSize) {
                // reconstruct the whole gradient to calculate objective value
                reconstruct_gradient();
                active_size = trainingSize;
                Svm.info("*");
            }
            System.err.print("\nWARNING: reaching max number of iterations\n");
        }

        // calculate rho

        si.rho = calculate_rho();

        // calculate objective value
        {
            double v = 0;
            int i;
            for (i = 0; i < trainingSize; i++) {
                v += alpha[i] * (G[i] + p[i]);
            }

            si.obj = v / 2;
        }

        // put back the solution
        {
            for (int i = 0; i < trainingSize; i++) {
                alpha_[active_set[i]] = alpha[i];
            }
        }

        si.upper_bound_p = Cp;
        si.upper_bound_n = Cn;

        Svm.info("\noptimization finished, #iter = " + iter + "\n");
    }

    // return 1 if already optimal, return 0 otherwise
    WorkingSet select_working_set() {
        // return i,j such that
        // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
        // j: minimizes the decrease of obj value
        //    (if quadratic coefficeint <= 0, replace it with tau)
        //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

        double Gmax = -INF;
        double Gmax2 = -INF;
        int Gmax_idx = -1;
        int Gmin_idx = -1;
        double obj_diff_min = INF;

        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!is_upper_bound(t)) {
                    if (-G[t] >= Gmax) {
                        Gmax = -G[t];
                        Gmax_idx = t;
                    }
                }
            } else {
                if (!is_lower_bound(t)) {
                    if (G[t] >= Gmax) {
                        Gmax = G[t];
                        Gmax_idx = t;
                    }
                }
            }
        }

        int i = Gmax_idx;
        float[] Q_i = null;
        if (i != -1) // null Q_i not accessed: Gmax=-INF if i=-1
        {
            Q_i = Q.get_Q(i, active_size);
        }

        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!is_lower_bound(j)) {
                    double grad_diff = Gmax + G[j];
                    if (G[j] >= Gmax2) {
                        Gmax2 = G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[i] + QD[j] - 2.0 * y[i] * Q_i[j];
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
                if (!is_upper_bound(j)) {
                    double grad_diff = Gmax - G[j];
                    if (-G[j] >= Gmax2) {
                        Gmax2 = -G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = QD[i] + QD[j] + 2.0 * y[i] * Q_i[j];
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

        if (Gmax + Gmax2 < eps || Gmin_idx == -1) {
            return null;
        }

        return new WorkingSet(Gmax_idx, Gmin_idx);
    }

    private boolean be_shrunk(int i, double Gmax1, double Gmax2) {
        if (is_upper_bound(i)) {
            if (y[i] == +1) {
                return (-G[i] > Gmax1);
            } else {
                return (-G[i] > Gmax2);
            }
        } else if (is_lower_bound(i)) {
            if (y[i] == +1) {
                return (G[i] > Gmax2);
            } else {
                return (G[i] > Gmax1);
            }
        } else {
            return (false);
        }
    }

    void do_shrinking() {
        int i;
        double Gmax1 = -INF;        // max { -y_i * grad(f)_i | i in I_up(\alpha) }
        double Gmax2 = -INF;        // max { y_i * grad(f)_i | i in I_low(\alpha) }

        // find maximal violating pair first
        for (i = 0; i < active_size; i++) {
            if (y[i] == +1) {
                if (!is_upper_bound(i)) {
                    if (-G[i] >= Gmax1) {
                        Gmax1 = -G[i];
                    }
                }
                if (!is_lower_bound(i)) {
                    if (G[i] >= Gmax2) {
                        Gmax2 = G[i];
                    }
                }
            } else {
                if (!is_upper_bound(i)) {
                    if (-G[i] >= Gmax2) {
                        Gmax2 = -G[i];
                    }
                }
                if (!is_lower_bound(i)) {
                    if (G[i] >= Gmax1) {
                        Gmax1 = G[i];
                    }
                }
            }
        }

        if (unshrink == false && Gmax1 + Gmax2 <= eps * 10) {
            unshrink = true;
            reconstruct_gradient();
            active_size = l;
            Svm.info("*");
        }

        for (i = 0; i < active_size; i++) {
            if (be_shrunk(i, Gmax1, Gmax2)) {
                active_size--;
                while (active_size > i) {
                    if (!be_shrunk(active_size, Gmax1, Gmax2)) {
                        swap_index(i, active_size);
                        break;
                    }
                    active_size--;
                }
            }
        }
    }

    double calculate_rho() {
        double r;
        int nr_free = 0;
        double ub = INF, lb = -INF, sum_free = 0;
        for (int i = 0; i < active_size; i++) {
            double yG = y[i] * G[i];

            if (is_upper_bound(i)) {
                if (y[i] < 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else if (is_lower_bound(i)) {
                if (y[i] > 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else {
                ++nr_free;
                sum_free += yG;
            }
        }

        if (nr_free > 0) {
            r = sum_free / nr_free;
        } else {
            r = (ub + lb) / 2;
        }

        return r;
    }

}
